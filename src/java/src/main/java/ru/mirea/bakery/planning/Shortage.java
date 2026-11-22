package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Material;

/** Нехватка сырья на складе. */
public record Shortage(Material material, double needKg, double availableKg) {
    public double shortageKg() {
        return needKg - availableKg;
    }
}
