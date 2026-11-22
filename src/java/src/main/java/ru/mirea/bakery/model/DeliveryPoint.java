package ru.mirea.bakery.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Точка доставки покупателя; определяет рейс и время его отправления. */
public class DeliveryPoint {
    private final String id;
    private final Customer customer;
    private final String address;
    private final LocalTime shipTime;

    public DeliveryPoint(String id, Customer customer, String address, LocalTime shipTime) {
        this.id = id;
        this.customer = customer;
        this.address = address;
        this.shipTime = shipTime;
    }

    /** Срок готовности продукции для этой точки: дата отгрузки и время отправления рейса. */
    public LocalDateTime deadlineFor(LocalDate shipDate) {
        return LocalDateTime.of(shipDate, shipTime);
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getAddress() { return address; }
    public LocalTime getShipTime() { return shipTime; }
}
