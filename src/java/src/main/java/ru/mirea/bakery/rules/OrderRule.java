package ru.mirea.bakery.rules;

import ru.mirea.bakery.model.Order;

import java.util.List;

/** Правило проверки заявки. Возвращает список найденных ошибок (пустой, если заявка корректна). */
public interface OrderRule {
    List<String> check(Order order);
}
