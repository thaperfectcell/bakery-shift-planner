package ru.mirea.bakery.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Хлебобулочное изделие и параметры его выпечки. */
public class Product {
    private final String id;
    private final String name;
    private final double pieceMassKg;
    private final Recipe recipe;
    private final String ovenType;
    private final String productGroup;
    private final int batchSize;
    private final int bakeMinutes;

    public Product(String id, String name, double pieceMassKg, Recipe recipe, String ovenType,
                   String productGroup, int batchSize, int bakeMinutes) {
        this.id = id;
        this.name = name;
        this.pieceMassKg = pieceMassKg;
        this.recipe = recipe;
        this.ovenType = ovenType;
        this.productGroup = productGroup;
        this.batchSize = batchSize;
        this.bakeMinutes = bakeMinutes;
    }

    /** Число партий, до которого округляется потребность (кратность загрузки печи). */
    public int batchCountFor(int quantity) {
        return (quantity + batchSize - 1) / batchSize;
    }

    /** Масса муки, кг, на заданное число штук изделия: F = Q * m / B * 100. */
    public double flourKgFor(int pieces) {
        double productKg = pieces * pieceMassKg;
        return productKg / recipe.getYieldKg() * 100;
    }

    /**
     * Разбивает выпуск на партии. Поставки упорядочиваются по сроку, партии закрывают их
     * по очереди: партия k начинается с изделия номер k * batchSize + 1 и должна быть готова
     * к сроку той поставки, в которую это изделие попадает.
     */
    public List<Batch> splitIntoBatches(List<Delivery> deliveries, int batchCount) {
        List<Delivery> ordered = new ArrayList<>(deliveries);
        ordered.sort(Delivery.ORDER);
        List<Batch> batches = new ArrayList<>();
        int index = 0;
        int covered = ordered.get(0).quantity();
        for (int k = 0; k < batchCount; k++) {
            int firstUnit = k * batchSize + 1;
            while (covered < firstUnit && index < ordered.size() - 1) {
                index++;
                covered += ordered.get(index).quantity();
            }
            LocalDateTime deadline = ordered.get(index).deadline();
            batches.add(new Batch(this, k + 1, batchSize, deadline));
        }
        return batches;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPieceMassKg() { return pieceMassKg; }
    public Recipe getRecipe() { return recipe; }
    public String getOvenType() { return ovenType; }
    public String getProductGroup() { return productGroup; }
    public int getBatchSize() { return batchSize; }
    public int getBakeMinutes() { return bakeMinutes; }
}
