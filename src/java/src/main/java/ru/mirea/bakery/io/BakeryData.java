package ru.mirea.bakery.io;

import ru.mirea.bakery.model.Customer;
import ru.mirea.bakery.model.DeliveryPoint;
import ru.mirea.bakery.model.Material;
import ru.mirea.bakery.model.Order;
import ru.mirea.bakery.model.Oven;
import ru.mirea.bakery.model.Product;
import ru.mirea.bakery.model.Recipe;
import ru.mirea.bakery.model.Shift;
import ru.mirea.bakery.model.StockRecord;

import java.util.List;
import java.util.Map;

/** Исходные данные, загруженные из CSV: справочники по первичному ключу и списки. */
public record BakeryData(Map<String, Customer> customers, Map<String, DeliveryPoint> points,
                         List<Order> orders, Map<String, Product> products, Map<String, Recipe> recipes,
                         Map<String, Material> materials, List<StockRecord> stock,
                         List<Oven> ovens, List<Shift> shifts) { }
