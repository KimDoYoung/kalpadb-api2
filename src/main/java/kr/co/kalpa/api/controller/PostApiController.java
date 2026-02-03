package kr.co.kalpa.api.controller;

import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.request.PostCreateRequest;
import kr.co.kalpa.api.dto.request.PostUpdateRequest;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.dto.response.PostResponse;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.service.FileMatchService;
import kr.co.kalpa.api.service.FileService;
import kr.co.kalpa.api.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostApiController {

    private final PostService postService;
    private final FileMatchService fileMatchService;
    private final FileService fileService;

    /**
     * 게시글 목록 조회
     * GET /api/posts?boardId=1&keyword=검색어&startDate=20260201&endDate=20260228&page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startYmd,
            @RequestParam(required = false) String endYmd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        log.info("게시글 목록 조회 - boardId: {}, keyword: {}, startYmd: {}, endYmd: {}, page: {}, size: {}",
                boardId, keyword, startYmd, endYmd, page, size);

        // Parse sort parameter
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sort.length > 0 ? sort[0] : "createdAt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<PostResponse> response = postService.getPosts(boardId, keyword, startYmd, endYmd, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("게시글 목록 조회 성공", response));
    }

    /**
     * 게시글 상세 조회
     * GET /api/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        log.info("게시글 상세 조회: {}", postId);
        PostResponse post = postService.getPost(postId);
        return ResponseEntity.ok(
                ApiResponse.success("게시글 조회 성공", post));
    }

    /**
     * 게시글 생성
     * POST /api/posts
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestParam Long boardId,
            @RequestParam String title,
            @RequestParam(required = false) String author,
            @RequestParam String contentType,
            @RequestParam String content,
            @RequestParam String baseYmd,
            @RequestParam(required = false) List<MultipartFile> files) {

        log.info("게시글 생성 - boardId: {}, title: {}", boardId, title);

        PostCreateRequest request = new PostCreateRequest(
                boardId, title, author, contentType, content, baseYmd, files
        );

        PostResponse response = postService.createPost(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 생성되었습니다", response));
    }

    /**
     * 게시글 수정
     * PUT /api/posts/{postId}
     */
    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String baseYmd,
            @RequestParam(required = false) List<MultipartFile> newFiles,
            @RequestParam(required = false) String deletedFileIds) {

        log.info("게시글 수정: {}", postId);

        PostUpdateRequest request = new PostUpdateRequest(
                title, author, contentType, content, baseYmd, newFiles, deletedFileIds
        );

        PostResponse response = postService.updatePost(postId, request);

        return ResponseEntity.ok(
                ApiResponse.success("게시글이 수정되었습니다", response));
    }

    /**
     * 게시글 삭제
     * DELETE /api/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        log.info("게시글 삭제: {}", postId);
        postService.deletePost(postId);
        return ResponseEntity.ok(
                ApiResponse.success("게시글이 삭제되었습니다", null));
    }

    /**
     * 조회수 증가
     * PATCH /api/posts/{postId}/view-count
     */
    @PatchMapping("/{postId}/view-count")
    public ResponseEntity<ApiResponse<PostResponse>> incrementViewCount(@PathVariable Long postId) {
        log.info("조회수 증가: {}", postId);
        PostResponse response = postService.incrementViewCount(postId);
        return ResponseEntity.ok(
                ApiResponse.success("조회수가 증가되었습니다", response));
    }

    /**
     * 게시글 파일 목록 조회 (ATTACHMENT만 - EDITOR_IMAGE는 content에 포함)
     * GET /api/posts/{postId}/files
     */
    @GetMapping("/{postId}/files")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getPostFiles(@PathVariable Long postId) {
        log.info("게시글 파일 목록 조회: {}", postId);

        // ATTACHMENT 파일만 조회
        var matches = fileMatchService.getMatchesByTargetAndType("posts", postId, FileType.ATTACHMENT);

        List<FileResponse> files = new ArrayList<>();
        for (var match : matches) {
            try {
                var file = fileService.getFile(match.getFileId());
                files.add(FileResponse.from(file));
            } catch (Exception e) {
                log.error("파일 조회 실패: {}", match.getFileId(), e);
            }
        }

        return ResponseEntity.ok(
                ApiResponse.success("파일 목록 조회 성공", files));
    }
}
