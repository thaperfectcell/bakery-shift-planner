package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.MaterialRequirement;
import ru.mirea.bakery.model.ShiftTask;

import java.time.LocalDate;
import java.util.List;

/** Процесс «Формирование сменного производственного задания»: вызывает операции по порядку. */
public class ShiftPlanningProcess {
    private final OrderRegistry registry;
    private final Warehouse warehouse;
    private final BakeryFloor floor;
    private final int recipeCount;

    public ShiftPlanningProcess(OrderRegistry registry, Warehouse warehouse, BakeryFloor floor, int recipeCount) {
        this.registry = registry;
        this.warehouse = warehouse;
        this.floor = floor;
        this.recipeCount = recipeCount;
    }

    public PlanningResult run(LocalDate shipDate) {
        Demand demand = registry.consolidate(shipDate);
        if (demand.isEmpty()) {
            return new PlanningResult(demand, null, null, warehouse, List.of(), List.of(), List.of(), floor, null);
        }
        ProductionPlan plan = ProductionPlan.build(demand);
        MaterialRequirement need = plan.calcMaterialRequirements(warehouse.flourMoisture(), recipeCount);
        List<Shortage> shortages = warehouse.checkStock(need);
        BakeryFloor.Schedule schedule = floor.schedule(plan.getBatches());
        List<ShiftTask> tasks = ShiftTask.formFor(floor.getShifts(), schedule.scheduled());
        Requisition requisition = Requisition.from(need);
        return new PlanningResult(demand, plan, need, warehouse, shortages, tasks,
                schedule.unscheduled(), floor, requisition);
    }
}
