package ru.mirea.bakery.model;

/** Компонент-полуфабрикат: по выходу находится масса муки в нём, и его рецептура раскрывается рекурсивно. */
public class SemiProductComponent extends RecipeComponent {
    private final Recipe semiRecipe;

    public SemiProductComponent(int lineNo, double amountKg, Recipe semiRecipe) {
        super(lineNo, amountKg);
        this.semiRecipe = semiRecipe;
    }

    @Override
    public void expand(double flourKg, MaterialRequirement need, int depth) {
        double amount = amountFor(flourKg);
        double semiFlour = amount / semiRecipe.getYieldKg() * 100;
        semiRecipe.expand(semiFlour, need, depth + 1);
    }

    public Recipe getSemiRecipe() { return semiRecipe; }
}
