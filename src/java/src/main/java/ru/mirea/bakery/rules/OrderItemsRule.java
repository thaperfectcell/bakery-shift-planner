package ru.mirea.bakery.rules;

import ru.mirea.bakery.model.Order;
import ru.mirea.bakery.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

/** Каждая позиция ссылается на известное изделие и содержит положительное количество. */
public class OrderItemsRule implements OrderRule {
    @Override
    public List<String> check(Order order) {
        List<String> errors = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            if (!item.hasKnownProduct()) {
                errors.add("неизвестное изделие " + item.getProductId());
            } else if (item.getQuantity() <= 0) {
                errors.add("неположительное количество изделия " + item.getProductId());
            }
        }
        return errors;
    }
}
