package ru.mirea.bakery.model;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Потребность в сырье, которая накапливается при раскрытии рецептур (алгоритм 1).
 * Хранит также фактическую влажность муки и допустимую глубину вложенности рецептур.
 */
public class MaterialRequirement {
    public static final double BASE_FLOUR_MOISTURE = 14.5;

    private final SortedMap<Material, Double> need = new TreeMap<>(Material.BY_ID);
    private final Map<String, Double> flourMoisture;
    private final int maxDepth;

    public MaterialRequirement(Map<String, Double> flourMoisture, int maxDepth) {
        this.flourMoisture = Map.copyOf(flourMoisture);
        this.maxDepth = maxDepth;
    }

    /** Коэффициент пересчёта массы муки с базисной влажности на фактическую. */
    public double flourCoefficient(Material flour) {
        double actual = flourMoisture.getOrDefault(flour.getId(), BASE_FLOUR_MOISTURE);
        return (100 - BASE_FLOUR_MOISTURE) / (100 - actual);
    }

    public void add(Material material, double kg) {
        need.merge(material, kg, Double::sum);
    }

    public double get(Material material) {
        return need.getOrDefault(material, 0.0);
    }

    public int getMaxDepth() { return maxDepth; }
    public SortedMap<Material, Double> asMap() { return Collections.unmodifiableSortedMap(need); }
}
