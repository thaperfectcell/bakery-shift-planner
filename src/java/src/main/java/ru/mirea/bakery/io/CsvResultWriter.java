package ru.mirea.bakery.io;

import ru.mirea.bakery.model.Batch;
import ru.mirea.bakery.model.Material;
import ru.mirea.bakery.model.ShiftTask;
import ru.mirea.bakery.planning.PlanPosition;
import ru.mirea.bakery.planning.PlanningResult;
import ru.mirea.bakery.planning.RequisitionLine;
import ru.mirea.bakery.planning.Shortage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Сохраняет результаты расчёта в CSV-файлы - те же файлы и столбцы, что в Python-версии. */
public class CsvResultWriter implements ResultWriter {
    private final Path outDir;

    public CsvResultWriter(Path outDir) {
        this.outDir = outDir;
    }

    @Override
    public void write(PlanningResult result) throws IOException {
        if (!result.hasProduction()) {
            return;
        }
        Files.createDirectories(outDir);

        List<LinkedHashMap<String, String>> plan = new ArrayList<>();
        for (PlanPosition p : result.plan().getPositions()) {
            plan.add(row("product_id", p.product().getId(), "name", p.product().getName(),
                    "ordered", String.valueOf(p.ordered()), "planned", String.valueOf(p.planned()),
                    "batch_count", String.valueOf(p.batchCount())));
        }
        CsvTable.write(outDir.resolve("plan.csv"),
                List.of("product_id", "name", "ordered", "planned", "batch_count"), plan);

        List<LinkedHashMap<String, String>> need = new ArrayList<>();
        for (Map.Entry<Material, Double> e : result.requirement().asMap().entrySet()) {
            need.add(row("material_id", e.getKey().getId(), "need_kg", Formats.rounded(e.getValue(), 2)));
        }
        CsvTable.write(outDir.resolve("material_requirements.csv"), List.of("material_id", "need_kg"), need);

        List<LinkedHashMap<String, String>> shortages = new ArrayList<>();
        for (Shortage s : result.shortages()) {
            shortages.add(row("material_id", s.material().getId(), "need_kg", Formats.rounded(s.needKg(), 2),
                    "available_kg", Formats.repr(s.availableKg()),
                    "shortage_kg", Formats.rounded(s.shortageKg(), 2)));
        }
        CsvTable.write(outDir.resolve("shortages.csv"),
                List.of("material_id", "need_kg", "available_kg", "shortage_kg"), shortages);

        List<LinkedHashMap<String, String>> tasks = new ArrayList<>();
        List<LinkedHashMap<String, String>> batches = new ArrayList<>();
        for (ShiftTask t : result.tasks()) {
            tasks.add(row("task_id", t.getId(), "shift_id", t.getShift().getId(), "status", t.getStatus()));
            for (Batch b : t.getBatches()) {
                batches.add(row("batch_id", b.getId(), "task_id", t.getId(), "product_id", b.getProduct().getId(),
                        "oven_id", b.getOven().getId(), "quantity", String.valueOf(b.getQuantity()),
                        "start", b.getStart().format(CsvDataSource.DATETIME),
                        "end", b.getEnd().format(CsvDataSource.DATETIME)));
            }
        }
        CsvTable.write(outDir.resolve("shift_tasks.csv"), List.of("task_id", "shift_id", "status"), tasks);
        CsvTable.write(outDir.resolve("batches.csv"),
                List.of("batch_id", "task_id", "product_id", "oven_id", "quantity", "start", "end"), batches);

        List<LinkedHashMap<String, String>> requisition = new ArrayList<>();
        for (RequisitionLine line : result.requisition().getLines()) {
            Material m = line.material();
            requisition.add(row("material_id", m.getId(), "name", m.getName(), "unit", m.getUnit(),
                    "quantity", Formats.repr(line.quantity())));
        }
        CsvTable.write(outDir.resolve("requisition.csv"), List.of("material_id", "name", "unit", "quantity"),
                requisition);
    }

    private static LinkedHashMap<String, String> row(String... keyValues) {
        LinkedHashMap<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            row.put(keyValues[i], keyValues[i + 1]);
        }
        return row;
    }
}
