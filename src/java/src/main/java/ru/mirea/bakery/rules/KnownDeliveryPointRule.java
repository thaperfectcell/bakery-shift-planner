package ru.mirea.bakery.rules;

import ru.mirea.bakery.model.Order;

import java.util.List;

/** Точка доставки заявки должна быть в справочнике. */
public class KnownDeliveryPointRule implements OrderRule {
    @Override
    public List<String> check(Order order) {
        if (order.getPoint() == null) {
            return List.of("неизвестная точка доставки " + order.getPointId());
        }
        return List.of();
    }
}
