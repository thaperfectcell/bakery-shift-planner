package ru.mirea.bakery.model;

/** Компонент-сырьё: расход добавляется в потребность, для муки - с пересчётом на влажность. */
public class MaterialComponent extends RecipeComponent {
    private final Material material;

    public MaterialComponent(int lineNo, double amountKg, Material material) {
        super(lineNo, amountKg);
        this.material = material;
    }

    @Override
    public void expand(double flourKg, MaterialRequirement need, int depth) {
        double amount = amountFor(flourKg);
        if (material.isFlour()) {
            amount *= need.flourCoefficient(material);
        }
        need.add(material, amount);
    }

    public Material getMaterial() { return material; }
}
