package ru.mirea.bakery.model;

import java.util.Comparator;

/** Вид сырья, учитываемый на складе. */
public class Material {
    public static final Comparator<Material> BY_ID = Comparator.comparing(Material::getId);

    private final String id;
    private final String name;
    private final String unit;
    private final boolean flour;

    public Material(String id, String name, String unit, boolean flour) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.flour = flour;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public boolean isFlour() { return flour; }
}
