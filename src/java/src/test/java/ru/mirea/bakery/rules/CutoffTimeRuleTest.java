package ru.mirea.bakery.rules;

import org.junit.jupiter.api.Test;
import ru.mirea.bakery.model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CutoffTimeRuleTest {
    private final OrderRule rule = new CutoffTimeRule(LocalTime.NOON);

    private static Order receivedAt(String moment) {
        return new Order("Z1", "D01", null, LocalDate.of(2026, 11, 5), "EDI", LocalDateTime.parse(moment));
    }

    @Test
    void orderBeforeCutoffIsAccepted() {
        assertTrue(rule.check(receivedAt("2026-11-04T12:00")).isEmpty());
    }

    @Test
    void orderAfterCutoffIsRejected() {
        assertEquals(1, rule.check(receivedAt("2026-11-04T12:01")).size());
    }
}
