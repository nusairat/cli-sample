package com.wallet.utils;

import org.slf4j.LoggerFactory;

import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateParser {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DateParser.class);

    private static final DateTimeFormatter DTF_FULL_DATE_WITH_OPTIONAL_TIME = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd[[ ]['T']HH:mm[:ss][.SSS]['Z'][Z]]");

    private static final DateTimeFormatter DTF_COMMAND_LINE = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd[['.']HH:mm[:ss][.SSS]['Z'][Z]]");

    public static LocalDateTime toLocalDateTimeCommandLine(String s) {
        return toLocalDateTime(s, DTF_COMMAND_LINE);
    }

    public static LocalDateTime toLocalDateTime(String s) {
        return toLocalDateTime(s, DTF_FULL_DATE_WITH_OPTIONAL_TIME);
    }

    public static LocalDateTime toLocalDateTime(String s, DateTimeFormatter formater) {
        LocalDateTime dateTime = null;
        if (checkPattern(s, formater)) {
            // Now parse and handle
            TemporalAccessor ta = formater.parseBest(s, ZonedDateTime::from, LocalDateTime::from, LocalDate::from, YearMonth::from);
            if (ta instanceof ZonedDateTime) {
                dateTime = ((ZonedDateTime)ta).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            }
            else if (ta instanceof LocalDate) {
                dateTime = ((LocalDate)ta).atStartOfDay();
            }
            else if (ta instanceof  LocalDateTime) {
                dateTime = (LocalDateTime) ta;
            }
            else {
                log.warn("Encountered a format we don't recognize for {} / {}", s, ta);
            }
        }
        else {
            log.warn("Parsing failed {}", s);
        }

        return dateTime;
    }

    public static boolean checkPattern(String s, DateTimeFormatter formatter) {
        if (s != null) {
            ParsePosition pos = new ParsePosition(0);
            formatter.parseUnresolved(s, pos);

            if (pos.getErrorIndex() < 0 && pos.getIndex() >= s.length()) {
                return true;
            }
        }
        return false;
    }
}
