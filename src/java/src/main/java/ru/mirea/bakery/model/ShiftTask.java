package ru.mirea.bakery.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Сменное задание: партии одной смены. Партии - части задания (композиция). */
public class ShiftTask {
    private final String id;
    private final Shift shift;
    private final String status;
    private final List<Batch> batches = new ArrayList<>();

    private ShiftTask(Shift shift, String status) {
        this.id = "СЗ-" + shift.getId();
        this.shift = shift;
        this.status = status;
    }

    /** Формирует задания по сменам: одно задание на смену, в которую попала хотя бы одна партия. */
    public static List<ShiftTask> formFor(List<Shift> shifts, List<Batch> scheduled) {
        List<ShiftTask> tasks = new ArrayList<>();
        for (Shift shift : shifts) {
            List<Batch> inShift = scheduled.stream()
                    .filter(b -> b.getShift() == shift)
                    .sorted(Comparator.comparing(Batch::getStart)
                            .thenComparing(b -> b.getOven().getId()))
                    .toList();
            if (inShift.isEmpty()) {
                continue;
            }
            ShiftTask task = new ShiftTask(shift, "проект");
            int number = 1;
            for (Batch batch : inShift) {
                batch.numberInTask(task.id, number++);
                task.batches.add(batch);
            }
            tasks.add(task);
        }
        return tasks;
    }

    public long lateBatchCount() {
        return batches.stream().filter(Batch::isLate).count();
    }

    public String getId() { return id; }
    public Shift getShift() { return shift; }
    public String getStatus() { return status; }
    public List<Batch> getBatches() { return Collections.unmodifiableList(batches); }
}
