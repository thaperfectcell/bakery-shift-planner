package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Product;

/** Позиция плана выпуска: заказано, запланировано с учётом кратности партий, число партий. */
public record PlanPosition(Product product, int ordered, int planned, int batchCount) { }
