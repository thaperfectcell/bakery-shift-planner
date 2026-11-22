package ru.mirea.bakery.io;

import ru.mirea.bakery.model.Batch;
import ru.mirea.bakery.model.Material;
import ru.mirea.bakery.model.ShiftTask;
import ru.mirea.bakery.planning.BakeryFloor;
import ru.mirea.bakery.planning.Demand;
import ru.mirea.bakery.planning.PlanPosition;
import ru.mirea.bakery.planning.PlanningResult;
import ru.mirea.bakery.planning.RequisitionLine;
import ru.mirea.bakery.planning.Shortage;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Печатает сводку, план, потребность, сменные задания и требование-накладную в виде таблиц. */
public class ConsoleReportWriter implements ResultWriter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final PrintStream out;

    public ConsoleReportWriter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void write(PlanningResult result) {
        printOrders(result.demand());
        if (!result.hasProduction()) {
            out.println("Нет принятых заявок на эту дату.");
            return;
        }
        printPlan(result.plan().getPositions());
        printRequirements(result);
        printSchedule(result.tasks(), result.unscheduled());
        printRequisition(result.requisition().getLines());
    }

    private void printOrders(Demand demand) {
        out.println("СВОДКА ЗАЯВОК НА " + demand.getShipDate().format(DATE));
        out.println("Принято заявок: " + demand.getAccepted().size());
        for (Demand.Duplicate d : demand.getDuplicates()) {
            out.println("Повтор: заявка " + d.orderId() + " совпадает с " + d.duplicateOf() + " - пропущена");
        }
        for (Demand.Rejection r : demand.getRejections()) {
            out.println("Отклонена: заявка " + r.orderId() + " - " + r.reason());
        }
    }

    private void printPlan(List<PlanPosition> positions) {
        List<List<String>> rows = new ArrayList<>();
        for (PlanPosition p : positions) {
            rows.add(List.of(p.product().getId(), p.product().getName(), String.valueOf(p.ordered()),
                    String.valueOf(p.planned()), String.valueOf(p.batchCount())));
        }
        printTable("ПЛАН ВЫПУСКА", List.of("Код", "Изделие", "Заказано, шт", "План, шт", "Партий"), rows);
    }

    private void printRequirements(PlanningResult result) {
        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<Material, Double> e : result.requirement().asMap().entrySet()) {
            Material m = e.getKey();
            rows.add(List.of(m.getId(), m.getName(), Formats.kg(e.getValue()),
                    Formats.kg(result.warehouse().available(m))));
        }
        printTable("ПОТРЕБНОСТЬ В СЫРЬЕ", List.of("Код", "Сырьё", "Нужно, кг", "Остаток, кг"), rows);
        if (result.shortages().isEmpty()) {
            out.println("Сырья достаточно.");
            return;
        }
        out.println("Сырья недостаточно - требуется решение начальника производства:");
        for (Shortage s : result.shortages()) {
            out.println("  " + s.material().getName() + ": не хватает " + Formats.kg(s.shortageKg()) + " кг");
        }
    }

    private void printSchedule(List<ShiftTask> tasks, List<BakeryFloor.Unscheduled> unscheduled) {
        long late = 0;
        for (ShiftTask task : tasks) {
            List<List<String>> rows = new ArrayList<>();
            for (Batch b : task.getBatches()) {
                rows.add(List.of(b.getId(), b.getOven().getName(), b.getProduct().getId(),
                        String.valueOf(b.getQuantity()), Formats.time(b.getStart()), Formats.time(b.getEnd()),
                        Formats.time(b.getDeadline()), b.isLate() ? "опоздание" : ""));
            }
            String title = "СМЕННОЕ ЗАДАНИЕ " + task.getId() + " (" + Formats.time(task.getShift().getStart())
                    + " - " + Formats.time(task.getShift().getEnd()) + "), статус: " + task.getStatus();
            printTable(title, List.of("Партия", "Печь", "Изделие", "Шт", "Начало", "Окончание", "Срок", "Отметка"),
                    rows);
            late += task.lateBatchCount();
        }
        out.println();
        out.println("Партий с опозданием: " + late);
        for (BakeryFloor.Unscheduled u : unscheduled) {
            out.println("Не распределена партия " + u.batch().getProduct().getId() + " № "
                    + u.batch().getBatchNo() + ": " + u.reason());
        }
    }

    private void printRequisition(List<RequisitionLine> lines) {
        List<List<String>> rows = new ArrayList<>();
        for (RequisitionLine line : lines) {
            Material m = line.material();
            rows.add(List.of(m.getId(), m.getName(), m.getUnit(), Formats.kg(line.quantity())));
        }
        printTable("ТРЕБОВАНИЕ-НАКЛАДНАЯ НА ОТПУСК СЫРЬЯ", List.of("Код", "Сырьё", "Ед.", "Количество"), rows);
    }

    /** Таблица с выравниванием столбцов по самому длинному значению. */
    private void printTable(String title, List<String> header, List<List<String>> rows) {
        int[] widths = new int[header.size()];
        for (int i = 0; i < header.size(); i++) {
            widths[i] = header.get(i).length();
            for (List<String> row : rows) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }
        out.println();
        out.println(title);
        out.println(line(header, widths));
        List<String> dashes = new ArrayList<>();
        for (int w : widths) {
            dashes.add("-".repeat(w));
        }
        out.println(String.join("  ", dashes));
        for (List<String> row : rows) {
            out.println(line(row, widths));
        }
    }

    private static String line(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append("  ");
            }
            sb.append(String.format("%-" + widths[i] + "s", cells.get(i)));
        }
        return sb.toString().stripTrailing();
    }
}
