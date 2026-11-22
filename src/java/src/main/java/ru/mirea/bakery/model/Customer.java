package ru.mirea.bakery.model;

/** Покупатель: торговая сеть, магазин или учреждение, которое заказывает продукцию. */
public class Customer {
    private final String id;
    private final String name;
    private final String type;

    public Customer(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
}
