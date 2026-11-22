package ru.mirea.bakery.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Заявка покупателя на дату отгрузки для одной точки доставки. Позиции - части заявки (композиция). */
public class Order {
    private final String id;
    private final String pointId;
    private final DeliveryPoint point;   // null, если точка не найдена в справочнике
    private final LocalDate shipDate;
    private final String channel;
    private final LocalDateTime receivedAt;
    private final List<OrderItem> items = new ArrayList<>();

    public Order(String id, String pointId, DeliveryPoint point, LocalDate shipDate,
                 String channel, LocalDateTime receivedAt) {
        this.id = id;
        this.pointId = pointId;
        this.point = point;
        this.shipDate = shipDate;
        this.channel = channel;
        this.receivedAt = receivedAt;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    /** Срок готовности продукции по заявке. */
    public LocalDateTime deadline() {
        return point.deadlineFor(shipDate);
    }

    /** Ключ для поиска повторов: точка доставки и состав заявки без учёта порядка позиций. */
    public String duplicateKey() {
        return pointId + "|" + items.stream()
                .map(i -> i.getProductId() + "x" + i.getQuantity())
                .sorted()
                .collect(Collectors.joining(","));
    }

    public static Comparator<Order> byReceivedAt() {
        return Comparator.comparing(Order::getReceivedAt);
    }

    public String getId() { return id; }
    public String getPointId() { return pointId; }
    public DeliveryPoint getPoint() { return point; }
    public LocalDate getShipDate() { return shipDate; }
    public String getChannel() { return channel; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
}
