package kr.co.kalpa.dbapi2.util;

import kr.co.kalpa.dbapi2.exception.InvalidYmdFormatException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class YmdValidator {

    private static final DateTimeFormatter YMD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Validate YMD format (8 digits and valid date)
     * @param ymd Date string in YYYYMMDD format
     * @throws InvalidYmdFormatException if format is invalid
     */
    public static void validate(String ymd) {
        if (ymd == null || !ymd.matches("^\\d{8}$")) {
            throw new InvalidYmdFormatException(ymd);
        }

        try {
            LocalDate.parse(ymd, YMD_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("Invalid date value: {}", ymd, e);
            throw new InvalidYmdFormatException(ymd);
        }
    }

    /**
     * Validate YMD format without throwing exception
     * @param ymd Date string in YYYYMMDD format
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String ymd) {
        if (ymd == null || !ymd.matches("^\\d{8}$")) {
            return false;
        }

        try {
            LocalDate.parse(ymd, YMD_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
