package ru.mirea.bakery.model;

/**
 * Компонент рецептуры: строка с расходом на 100 кг муки. Абстрактный класс;
 * подклассы по-разному добавляют свой расход в потребность в сырье.
 */
public abstract class RecipeComponent {
    private final int lineNo;
    private final double amountKg;

    protected RecipeComponent(int lineNo, double amountKg) {
        this.lineNo = lineNo;
        this.amountKg = amountKg;
    }

    /** Расход компонента, кг, на flourKg кг муки рецептуры. */
    protected double amountFor(double flourKg) {
        return amountKg * flourKg / 100;
    }

    /** Добавляет в need расход сырья, который даёт этот компонент. */
    public abstract void expand(double flourKg, MaterialRequirement need, int depth);

    public int getLineNo() { return lineNo; }
    public double getAmountKg() { return amountKg; }
}
