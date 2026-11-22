package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Delivery;
import ru.mirea.bakery.model.Order;
import ru.mirea.bakery.model.OrderItem;
import ru.mirea.bakery.rules.OrderRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Реестр поступивших заявок и правила их проверки. */
public class OrderRegistry {
    private final List<Order> orders;
    private final List<OrderRule> rules;

    public OrderRegistry(List<Order> orders, List<OrderRule> rules) {
        this.orders = List.copyOf(orders);
        this.rules = List.copyOf(rules);
    }

    /** Ближайшая дата отгрузки среди заявок. */
    public Optional<LocalDate> earliestShipDate() {
        return orders.stream().map(Order::getShipDate).min(LocalDate::compareTo);
    }

    /**
     * Операция «Свести заявки по изделиям». Заявки на дату отгрузки обрабатываются в порядке
     * поступления: заявка с ошибками отклоняется, повтор уже принятой заявки пропускается.
     */
    public Demand consolidate(LocalDate shipDate) {
        Demand demand = new Demand(shipDate);
        Map<String, String> seen = new HashMap<>();
        List<Order> dayOrders = new ArrayList<>(orders.stream()
                .filter(o -> o.getShipDate().equals(shipDate)).toList());
        dayOrders.sort(Order.byReceivedAt());
        for (Order order : dayOrders) {
            List<String> errors = validate(order);
            if (!errors.isEmpty()) {
                demand.reject(order.getId(), String.join("; ", errors));
                continue;
            }
            String key = order.duplicateKey();
            if (seen.containsKey(key)) {
                demand.duplicate(order.getId(), seen.get(key));
                continue;
            }
            seen.put(key, order.getId());
            demand.accept(order.getId());
            LocalDateTime deadline = order.deadline();
            for (OrderItem item : order.getItems()) {
                demand.addDelivery(item.getProduct(), new Delivery(deadline, order.getId(), item.getQuantity()));
            }
        }
        return demand;
    }

    private List<String> validate(Order order) {
        List<String> errors = new ArrayList<>();
        for (OrderRule rule : rules) {
            errors.addAll(rule.check(order));
        }
        return errors;
    }
}
