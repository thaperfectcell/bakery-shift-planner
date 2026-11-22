package ru.mirea.bakery.model;

/** Позиция заявки: количество одного изделия в заявке. */
public class OrderItem {
    private final String productId;
    private final Product product;   // null, если изделие не найдено в справочнике
    private final int quantity;

    public OrderItem(String productId, Product product, int quantity) {
        this.productId = productId;
        this.product = product;
        this.quantity = quantity;
    }

    public boolean hasKnownProduct() { return product != null; }

    public String getProductId() { return productId; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}
