package ru.mirea.bakery.planning;

import org.junit.jupiter.api.Test;
import ru.mirea.bakery.model.Batch;
import ru.mirea.bakery.model.Oven;
import ru.mirea.bakery.model.Product;
import ru.mirea.bakery.model.Recipe;
import ru.mirea.bakery.model.Shift;
import ru.mirea.bakery.model.ShiftTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Распределение партий, когда между сменами есть перерыв (замечание рецензента #2). */
class BakeryFloorTest {
    private static final LocalDateTime DAY = LocalDateTime.of(2026, 11, 5, 0, 0);
    private final Recipe recipe = new Recipe("R11", "Хлеб", "изделие", 150);
    private final Product bread = new Product("P01", "Хлеб", 0.5, recipe, "туннельная", "хлеб", 100, 30);
    private final Oven oven = new Oven("O1", "Печь 1", "туннельная", 0);
    private final List<Shift> shifts = List.of(
            new Shift("S1", DAY.withHour(9), DAY.withHour(10)),
            new Shift("S2", DAY.withHour(12), DAY.withHour(13)));
    private final BakeryFloor floor = new BakeryFloor(List.of(oven), shifts, Duration.ofHours(7));

    @Test
    void batchStartingInBreakMovesToNextShift() {
        Batch batch = new Batch(bread, 1, 100, DAY.withHour(17).withMinute(30));   // раньше 10:30 нельзя

        BakeryFloor.Schedule schedule = floor.schedule(List.of(batch));

        assertEquals(1, schedule.scheduled().size());
        assertEquals(0, schedule.unscheduled().size());
        assertEquals("S2", batch.getShift().getId());
        assertEquals(DAY.withHour(12), batch.getStart());
        assertEquals(DAY.withHour(12).withMinute(30), batch.getEnd());
    }

    @Test
    void batchAfterLastShiftIsUnscheduled() {
        Batch batch = new Batch(bread, 1, 100, DAY.withHour(21));                  // раньше 14:00 нельзя

        BakeryFloor.Schedule schedule = floor.schedule(List.of(batch));

        assertEquals(0, schedule.scheduled().size());
        assertEquals("не помещается в смены", schedule.unscheduled().getFirst().reason());
    }

    @Test
    void everyScheduledBatchIsInExactlyOneShiftTask() {
        List<Batch> batches = List.of(
                new Batch(bread, 1, 100, DAY.withHour(10)),
                new Batch(bread, 2, 100, DAY.withHour(17).withMinute(30)),
                new Batch(bread, 3, 100, DAY.withHour(17).withMinute(30)),
                new Batch(bread, 4, 100, DAY.withHour(21)));

        BakeryFloor.Schedule schedule = floor.schedule(batches);
        List<ShiftTask> tasks = ShiftTask.formFor(shifts, schedule.scheduled());

        int inTasks = tasks.stream().mapToInt(t -> t.getBatches().size()).sum();
        assertEquals(schedule.scheduled().size(), inTasks);
        assertEquals(batches.size(), schedule.scheduled().size() + schedule.unscheduled().size());
        for (Batch b : schedule.scheduled()) {
            assertNotNull(b.getShift());
            assertTrue(b.getShift().contains(b.getStart()));
        }
    }
}
