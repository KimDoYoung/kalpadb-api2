package kr.co.kalpa.api.exception;

public class ApNodeNotFoundException extends RuntimeException {
    public ApNodeNotFoundException(String message) {
        super(message);
    }

    public ApNodeNotFoundException(String nodeId, boolean isId) {
        super("노드를 찾을 수 없습니다 (ID: " + nodeId + ")");
    }
}
