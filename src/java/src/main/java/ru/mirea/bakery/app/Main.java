package ru.mirea.bakery.app;

import ru.mirea.bakery.io.BakeryData;
import ru.mirea.bakery.io.ConsoleReportWriter;
import ru.mirea.bakery.io.CsvDataSource;
import ru.mirea.bakery.io.CsvResultWriter;
import ru.mirea.bakery.io.ResultWriter;
import ru.mirea.bakery.planning.BakeryFloor;
import ru.mirea.bakery.planning.OrderRegistry;
import ru.mirea.bakery.planning.PlanningResult;
import ru.mirea.bakery.planning.ShiftPlanningProcess;
import ru.mirea.bakery.planning.Warehouse;
import ru.mirea.bakery.rules.CutoffTimeRule;
import ru.mirea.bakery.rules.KnownDeliveryPointRule;
import ru.mirea.bakery.rules.NonEmptyOrderRule;
import ru.mirea.bakery.rules.OrderItemsRule;
import ru.mirea.bakery.rules.OrderRule;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Формирование сменного производственного задания по заказам покупателей (ООП-реализация, КТ3).
 * Запуск: java -jar bakery-shift-planner.jar [--data КАТАЛОГ] [--date ГГГГ-ММ-ДД] [--out КАТАЛОГ]
 */
public final class Main {
    private Main() { }

    public static void main(String[] args) throws IOException {
        System.exit(run(args));
    }

    static int run(String[] args) throws IOException {
        Path dataDir = Path.of("data");
        Path outDir = Path.of("out");
        LocalDate shipDate = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--data" -> dataDir = Path.of(args[++i]);
                case "--out" -> outDir = Path.of(args[++i]);
                case "--date" -> shipDate = LocalDate.parse(args[++i]);
                case "-h", "--help" -> {
                    System.out.println("Использование: java -jar bakery-shift-planner.jar "
                            + "[--data КАТАЛОГ] [--date ГГГГ-ММ-ДД] [--out КАТАЛОГ]");
                    return 0;
                }
                default -> throw new IllegalArgumentException("неизвестный параметр " + args[i]);
            }
        }

        BakeryData data = new CsvDataSource(dataDir).load();
        List<OrderRule> rules = List.of(new KnownDeliveryPointRule(), new NonEmptyOrderRule(),
                new OrderItemsRule(), new CutoffTimeRule(LocalTime.NOON));
        OrderRegistry registry = new OrderRegistry(data.orders(), rules);
        ShiftPlanningProcess process = new ShiftPlanningProcess(registry, new Warehouse(data.stock()),
                new BakeryFloor(data.ovens(), data.shifts()), data.recipes().size());

        LocalDate date = shipDate != null ? shipDate : registry.earliestShipDate().orElseThrow();
        PlanningResult result = process.run(date);

        List<ResultWriter> writers = List.of(new ConsoleReportWriter(System.out), new CsvResultWriter(outDir));
        for (ResultWriter writer : writers) {
            writer.write(result);
        }
        if (!result.hasProduction()) {
            return 1;
        }
        System.out.println();
        System.out.println("Результаты сохранены в каталог " + outDir);
        return 0;
    }
}
