package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Material;
import ru.mirea.bakery.model.MaterialRequirement;
import ru.mirea.bakery.model.StockRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Склад сырья: последние по дате остатки каждого вида сырья. */
public class Warehouse {
    private final Map<String, StockRecord> latest = new HashMap<>();

    public Warehouse(List<StockRecord> records) {
        for (StockRecord record : records) {
            String id = record.getMaterial().getId();
            if (record.isNewerThan(latest.get(id))) {
                latest.put(id, record);
            }
        }
    }

    public double available(Material material) {
        StockRecord record = latest.get(material.getId());
        return record == null ? 0.0 : record.getQuantityKg();
    }

    /** Фактическая влажность муки по данным лабораторного контроля. */
    public Map<String, Double> flourMoisture() {
        Map<String, Double> moisture = new HashMap<>();
        for (StockRecord record : latest.values()) {
            if (record.getMaterial().isFlour()) {
                record.getMoisturePct().ifPresent(m -> moisture.put(record.getMaterial().getId(), m));
            }
        }
        return moisture;
    }

    /** Операция «Сверить потребность с остатками склада». */
    public List<Shortage> checkStock(MaterialRequirement need) {
        List<Shortage> shortages = new ArrayList<>();
        for (Map.Entry<Material, Double> entry : need.asMap().entrySet()) {
            double available = available(entry.getKey());
            if (entry.getValue() > available) {
                shortages.add(new Shortage(entry.getKey(), entry.getValue(), available));
            }
        }
        return shortages;
    }
}
