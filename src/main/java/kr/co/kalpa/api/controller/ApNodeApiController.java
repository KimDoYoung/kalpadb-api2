package kr.co.kalpa.api.controller;

import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.request.ApNodeCreateRequest;
import kr.co.kalpa.api.dto.request.ApNodeMoveRequest;
import kr.co.kalpa.api.dto.request.ApNodeRenameRequest;
import kr.co.kalpa.api.dto.response.ApNodeResponse;
import kr.co.kalpa.api.service.ApNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
@Slf4j
public class ApNodeApiController {

    private final ApNodeService apNodeService;

    /**
     * 트리 데이터 조회
     * GET /api/nodes/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<ApNodeResponse>>> getTree() {
        log.info("트리 데이터 조회");
        List<ApNodeResponse> tree = apNodeService.getTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * 폴더 하위 노드 목록
     * GET /api/nodes/{folderId}/children
     */
    @GetMapping("/{folderId}/children")
    public ResponseEntity<ApiResponse<List<ApNodeResponse>>> getChildren(@PathVariable String folderId) {
        log.info("폴더 하위 목록 조회: {}", folderId);
        List<ApNodeResponse> children = apNodeService.getChildren(folderId);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    /**
     * Breadcrumb 경로 조회
     * GET /api/nodes/{folderId}/breadcrumb
     */
    @GetMapping("/{folderId}/breadcrumb")
    public ResponseEntity<ApiResponse<List<ApNodeResponse>>> getBreadcrumb(@PathVariable String folderId) {
        log.info("Breadcrumb 조회: {}", folderId);
        List<ApNodeResponse> breadcrumb = apNodeService.getBreadcrumb(folderId);
        return ResponseEntity.ok(ApiResponse.success(breadcrumb));
    }

    /**
     * 폴더 생성
     * POST /api/nodes
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ApNodeResponse>> createFolder(@RequestBody ApNodeCreateRequest request) {
        log.info("폴더 생성: {} (parent: {})", request.getName(), request.getParentId());
        ApNodeResponse response = apNodeService.createFolder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("폴더가 생성되었습니다", response));
    }

    /**
     * 노드 이름 변경
     * PUT /api/nodes/{nodeId}
     */
    @PutMapping("/{nodeId}")
    public ResponseEntity<ApiResponse<ApNodeResponse>> renameNode(
            @PathVariable String nodeId,
            @RequestBody ApNodeRenameRequest request) {
        log.info("노드 이름 변경: {} -> {}", nodeId, request.getName());
        ApNodeResponse response = apNodeService.renameNode(nodeId, request.getName());
        return ResponseEntity.ok(ApiResponse.success("이름이 변경되었습니다", response));
    }

    /**
     * 노드 이동
     * POST /api/nodes/{nodeId}/move
     */
    @PostMapping("/{nodeId}/move")
    public ResponseEntity<ApiResponse<ApNodeResponse>> moveNode(
            @PathVariable String nodeId,
            @RequestBody ApNodeMoveRequest request) {
        log.info("노드 이동: {} -> {}", nodeId, request.getTargetParentId());
        ApNodeResponse response = apNodeService.moveNode(nodeId, request);
        return ResponseEntity.ok(ApiResponse.success("이동되었습니다", response));
    }

    /**
     * 노드 삭제
     * DELETE /api/nodes/{nodeId}
     */
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNode(@PathVariable String nodeId) {
        log.info("노드 삭제: {}", nodeId);
        apNodeService.deleteNode(nodeId);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다", null));
    }

    /**
     * 파일 업로드
     * POST /api/nodes/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<ApNodeResponse>>> uploadFiles(
            @RequestParam String folderId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        log.info("파일 업로드: folderId={}, 파일수={}", folderId, files.size());
        List<ApNodeResponse> responses = apNodeService.uploadFiles(folderId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("파일이 업로드되었습니다", responses));
    }

    /**
     * 파일 다운로드
     * GET /api/nodes/{nodeId}/download
     */
    @GetMapping("/{nodeId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String nodeId) throws IOException {
        log.info("파일 다운로드: {}", nodeId);

        Path filePath = apNodeService.getDownloadPath(nodeId);
        String originalName = apNodeService.getOriginalName(nodeId);

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    /**
     * 검색
     * GET /api/nodes/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ApNodeResponse>>> search(@RequestParam String keyword) {
        log.info("노드 검색: {}", keyword);
        List<ApNodeResponse> results = apNodeService.search(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
