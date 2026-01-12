package kr.co.kalpa.api.exception;

public class DiaryAlreadyExistsException extends RuntimeException {
    public DiaryAlreadyExistsException(String ymd) {
        super("이미 해당 날짜의 일기가 존재합니다: " + ymd);
    }

    public DiaryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
