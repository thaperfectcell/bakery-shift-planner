package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Batch;
import ru.mirea.bakery.model.MaterialRequirement;
import ru.mirea.bakery.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** План выпуска на дату отгрузки: позиции по изделиям и партии со сроками готовности. */
public class ProductionPlan {
    private final List<PlanPosition> positions = new ArrayList<>();
    private final List<Batch> batches = new ArrayList<>();

    private ProductionPlan() { }

    /** Операция «Скорректировать план выпуска»: округление до кратности загрузки печи и разбиение на партии. */
    public static ProductionPlan build(Demand demand) {
        ProductionPlan plan = new ProductionPlan();
        for (Map.Entry<Product, Integer> entry : demand.getQuantities().entrySet()) {
            Product product = entry.getKey();
            int ordered = entry.getValue();
            int count = product.batchCountFor(ordered);
            plan.positions.add(new PlanPosition(product, ordered, count * product.getBatchSize(), count));
            plan.batches.addAll(product.splitIntoBatches(demand.deliveriesOf(product), count));
        }
        return plan;
    }

    /**
     * Операция «Рассчитать потребность в сырье» (алгоритм 1): для каждой позиции плана
     * вычисляется масса муки, и рецептура изделия раскрывается рекурсивно.
     */
    public MaterialRequirement calcMaterialRequirements(Map<String, Double> flourMoisture, int recipeCount) {
        MaterialRequirement need = new MaterialRequirement(flourMoisture, recipeCount);
        for (PlanPosition position : positions) {
            Product product = position.product();
            double flourKg = product.flourKgFor(position.planned());
            product.getRecipe().expand(flourKg, need, 0);
        }
        return need;
    }

    public List<PlanPosition> getPositions() { return Collections.unmodifiableList(positions); }
    public List<Batch> getBatches() { return Collections.unmodifiableList(batches); }
}
