package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Batch;
import ru.mirea.bakery.model.Oven;
import ru.mirea.bakery.model.Shift;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Производственные мощности: печи и смены планового периода. */
public class BakeryFloor {
    /** Выпечка начинается не раньше чем за 7 ч до срока готовности. */
    public static final Duration MAX_LEAD = Duration.ofHours(7);

    public record Unscheduled(Batch batch, String reason) { }
    public record Schedule(List<Batch> scheduled, List<Unscheduled> unscheduled) { }

    /** Состояние печи во время распределения: когда освободится и какую группу пекла последней. */
    private static final class OvenLoad {
        private final Oven oven;
        private LocalDateTime freeAt;
        private String lastGroup;

        private OvenLoad(Oven oven, LocalDateTime freeAt) {
            this.oven = oven;
            this.freeAt = freeAt;
        }
    }

    private static final Comparator<OvenLoad> EARLIEST_FREE =
            Comparator.comparing((OvenLoad l) -> l.freeAt).thenComparing(l -> l.oven.getId());

    private static final Comparator<Batch> EDD = Comparator
            .comparing(Batch::getDeadline)
            .thenComparing(b -> -b.getProduct().getBakeMinutes())
            .thenComparing(b -> b.getProduct().getId())
            .thenComparingInt(Batch::getBatchNo);

    private final List<Oven> ovens;
    private final List<Shift> shifts;
    private final Duration maxLead;

    public BakeryFloor(List<Oven> ovens, List<Shift> shifts) {
        this(ovens, shifts, MAX_LEAD);
    }

    public BakeryFloor(List<Oven> ovens, List<Shift> shifts, Duration maxLead) {
        this.ovens = List.copyOf(ovens);
        this.shifts = shifts.stream().sorted(Comparator.comparing(Shift::getStart)).toList();
        this.maxLead = maxLead;
    }

    /**
     * Операция «Распределить партии по печам и сменам» (алгоритм 2). Партии берутся по
     * возрастанию срока готовности, каждая ставится на печь нужного типа, которая
     * освобождается раньше других (очередь с приоритетом по типам печей).
     */
    public Schedule schedule(List<Batch> batches) {
        List<Batch> order = new ArrayList<>(batches);
        order.sort(EDD);
        LocalDateTime horizonStart = shifts.getFirst().getStart();
        LocalDateTime horizonEnd = shifts.getLast().getEnd();

        Map<String, PriorityQueue<OvenLoad>> free = new HashMap<>();
        for (Oven oven : ovens) {
            free.computeIfAbsent(oven.getOvenType(), t -> new PriorityQueue<>(EARLIEST_FREE))
                    .add(new OvenLoad(oven, horizonStart));
        }

        List<Batch> scheduled = new ArrayList<>();
        List<Unscheduled> unscheduled = new ArrayList<>();
        for (Batch batch : order) {
            PriorityQueue<OvenLoad> queue = free.get(batch.getProduct().getOvenType());
            if (queue == null || queue.isEmpty()) {
                unscheduled.add(new Unscheduled(batch, "нет печи нужного типа"));
                continue;
            }
            OvenLoad load = queue.poll();
            LocalDateTime start = load.freeAt;
            String group = batch.getProduct().getProductGroup();
            if (load.lastGroup != null && !load.lastGroup.equals(group)) {
                start = start.plus(load.oven.getChangeover());
            }
            LocalDateTime earliest = batch.getDeadline().minus(maxLead);
            if (start.isBefore(earliest)) {
                start = earliest;
            }
            LocalDateTime end = start.plus(batch.bakeDuration());
            if (end.isAfter(horizonEnd)) {
                queue.add(load);
                unscheduled.add(new Unscheduled(batch, "не помещается в смены"));
                continue;
            }
            batch.assign(load.oven, findShift(start), start, end);
            scheduled.add(batch);
            load.lastGroup = group;
            load.freeAt = end;
            queue.add(load);
        }
        return new Schedule(scheduled, unscheduled);
    }

    /** Смена, в которую попадает момент времени (двоичный поиск по началам смен), или null. */
    private Shift findShift(LocalDateTime moment) {
        int lo = 0;
        int hi = shifts.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (!shifts.get(mid).getStart().isAfter(moment)) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        int i = lo - 1;
        return i >= 0 && shifts.get(i).contains(moment) ? shifts.get(i) : null;
    }

    public List<Oven> getOvens() { return ovens; }
    public List<Shift> getShifts() { return shifts; }
}
