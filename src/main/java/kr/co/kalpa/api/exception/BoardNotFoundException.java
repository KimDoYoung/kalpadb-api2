package kr.co.kalpa.api.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException(String message) {
        super(message);
    }

    public BoardNotFoundException(Long boardId) {
        super("게시판을 찾을 수 없습니다 (ID: " + boardId + ")");
    }
}
