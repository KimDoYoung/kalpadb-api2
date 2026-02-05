package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.ApNodeCreateRequest;
import kr.co.kalpa.api.dto.request.ApNodeMoveRequest;
import kr.co.kalpa.api.dto.response.ApNodeResponse;
import kr.co.kalpa.api.entity.ApFile;
import kr.co.kalpa.api.entity.ApNode;
import kr.co.kalpa.api.exception.ApNodeNotFoundException;
import kr.co.kalpa.api.repository.ApFileRepository;
import kr.co.kalpa.api.repository.ApNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApNodeService {

    private final ApNodeRepository apNodeRepository;
    private final ApFileRepository apFileRepository;

    @Value("${apnode.file.base-dir}")
    private String apnodeBaseDir;

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ─── 폴더 관련 ────────────────────────────────────────

    /**
     * 루트 노드 조회 (없으면 자동 생성)
     */
    @Transactional
    public ApNode getRootNode() {
        return apNodeRepository.findByParentIdIsNullAndIsDeletedFalse()
                .orElseGet(() -> {
                    log.info("루트 노드가 없습니다. 자동 생성합니다.");
                    ApNode root = ApNode.builder()
                            .id(generateId())
                            .nodeType("D")
                            .parentId(null)
                            .name("/")
                            .depth(0)
                            .isDeleted(false)
                            .createDt(LocalDateTime.now())
                            .childCount(0)
                            .totalSize(0L)
                            .build();
                    return apNodeRepository.save(root);
                });
    }

    /**
     * 폴더 생성
     */
    @Transactional
    public ApNodeResponse createFolder(ApNodeCreateRequest request) {
        // 부모 노드 확인
        ApNode parent = apNodeRepository.findById(request.getParentId())
                .orElseThrow(() -> new ApNodeNotFoundException("부모 노드를 찾을 수 없습니다 (ID: " + request.getParentId() + ")"));

        if (!"D".equals(parent.getNodeType())) {
            throw new IllegalArgumentException("파일 노드 하위에 폴더를 생성할 수 없습니다");
        }

        // 동일 이름 중복 확인
        if (apNodeRepository.existsByParentIdAndNameAndIsDeletedFalse(request.getParentId(), request.getName())) {
            throw new IllegalArgumentException("같은 이름의 항목이 이미 존재합니다: " + request.getName());
        }

        // depth 제한 확인
        int newDepth = parent.getDepth() + 1;
        if (newDepth > 20) {
            throw new IllegalArgumentException("최대 깊이(20)를 초과할 수 없습니다");
        }

        ApNode folder = ApNode.builder()
                .id(generateId())
                .nodeType("D")
                .parentId(request.getParentId())
                .name(request.getName())
                .depth(newDepth)
                .isDeleted(false)
                .createDt(LocalDateTime.now())
                .childCount(0)
                .totalSize(0L)
                .build();

        ApNode saved = apNodeRepository.save(folder);
        log.info("폴더 생성: {} (parent: {})", saved.getName(), request.getParentId());

        // 부모의 child_count 갱신
        updateChildCount(request.getParentId());

        return ApNodeResponse.from(saved);
    }

    // ─── 공통 기능 ────────────────────────────────────────

    /**
     * 노드 이름 변경
     */
    @Transactional
    public ApNodeResponse renameNode(String nodeId, String newName) {
        ApNode node = findNodeOrThrow(nodeId);

        // 동일 이름 중복 확인
        if (apNodeRepository.existsByParentIdAndNameAndIsDeletedFalse(node.getParentId(), newName)) {
            throw new IllegalArgumentException("같은 이름의 항목이 이미 존재합니다: " + newName);
        }

        node.rename(newName);
        ApNode saved = apNodeRepository.save(node);
        log.info("노드 이름 변경: {} -> {}", nodeId, newName);

        return ApNodeResponse.from(saved);
    }

    /**
     * 노드 이동
     */
    @Transactional
    public ApNodeResponse moveNode(String nodeId, ApNodeMoveRequest request) {
        ApNode node = findNodeOrThrow(nodeId);
        String oldParentId = node.getParentId();

        ApNode targetParent = apNodeRepository.findById(request.getTargetParentId())
                .orElseThrow(() -> new ApNodeNotFoundException("대상 폴더를 찾을 수 없습니다 (ID: " + request.getTargetParentId() + ")"));

        if (!"D".equals(targetParent.getNodeType())) {
            throw new IllegalArgumentException("파일 노드로 이동할 수 없습니다");
        }

        // 자기 자신이나 자식으로 이동 방지
        if (nodeId.equals(request.getTargetParentId())) {
            throw new IllegalArgumentException("자기 자신으로 이동할 수 없습니다");
        }

        // 동일 이름 중복 확인
        if (apNodeRepository.existsByParentIdAndNameAndIsDeletedFalse(request.getTargetParentId(), node.getName())) {
            throw new IllegalArgumentException("대상 폴더에 같은 이름의 항목이 이미 존재합니다: " + node.getName());
        }

        int newDepth = targetParent.getDepth() + 1;
        int depthDiff = newDepth - node.getDepth();

        node.moveTo(request.getTargetParentId(), newDepth);
        apNodeRepository.save(node);

        // 하위 노드의 depth 갱신 (디렉토리인 경우)
        if ("D".equals(node.getNodeType())) {
            updateDescendantsDepth(nodeId, depthDiff);
        }

        // 이전/새 부모의 child_count 갱신
        if (oldParentId != null) updateChildCount(oldParentId);
        updateChildCount(request.getTargetParentId());

        log.info("노드 이동: {} -> parent {}", nodeId, request.getTargetParentId());
        return ApNodeResponse.from(node);
    }

    /**
     * 노드 삭제 (논리 삭제)
     */
    @Transactional
    public void deleteNode(String nodeId) {
        ApNode node = findNodeOrThrow(nodeId);

        if (node.getParentId() == null) {
            throw new IllegalArgumentException("루트 노드는 삭제할 수 없습니다");
        }

        // 재귀적으로 하위 노드 모두 논리 삭제
        List<ApNode> descendants = apNodeRepository.findAllDescendants(nodeId);
        for (ApNode desc : descendants) {
            desc.softDelete();
            apNodeRepository.save(desc);
        }

        log.info("노드 삭제: {} (하위 {}개 포함)", nodeId, descendants.size());

        // 부모의 child_count 갱신
        if (node.getParentId() != null) {
            updateChildCount(node.getParentId());
        }
    }

    // ─── 조회 기능 ────────────────────────────────────────

    /**
     * 특정 폴더의 하위 노드 목록 (파일정보 포함)
     */
    @Transactional(readOnly = true)
    public List<ApNodeResponse> getChildren(String folderId) {
        // "root"이면 루트 노드의 ID로 치환
        if ("root".equals(folderId)) {
            ApNode root = getRootNode();
            folderId = root.getId();
        }

        List<ApNode> children = apNodeRepository.findChildrenWithFile(folderId);
        return children.stream()
                .map(ApNodeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Breadcrumb 경로 조회
     */
    @Transactional(readOnly = true)
    public List<ApNodeResponse> getBreadcrumb(String nodeId) {
        if ("root".equals(nodeId)) {
            ApNode root = getRootNode();
            return List.of(ApNodeResponse.from(root));
        }

        List<ApNodeResponse> breadcrumb = new ArrayList<>();
        String currentId = nodeId;

        while (currentId != null) {
            ApNode node = apNodeRepository.findById(currentId).orElse(null);
            if (node == null) break;
            breadcrumb.add(0, ApNodeResponse.from(node));
            currentId = node.getParentId();
        }

        return breadcrumb;
    }

    /**
     * 전체 디렉토리 트리 조회
     */
    @Transactional(readOnly = true)
    public List<ApNodeResponse> getTree() {
        List<ApNode> allDirs = apNodeRepository.findAllDirectories();

        // parentId -> children 매핑
        Map<String, List<ApNodeResponse>> childrenMap = new LinkedHashMap<>();
        Map<String, ApNodeResponse> nodeMap = new LinkedHashMap<>();

        for (ApNode dir : allDirs) {
            ApNodeResponse resp = ApNodeResponse.from(dir);
            resp.setChildren(new ArrayList<>());
            nodeMap.put(dir.getId(), resp);

            String parentId = dir.getParentId();
            childrenMap.computeIfAbsent(parentId == null ? "ROOT" : parentId, k -> new ArrayList<>()).add(resp);
        }

        // 트리 조립
        for (ApNode dir : allDirs) {
            ApNodeResponse resp = nodeMap.get(dir.getId());
            List<ApNodeResponse> children = childrenMap.get(dir.getId());
            if (children != null) {
                resp.setChildren(children);
            }
        }

        // 루트 반환
        List<ApNodeResponse> roots = childrenMap.getOrDefault("ROOT", new ArrayList<>());
        return roots;
    }

    /**
     * 검색
     */
    @Transactional(readOnly = true)
    public List<ApNodeResponse> search(String keyword) {
        List<ApNode> results = apNodeRepository.searchByName(keyword);
        return results.stream()
                .map(ApNodeResponse::from)
                .collect(Collectors.toList());
    }

    // ─── 파일 업로드 ──────────────────────────────────────

    /**
     * 파일 업로드
     */
    @Transactional
    public List<ApNodeResponse> uploadFiles(String folderId, List<MultipartFile> files) throws IOException {
        String resolvedFolderId = folderId;
        if ("root".equals(resolvedFolderId)) {
            ApNode root = getRootNode();
            resolvedFolderId = root.getId();
        }

        final String targetFolderId = resolvedFolderId;
        ApNode parent = apNodeRepository.findById(targetFolderId)
                .orElseThrow(() -> new ApNodeNotFoundException("폴더를 찾을 수 없습니다 (ID: " + targetFolderId + ")"));

        if (!"D".equals(parent.getNodeType())) {
            throw new IllegalArgumentException("파일 노드에 업로드할 수 없습니다");
        }

        int newDepth = parent.getDepth() + 1;
        List<ApNodeResponse> responses = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();

        // 날짜 기반 폴더 생성
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path baseDirPath = Paths.get(apnodeBaseDir).toAbsolutePath();
        Path storagePath = baseDirPath.resolve(dateFolder);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String nodeId = generateId();
            String originalName = file.getOriginalFilename();

            // 동일 이름 중복 시 자동 리네임: 1.jpg → 1 (1).jpg → 1 (2).jpg
            originalName = resolveUniqueName(targetFolderId, originalName, usedNames);
            usedNames.add(originalName);

            String physicalName = generateId();

            // 확장자 보존
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String savedFileName = physicalName + ext;

            // 파일 저장
            Path filePath = storagePath.resolve(savedFileName);
            file.transferTo(filePath.toFile());

            // saved_path: base-dir 제외한 상대경로
            String savedPath = dateFolder + "/" + savedFileName;

            // SHA-256 해시
            String hash = computeSha256(filePath);

            // ap_node 생성
            ApNode fileNode = ApNode.builder()
                    .id(nodeId)
                    .nodeType("F")
                    .parentId(targetFolderId)
                    .name(originalName)
                    .depth(newDepth)
                    .isDeleted(false)
                    .createDt(LocalDateTime.now())
                    .childCount(0)
                    .totalSize(0L)
                    .build();
            fileNode = apNodeRepository.save(fileNode);

            // ap_file 생성
            ApFile apFile = ApFile.builder()
                    .node(fileNode)
                    .savedPath(savedPath)
                    .originalName(originalName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .sha256Hash(hash)
                    .uploadDt(LocalDateTime.now())
                    .build();
            apFileRepository.save(apFile);

            log.info("파일 업로드: {} -> {}", originalName, savedPath);

            ApNodeResponse resp = ApNodeResponse.from(fileNode);
            resp.setFileSize(file.getSize());
            resp.setContentType(file.getContentType());
            resp.setOriginalName(originalName);
            responses.add(resp);
        }

        // 부모의 child_count 갱신
        updateChildCount(targetFolderId);

        return responses;
    }

    /**
     * 파일 다운로드용 경로 반환
     */
    @Transactional(readOnly = true)
    public Path getDownloadPath(String nodeId) {
        ApNode node = findNodeOrThrow(nodeId);
        if (!"F".equals(node.getNodeType())) {
            throw new IllegalArgumentException("파일 노드가 아닙니다");
        }

        ApFile apFile = apFileRepository.findById(nodeId)
                .orElseThrow(() -> new ApNodeNotFoundException("파일 정보를 찾을 수 없습니다 (ID: " + nodeId + ")"));

        Path baseDirPath = Paths.get(apnodeBaseDir).toAbsolutePath();
        return baseDirPath.resolve(apFile.getSavedPath());
    }

    /**
     * 파일 원본 이름 반환
     */
    @Transactional(readOnly = true)
    public String getOriginalName(String nodeId) {
        ApFile apFile = apFileRepository.findById(nodeId)
                .orElseThrow(() -> new ApNodeNotFoundException("파일 정보를 찾을 수 없습니다 (ID: " + nodeId + ")"));
        return apFile.getOriginalName();
    }

    // ─── 내부 헬퍼 ────────────────────────────────────────

    private ApNode findNodeOrThrow(String nodeId) {
        return apNodeRepository.findById(nodeId)
                .filter(n -> !n.getIsDeleted())
                .orElseThrow(() -> new ApNodeNotFoundException(nodeId, true));
    }

    private void updateChildCount(String parentId) {
        int count = apNodeRepository.countChildrenByParentId(parentId);
        apNodeRepository.findById(parentId).ifPresent(parent -> {
            parent.updateChildCount(count);
            apNodeRepository.save(parent);
        });
    }

    private void updateDescendantsDepth(String parentId, int depthDiff) {
        List<ApNode> children = apNodeRepository.findChildrenByParentId(parentId);
        for (ApNode child : children) {
            child.moveTo(child.getParentId(), child.getDepth() + depthDiff);
            apNodeRepository.save(child);
            if ("D".equals(child.getNodeType())) {
                updateDescendantsDepth(child.getId(), depthDiff);
            }
        }
    }

    private String resolveUniqueName(String parentId, String name, Set<String> usedNames) {
        if (!usedNames.contains(name) && !apNodeRepository.existsByParentIdAndNameAndIsDeletedFalse(parentId, name)) {
            return name;
        }
        String baseName = name;
        String ext = "";
        int dotIdx = name.lastIndexOf(".");
        if (dotIdx > 0) {
            baseName = name.substring(0, dotIdx);
            ext = name.substring(dotIdx);
        }
        for (int i = 1; i <= 100; i++) {
            String candidate = baseName + " (" + i + ")" + ext;
            if (!usedNames.contains(candidate) && !apNodeRepository.existsByParentIdAndNameAndIsDeletedFalse(parentId, candidate)) {
                return candidate;
            }
        }
        return baseName + " (" + System.currentTimeMillis() + ")" + ext;
    }

    private String computeSha256(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.warn("SHA-256 해시 계산 실패: {}", filePath, e);
            return null;
        }
    }
}
