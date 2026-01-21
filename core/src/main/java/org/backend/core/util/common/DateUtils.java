package org.backend.core.util.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {

    private DateUtils() {}


     // 문자열 월 포맷 표준: YYYY-MM (예: 2026-01)
    public static final DateTimeFormatter YEAR_MONTH_DASH = DateTimeFormatter.ofPattern("yyyy-MM");

     // YYYY-MM 문자열 유효성 검증
    public static boolean isValidYearMonth(String ym) {
        if (ym == null) return false;
        try {
            YearMonth.parse(ym, YEAR_MONTH_DASH);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

     // YYYY-MM -> YearMonth
    public static YearMonth parseYearMonth(String ym) {
        if (!isValidYearMonth(ym)) {
            throw new IllegalArgumentException("Invalid yearMonth format. expected yyyy-MM, value=" + ym);
        }
        return YearMonth.parse(ym, YEAR_MONTH_DASH);
    }

     // YearMonth -> YYYY-MM
    public static String formatYearMonth(YearMonth ym) {
        if (ym == null) throw new IllegalArgumentException("YearMonth is null");
        return ym.format(YEAR_MONTH_DASH);
    }

     // YYYY-MM -> YYYYMM(int) (예: 2026-01 -> 202601)
     // invoice.billing_month(INT) 같은 곳에 사용 가능
    public static int toIntYYYYMM(String ym) {
        YearMonth yearMonth = parseYearMonth(ym);
        return yearMonth.getYear() * 100 + yearMonth.getMonthValue();
    }

     // YYYYMM(int) -> YYYY-MM (예: 202601 -> 2026-01)
    public static String fromIntYYYYMM(int yyyymm) {
        int year = yyyymm / 100;
        int month = yyyymm % 100;
        YearMonth ym = YearMonth.of(year, month);
        return formatYearMonth(ym);
    }

     // YYYY-MM의 시작일시 (00:00:00)
    public static LocalDateTime startOfMonth(String ym) {
        YearMonth yearMonth = parseYearMonth(ym);
        LocalDate first = yearMonth.atDay(1);
        return first.atStartOfDay();
    }

     // YYYY-MM의 마지막일시 (23:59:59.999999999)
    public static LocalDateTime endOfMonth(String ym) {
        YearMonth yearMonth = parseYearMonth(ym);
        LocalDate last = yearMonth.atEndOfMonth();
        return last.atTime(23, 59, 59, 999_999_999);
    }
}