package kr.co.kalpa.api.controller;

import jakarta.validation.Valid;
import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.request.BoardCreateRequest;
import kr.co.kalpa.api.dto.request.BoardUpdateRequest;
import kr.co.kalpa.api.dto.response.BoardResponse;
import kr.co.kalpa.api.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardApiController {

    private final BoardService boardService;

    /**
     * 게시판 목록 조회
     * GET /api/boards
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoards() {
        log.info("게시판 목록 조회");
        List<BoardResponse> boards = boardService.getAllBoards();
        return ResponseEntity.ok(
                ApiResponse.success("게시판 목록 조회 성공", boards)
        );
    }

    /**
     * 게시판 상세 조회
     * GET /api/boards/{boardId}
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoard(@PathVariable Long boardId) {
        log.info("게시판 상세 조회: {}", boardId);
        BoardResponse board = boardService.getBoard(boardId);
        return ResponseEntity.ok(
                ApiResponse.success("게시판 조회 성공", board)
        );
    }

    /**
     * 게시판 생성 (관리자만)
     * POST /api/boards
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @Valid @RequestBody BoardCreateRequest request) {
        log.info("게시판 생성: {}", request.getBoardCode());
        BoardResponse board = boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시판이 생성되었습니다", board));
    }

    /**
     * 게시판 수정 (관리자만)
     * PUT /api/boards/{boardId}
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequest request) {
        log.info("게시판 수정: {}", boardId);
        BoardResponse board = boardService.updateBoard(boardId, request);
        return ResponseEntity.ok(
                ApiResponse.success("게시판이 수정되었습니다", board)
        );
    }

    /**
     * 게시판 삭제 (관리자만)
     * DELETE /api/boards/{boardId}
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(@PathVariable Long boardId) {
        log.info("게시판 삭제: {}", boardId);
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(
                ApiResponse.success("게시판이 삭제되었습니다", null)
        );
    }
}
