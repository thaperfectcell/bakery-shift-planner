package ru.mirea.bakery.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Рецептура изделия или полуфабриката: расход компонентов на 100 кг муки и выход. */
public class Recipe {
    private final String id;
    private final String name;
    private final String kind;
    private final double yieldKg;
    private final List<RecipeComponent> components = new ArrayList<>();

    public Recipe(String id, String name, String kind, double yieldKg) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.yieldKg = yieldKg;
    }

    public void addComponent(RecipeComponent component) {
        components.add(component);
        components.sort((a, b) -> Integer.compare(a.getLineNo(), b.getLineNo()));
    }

    /**
     * Алгоритм 1 (рекурсивная часть): раскрывает рецептуру на flourKg кг муки
     * и добавляет расход сырья в need. Каждый компонент сам решает, как себя раскрыть.
     */
    public void expand(double flourKg, MaterialRequirement need, int depth) {
        if (depth > need.getMaxDepth()) {
            throw new IllegalStateException("циклическая ссылка в рецептуре " + id);
        }
        for (RecipeComponent component : components) {
            component.expand(flourKg, need, depth);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getKind() { return kind; }
    public double getYieldKg() { return yieldKg; }
    public List<RecipeComponent> getComponents() { return Collections.unmodifiableList(components); }
}
