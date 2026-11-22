package ru.mirea.bakery.rules;

import ru.mirea.bakery.model.Order;

import java.util.List;

/** В заявке должна быть хотя бы одна позиция. */
public class NonEmptyOrderRule implements OrderRule {
    @Override
    public List<String> check(Order order) {
        return order.getItems().isEmpty() ? List.of("в заявке нет позиций") : List.of();
    }
}
