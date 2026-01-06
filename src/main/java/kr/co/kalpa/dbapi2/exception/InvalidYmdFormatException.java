package kr.co.kalpa.dbapi2.exception;

public class InvalidYmdFormatException extends RuntimeException {
    public InvalidYmdFormatException(String ymd) {
        super("잘못된 날짜 형식입니다: " + ymd + " (YYYYMMDD 형식이어야 합니다)");
    }

    public InvalidYmdFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
