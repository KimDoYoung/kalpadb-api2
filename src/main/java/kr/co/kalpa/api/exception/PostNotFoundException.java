package kr.co.kalpa.api.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }

    public PostNotFoundException(Long postId) {
        super("게시글을 찾을 수 없습니다 (ID: " + postId + ")");
    }
}
