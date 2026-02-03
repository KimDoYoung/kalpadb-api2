package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.PostCreateRequest;
import kr.co.kalpa.api.dto.request.PostUpdateRequest;
import kr.co.kalpa.api.dto.response.PostResponse;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.entity.Post;
import kr.co.kalpa.api.exception.BoardNotFoundException;
import kr.co.kalpa.api.exception.PostNotFoundException;
import kr.co.kalpa.api.repository.BoardRepository;
import kr.co.kalpa.api.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final FileMatchService fileMatchService;
    private final FileService fileService;

    // 이미지 파일ID 추출 패턴
    private static final Pattern FILE_ID_PATTERN = Pattern.compile("/api/file/download/(\\d+)");

    /**
     * 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Long boardId, String keyword, String startYmd, String endYmd,
            Pageable pageable) {
        log.info("게시글 목록 조회 - boardId: {}, keyword: {}, startYmd: {}, endYmd: {}", boardId, keyword, startYmd, endYmd);

        Page<Post> posts;

        if (boardId != null) {
            // 게시판 존재 여부 확인
            boardRepository.findById(boardId)
                    .orElseThrow(() -> new BoardNotFoundException(boardId));

            if (keyword != null && !keyword.isEmpty() && startYmd != null && endYmd != null) {
                posts = postRepository.findByBoardIdAndKeywordAndDateRange(boardId, keyword, startYmd, endYmd,
                        pageable);
            } else if (keyword != null && !keyword.isEmpty()) {
                posts = postRepository.findByBoardIdAndKeyword(boardId, keyword, pageable);
            } else if (startYmd != null && endYmd != null) {
                posts = postRepository.findByBoardIdAndDateRange(boardId, startYmd, endYmd, pageable);
            } else {
                posts = postRepository.findByBoardId(boardId, pageable);
            }
        } else {
            if (startYmd != null && endYmd != null) {
                posts = postRepository.findByDateRange(startYmd, endYmd, pageable);
            } else if (keyword != null && !keyword.isEmpty()) {
                posts = postRepository.findByKeyword(keyword, pageable);
            } else {
                posts = postRepository.findAll(pageable);
            }
        }

        return posts.map(post -> {
            PostResponse response = PostResponse.fromWithoutContent(post);
            int attachmentCount = fileMatchService.countMatchesByTargetAndType("posts", post.getId(),
                    FileType.ATTACHMENT);
            response.setAttachmentCount(attachmentCount);
            return response;
        });
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        log.info("게시글 상세 조회: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return PostResponse.from(post);
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        log.info("게시글 생성 - boardId: {}, title: {}", request.getBoardId(), request.getTitle());

        // 게시판 존재 여부 확인
        boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new BoardNotFoundException(request.getBoardId()));

        Post post = Post.builder()
                .boardId(request.getBoardId())
                .title(request.getTitle())
                .author(request.getAuthor())
                .contentType(request.getContentType())
                .content(request.getContent())
                .baseYmd(request.getBaseYmd())
                .viewCount(0)
                .build();

        Post saved = postRepository.save(post);
        log.info("게시글 생성 완료: {}", saved.getId());

        // Content에서 이미지 추출 및 file_match 생성
        syncContentImages(saved.getId(), request.getContent(), request.getContentType());

        // 첨부파일 처리
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile file : request.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        var savedFile = fileService.saveFile(file);
                        fileMatchService.createMatch("posts", saved.getId(), savedFile.getFileId(),
                                FileType.ATTACHMENT);
                    } catch (IOException e) {
                        log.warn("파일 저장 실패: {}", file.getOriginalFilename(), e);
                    }
                }
            }
        }

        return PostResponse.from(saved);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        log.info("게시글 수정: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 업데이트할 필드만 변경
        if (request.getTitle() != null) {
            post.update(
                    request.getTitle(),
                    request.getAuthor() != null ? request.getAuthor() : post.getAuthor(),
                    request.getContentType() != null ? request.getContentType() : post.getContentType(),
                    request.getContent() != null ? request.getContent() : post.getContent(),
                    request.getBaseYmd() != null ? request.getBaseYmd() : post.getBaseYmd());
        }

        Post updated = postRepository.save(post);

        // Content 변경 시 이미지 재동기화
        if (request.getContent() != null) {
            syncContentImages(updated.getId(), request.getContent(),
                    request.getContentType() != null ? request.getContentType() : updated.getContentType());
        }

        // 새로운 첨부파일 추가
        if (request.getNewFiles() != null && !request.getNewFiles().isEmpty()) {
            for (MultipartFile file : request.getNewFiles()) {
                if (!file.isEmpty()) {
                    try {
                        var savedFile = fileService.saveFile(file);
                        fileMatchService.createMatch("posts", postId, savedFile.getFileId(), FileType.ATTACHMENT);
                    } catch (IOException e) {
                        log.warn("파일 저장 실패: {}", file.getOriginalFilename(), e);
                    }
                }
            }
        }

        // 기존 첨부파일 삭제
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isBlank()) {
            List<Long> deletedIds = parseDeletedFileIds(request.getDeletedFileIds());
            for (Long fileId : deletedIds) {
                fileMatchService.deleteMatch("posts", postId, fileId);
            }
        }

        log.info("게시글 수정 완료: {}", postId);
        return PostResponse.from(updated);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId) {
        log.info("게시글 삭제: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 관련 file_match 삭제 (파일은 보존)
        fileMatchService.deleteMatchesByTarget("posts", postId);

        postRepository.delete(post);
        log.info("게시글 삭제 완료: {}", postId);
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public PostResponse incrementViewCount(Long postId) {
        log.info("조회수 증가: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        post.incrementViewCount();
        Post updated = postRepository.save(post);

        return PostResponse.from(updated);
    }

    /**
     * Content에서 이미지 파일ID 추출 및 file_match 동기화
     * HTML: <img src="/api/file/download/123">
     * Markdown: ![alt](/api/file/download/123)
     */
    private void syncContentImages(Long postId, String content, String contentType) {
        if (content == null || content.isBlank()) {
            return;
        }

        // Content에서 파일ID 추출
        Set<Long> imageFileIds = extractFileIdsFromContent(content);

        log.info("게시글 {} - 추출된 이미지 파일ID: {}", postId, imageFileIds);

        // file_match에 레코드 생성
        for (Long fileId : imageFileIds) {
            try {
                fileMatchService.createMatch("posts", postId, fileId, FileType.EDITOR_IMAGE);
            } catch (Exception e) {
                log.warn("파일 매칭 생성 실패 - postId: {}, fileId: {}", postId, fileId, e);
            }
        }
    }

    /**
     * Content에서 파일ID 추출
     */
    private Set<Long> extractFileIdsFromContent(String content) {
        Set<Long> fileIds = new HashSet<>();
        Matcher matcher = FILE_ID_PATTERN.matcher(content);

        while (matcher.find()) {
            try {
                Long fileId = Long.parseLong(matcher.group(1));
                fileIds.add(fileId);
            } catch (NumberFormatException e) {
                log.warn("파일ID 파싱 실패: {}", matcher.group(1));
            }
        }

        return fileIds;
    }

    /**
     * 삭제할 파일ID 파싱
     */
    private List<Long> parseDeletedFileIds(String deletedFileIds) {
        List<Long> ids = new ArrayList<>();
        try {
            String trimmed = deletedFileIds.replaceAll("[\\[\\]\\s]", "");
            if (!trimmed.isEmpty()) {
                String[] idArray = trimmed.split(",");
                for (String id : idArray) {
                    ids.add(Long.parseLong(id.trim()));
                }
            }
        } catch (Exception e) {
            log.warn("삭제 파일ID 파싱 실패: {}", deletedFileIds, e);
        }
        return ids;
    }
}
