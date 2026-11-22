package ru.mirea.bakery.model;

import java.time.LocalDateTime;
import java.util.Comparator;

/** Поставка изделия по принятой заявке: срок готовности, номер заявки, количество. */
public record Delivery(LocalDateTime deadline, String orderId, int quantity) {

    /** Порядок как у кортежей в Python-версии: срок, затем заявка, затем количество. */
    public static final Comparator<Delivery> ORDER = Comparator
            .comparing(Delivery::deadline)
            .thenComparing(Delivery::orderId)
            .thenComparingInt(Delivery::quantity);
}
