package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Delivery;
import ru.mirea.bakery.model.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/** Результат сводки заявок: потребность по изделиям, поставки и итоги проверки заявок. */
public class Demand {
    public record Duplicate(String orderId, String duplicateOf) { }
    public record Rejection(String orderId, String reason) { }

    private final LocalDate shipDate;
    private final SortedMap<Product, Integer> quantities = new TreeMap<>(Comparator.comparing(Product::getId));
    private final Map<Product, List<Delivery>> deliveries = new TreeMap<>(Comparator.comparing(Product::getId));
    private final List<String> accepted = new ArrayList<>();
    private final List<Duplicate> duplicates = new ArrayList<>();
    private final List<Rejection> rejections = new ArrayList<>();

    Demand(LocalDate shipDate) {
        this.shipDate = shipDate;
    }

    void addDelivery(Product product, Delivery delivery) {
        quantities.merge(product, delivery.quantity(), Integer::sum);
        deliveries.computeIfAbsent(product, p -> new ArrayList<>()).add(delivery);
    }

    void accept(String orderId) { accepted.add(orderId); }
    void duplicate(String orderId, String duplicateOf) { duplicates.add(new Duplicate(orderId, duplicateOf)); }
    void reject(String orderId, String reason) { rejections.add(new Rejection(orderId, reason)); }

    public boolean isEmpty() { return quantities.isEmpty(); }
    public LocalDate getShipDate() { return shipDate; }
    public SortedMap<Product, Integer> getQuantities() { return Collections.unmodifiableSortedMap(quantities); }
    public List<Delivery> deliveriesOf(Product product) { return Collections.unmodifiableList(deliveries.get(product)); }
    public List<String> getAccepted() { return Collections.unmodifiableList(accepted); }
    public List<Duplicate> getDuplicates() { return Collections.unmodifiableList(duplicates); }
    public List<Rejection> getRejections() { return Collections.unmodifiableList(rejections); }
}
