package ru.mirea.bakery.model;

import java.time.LocalDate;
import java.util.Optional;

/** Остаток сырья на складе на дату; для муки - фактическая влажность. */
public class StockRecord {
    private final Material material;
    private final LocalDate date;
    private final double quantityKg;
    private final Double moisturePct;   // null, если влажность не измеряется

    public StockRecord(Material material, LocalDate date, double quantityKg, Double moisturePct) {
        this.material = material;
        this.date = date;
        this.quantityKg = quantityKg;
        this.moisturePct = moisturePct;
    }

    public boolean isNewerThan(StockRecord other) {
        return other == null || date.isAfter(other.date);
    }

    public Material getMaterial() { return material; }
    public LocalDate getDate() { return date; }
    public double getQuantityKg() { return quantityKg; }
    public Optional<Double> getMoisturePct() { return Optional.ofNullable(moisturePct); }
}
