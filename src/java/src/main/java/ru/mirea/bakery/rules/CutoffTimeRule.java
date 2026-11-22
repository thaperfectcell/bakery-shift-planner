package ru.mirea.bakery.rules;

import ru.mirea.bakery.model.Order;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Заявка должна поступить не позже окончания приёма: 12:00 дня, предшествующего отгрузке. */
public class CutoffTimeRule implements OrderRule {
    private final LocalTime cutoff;

    public CutoffTimeRule(LocalTime cutoff) {
        this.cutoff = cutoff;
    }

    @Override
    public List<String> check(Order order) {
        LocalDateTime deadline = order.getShipDate().minusDays(1).atTime(cutoff);
        if (order.getReceivedAt().isAfter(deadline)) {
            return List.of("заявка поступила после окончания приёма (" + cutoff + " накануне отгрузки)");
        }
        return List.of();
    }
}
