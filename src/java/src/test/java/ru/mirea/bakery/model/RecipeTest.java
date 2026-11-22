package ru.mirea.bakery.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeTest {
    private final Material flour = new Material("M01", "Мука", "кг", true);
    private final Material salt = new Material("M05", "Соль", "кг", false);

    @Test
    void semiProductIsExpandedRecursively() {
        Recipe sponge = new Recipe("R01", "Опара", "полуфабрикат", 200);
        sponge.addComponent(new MaterialComponent(1, 100, flour));
        Recipe bread = new Recipe("R11", "Хлеб", "изделие", 150);
        bread.addComponent(new SemiProductComponent(1, 100, sponge));   // 100 кг опары на 100 кг муки
        bread.addComponent(new MaterialComponent(2, 2, salt));

        MaterialRequirement need = new MaterialRequirement(Map.of(), 2);
        bread.expand(100, need, 0);

        assertEquals(50.0, need.get(flour), 1e-9);   // в 100 кг опары 50 кг муки
        assertEquals(2.0, need.get(salt), 1e-9);
    }

    @Test
    void flourIsRecalculatedToActualMoisture() {
        Recipe bread = new Recipe("R12", "Хлеб", "изделие", 140);
        bread.addComponent(new MaterialComponent(1, 100, flour));
        MaterialRequirement need = new MaterialRequirement(Map.of("M01", 15.5), 1);
        bread.expand(100, need, 0);
        assertEquals(100 * (100 - 14.5) / (100 - 15.5), need.get(flour), 1e-9);
    }

    @Test
    void cyclicRecipeIsRejected() {
        Recipe a = new Recipe("A", "A", "полуфабрикат", 150);
        Recipe b = new Recipe("B", "B", "полуфабрикат", 150);
        a.addComponent(new SemiProductComponent(1, 50, b));
        b.addComponent(new SemiProductComponent(1, 50, a));
        MaterialRequirement need = new MaterialRequirement(Map.of(), 2);
        assertThrows(IllegalStateException.class, () -> a.expand(100, need, 0));
    }
}
