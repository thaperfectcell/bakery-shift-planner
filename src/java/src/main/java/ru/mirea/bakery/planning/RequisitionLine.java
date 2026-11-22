package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Material;

import java.math.BigDecimal;

/** Строка требования-накладной: сырьё и количество, округлённое до 0,1. */
public record RequisitionLine(Material material, BigDecimal quantity) { }
