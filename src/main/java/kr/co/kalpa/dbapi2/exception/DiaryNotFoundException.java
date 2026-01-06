package kr.co.kalpa.dbapi2.exception;

public class DiaryNotFoundException extends RuntimeException {
    public DiaryNotFoundException(String ymd) {
        super("일기를 찾을 수 없습니다: " + ymd);
    }

    public DiaryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
