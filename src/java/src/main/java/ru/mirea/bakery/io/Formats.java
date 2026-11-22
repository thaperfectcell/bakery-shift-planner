package ru.mirea.bakery.io;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Форматирование чисел и времени так же, как в Python-версии. */
public final class Formats {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private Formats() { }

    /** «12 345,6»: одна цифра после запятой, разряды через пробел. */
    public static String kg(double value) {
        return kg(new BigDecimal(value));
    }

    public static String kg(BigDecimal value) {
        String plain = value.setScale(1, RoundingMode.HALF_EVEN).toPlainString();
        boolean negative = plain.startsWith("-");
        if (negative) {
            plain = plain.substring(1);
        }
        int dot = plain.indexOf('.');
        String intPart = plain.substring(0, dot);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < intPart.length(); i++) {
            if (i > 0 && (intPart.length() - i) % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(intPart.charAt(i));
        }
        return (negative ? "-" : "") + grouped + "," + plain.substring(dot + 1);
    }

    public static String time(LocalDateTime moment) {
        return moment.format(TIME);
    }

    /** Число с округлением до digits знаков, записанное как repr(round(x, digits)) в Python. */
    public static String rounded(double value, int digits) {
        return repr(new BigDecimal(value).setScale(digits, RoundingMode.HALF_EVEN).doubleValue());
    }

    /** Кратчайшая запись дробного числа, как repr(float) в Python: 30.0, 12.9, 1234.57. */
    public static String repr(double value) {
        BigDecimal d = BigDecimal.valueOf(value).stripTrailingZeros();
        if (d.scale() <= 0) {
            d = d.setScale(1, RoundingMode.UNNECESSARY);
        }
        return d.toPlainString();
    }

    public static String repr(BigDecimal value) {
        return repr(value.doubleValue());
    }
}
