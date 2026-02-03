package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.BoardCreateRequest;
import kr.co.kalpa.api.dto.request.BoardUpdateRequest;
import kr.co.kalpa.api.dto.response.BoardResponse;
import kr.co.kalpa.api.entity.Board;
import kr.co.kalpa.api.exception.BoardNotFoundException;
import kr.co.kalpa.api.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 게시판 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> getAllBoards() {
        log.info("모든 게시판 조회");
        return boardRepository.findAll()
                .stream()
                .map(BoardResponse::from)
                .toList();
    }

    /**
     * 게시판 상세 조회
     */
    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long boardId) {
        log.info("게시판 조회: {}", boardId);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
        return BoardResponse.from(board);
    }

    /**
     * 게시판 생성
     */
    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request) {
        log.info("게시판 생성: {}", request.getBoardCode());

        // 게시판 코드 중복 확인
        if (boardRepository.findByBoardCode(request.getBoardCode()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 게시판 코드입니다: " + request.getBoardCode());
        }

        Board board = Board.builder()
                .boardCode(request.getBoardCode())
                .boardNameKor(request.getBoardNameKor())
                .contentType(request.getContentType())
                .description(request.getDescription())
                .build();

        Board saved = boardRepository.save(board);
        log.info("게시판 생성 완료: {}", saved.getId());

        return BoardResponse.from(saved);
    }

    /**
     * 게시판 수정
     */
    @Transactional
    public BoardResponse updateBoard(Long boardId, BoardUpdateRequest request) {
        log.info("게시판 수정: {}", boardId);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        board.update(request.getBoardNameKor(), request.getDescription());
        Board updated = boardRepository.save(board);

        log.info("게시판 수정 완료: {}", boardId);
        return BoardResponse.from(updated);
    }

    /**
     * 게시판 삭제
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        log.info("게시판 삭제: {}", boardId);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        boardRepository.delete(board);
        log.info("게시판 삭제 완료: {}", boardId);
    }
}
