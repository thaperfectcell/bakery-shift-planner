package ru.mirea.bakery.io;

import ru.mirea.bakery.model.Customer;
import ru.mirea.bakery.model.DeliveryPoint;
import ru.mirea.bakery.model.Material;
import ru.mirea.bakery.model.MaterialComponent;
import ru.mirea.bakery.model.Order;
import ru.mirea.bakery.model.OrderItem;
import ru.mirea.bakery.model.Oven;
import ru.mirea.bakery.model.Product;
import ru.mirea.bakery.model.Recipe;
import ru.mirea.bakery.model.SemiProductComponent;
import ru.mirea.bakery.model.Shift;
import ru.mirea.bakery.model.StockRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Загружает сущности логической модели из каталога с CSV-файлами и связывает их ссылками. */
public class CsvDataSource {
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Path dir;

    public CsvDataSource(Path dir) {
        this.dir = dir;
    }

    public BakeryData load() throws IOException {
        Map<String, Customer> customers = new LinkedHashMap<>();
        for (var r : CsvTable.read(dir.resolve("customers.csv"))) {
            customers.put(r.get("customer_id"),
                    new Customer(r.get("customer_id"), r.get("name"), r.get("customer_type")));
        }
        Map<String, DeliveryPoint> points = new LinkedHashMap<>();
        for (var r : CsvTable.read(dir.resolve("delivery_points.csv"))) {
            points.put(r.get("point_id"), new DeliveryPoint(r.get("point_id"), customers.get(r.get("customer_id")),
                    r.get("address"), LocalTime.parse(r.get("ship_time"))));
        }
        Map<String, Material> materials = new LinkedHashMap<>();
        for (var r : CsvTable.read(dir.resolve("materials.csv"))) {
            materials.put(r.get("material_id"), new Material(r.get("material_id"), r.get("name"),
                    r.get("unit"), "1".equals(r.get("is_flour"))));
        }
        Map<String, Recipe> recipes = loadRecipes(materials);
        Map<String, Product> products = new LinkedHashMap<>();
        for (var r : CsvTable.read(dir.resolve("products.csv"))) {
            products.put(r.get("product_id"), new Product(r.get("product_id"), r.get("name"),
                    Double.parseDouble(r.get("piece_mass_kg")), recipes.get(r.get("recipe_id")),
                    r.get("oven_type"), r.get("product_group"),
                    Integer.parseInt(r.get("batch_size")), Integer.parseInt(r.get("bake_minutes"))));
        }
        List<Order> orders = loadOrders(points, products);
        List<StockRecord> stock = new ArrayList<>();
        for (var r : CsvTable.read(dir.resolve("stock.csv"))) {
            String moisture = r.get("moisture_pct");
            stock.add(new StockRecord(materials.get(r.get("material_id")), LocalDate.parse(r.get("stock_date")),
                    Double.parseDouble(r.get("quantity_kg")),
                    moisture.isEmpty() ? null : Double.parseDouble(moisture)));
        }
        List<Oven> ovens = new ArrayList<>();
        for (var r : CsvTable.read(dir.resolve("ovens.csv"))) {
            ovens.add(new Oven(r.get("oven_id"), r.get("name"), r.get("oven_type"),
                    Integer.parseInt(r.get("changeover_min"))));
        }
        List<Shift> shifts = new ArrayList<>();
        for (var r : CsvTable.read(dir.resolve("shifts.csv"))) {
            shifts.add(new Shift(r.get("shift_id"), LocalDateTime.parse(r.get("start"), DATETIME),
                    LocalDateTime.parse(r.get("end"), DATETIME)));
        }
        return new BakeryData(customers, points, orders, products, recipes, materials, stock, ovens, shifts);
    }

    /** Рецептуры загружаются в два прохода: сначала сами рецептуры, затем их компоненты. */
    private Map<String, Recipe> loadRecipes(Map<String, Material> materials) throws IOException {
        Map<String, Recipe> recipes = new LinkedHashMap<>();
        for (var r : CsvTable.read(dir.resolve("recipes.csv"))) {
            recipes.put(r.get("recipe_id"), new Recipe(r.get("recipe_id"), r.get("name"), r.get("kind"),
                    Double.parseDouble(r.get("yield_kg"))));
        }
        for (var r : CsvTable.read(dir.resolve("recipe_components.csv"))) {
            Recipe recipe = recipes.get(r.get("recipe_id"));
            int lineNo = Integer.parseInt(r.get("line_no"));
            double amount = Double.parseDouble(r.get("amount_kg"));
            String semi = r.get("semi_recipe_id");
            if (semi.isEmpty()) {
                recipe.addComponent(new MaterialComponent(lineNo, amount, materials.get(r.get("material_id"))));
            } else {
                recipe.addComponent(new SemiProductComponent(lineNo, amount, recipes.get(semi)));
            }
        }
        return recipes;
    }

    private List<Order> loadOrders(Map<String, DeliveryPoint> points, Map<String, Product> products)
            throws IOException {
        Map<String, Order> orders = new LinkedHashMap<>();
        for (var r : CsvTable.read(dir.resolve("orders.csv"))) {
            String pointId = r.get("point_id");
            orders.put(r.get("order_id"), new Order(r.get("order_id"), pointId, points.get(pointId),
                    LocalDate.parse(r.get("ship_date")), r.get("channel"),
                    LocalDateTime.parse(r.get("received_at"), DATETIME)));
        }
        for (var r : CsvTable.read(dir.resolve("order_items.csv"))) {
            Order order = orders.get(r.get("order_id"));
            if (order != null) {
                String productId = r.get("product_id");
                order.addItem(new OrderItem(productId, products.get(productId), Integer.parseInt(r.get("quantity"))));
            }
        }
        return new ArrayList<>(orders.values());
    }
}
