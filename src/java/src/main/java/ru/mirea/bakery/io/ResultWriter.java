package ru.mirea.bakery.io;

import ru.mirea.bakery.planning.PlanningResult;

import java.io.IOException;

/** Способ выдачи результатов расчёта: в консоль, в файлы и т. п. */
public interface ResultWriter {
    void write(PlanningResult result) throws IOException;
}
