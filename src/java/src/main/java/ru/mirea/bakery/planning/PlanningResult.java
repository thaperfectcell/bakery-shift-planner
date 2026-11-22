package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.MaterialRequirement;
import ru.mirea.bakery.model.ShiftTask;

import java.util.List;

/** Все результаты расчёта сменного задания - то, что выводят ResultWriter. */
public record PlanningResult(Demand demand, ProductionPlan plan, MaterialRequirement requirement,
                             Warehouse warehouse, List<Shortage> shortages, List<ShiftTask> tasks,
                             List<BakeryFloor.Unscheduled> unscheduled, BakeryFloor floor,
                             Requisition requisition) {

    public boolean hasProduction() {
        return plan != null;
    }
}
