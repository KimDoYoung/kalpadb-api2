package kr.co.kalpa.api.exception;

public class JangbiNotFoundException extends RuntimeException {
    public JangbiNotFoundException(Long id) {
        super("장비를 찾을 수 없습니다: " + id);
    }

    public JangbiNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
