package ru.mirea.bakery.planning;

import ru.mirea.bakery.model.Material;
import ru.mirea.bakery.model.MaterialRequirement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Требование-накладная на отпуск сырья в производство. */
public class Requisition {
    private final List<RequisitionLine> lines = new ArrayList<>();

    private Requisition() { }

    /** Операция «Сформировать ... требование-накладную»: строки по потребности в сырье. */
    public static Requisition from(MaterialRequirement need) {
        Requisition requisition = new Requisition();
        for (Map.Entry<Material, Double> entry : need.asMap().entrySet()) {
            BigDecimal quantity = new BigDecimal(entry.getValue()).setScale(1, RoundingMode.HALF_EVEN);
            requisition.lines.add(new RequisitionLine(entry.getKey(), quantity));
        }
        return requisition;
    }

    public List<RequisitionLine> getLines() { return Collections.unmodifiableList(lines); }
}
