package ru.mirea.bakery.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Партия: объём одного изделия за одну загрузку печи. Создаётся при корректировке плана
 * (изделие, номер, количество, срок), затем получает печь, смену и время выпечки.
 * Количество и окончание - плановые значения на момент составления задания.
 */
public class Batch {
    private final Product product;
    private final int batchNo;
    private final int quantity;
    private final LocalDateTime deadline;

    private Oven oven;
    private Shift shift;
    private LocalDateTime start;
    private LocalDateTime end;
    private String id;

    public Batch(Product product, int batchNo, int quantity, LocalDateTime deadline) {
        this.product = product;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.deadline = deadline;
    }

    public Duration bakeDuration() {
        return Duration.ofMinutes(product.getBakeMinutes());
    }

    /** Закрепляет партию за печью и сменой. */
    public void assign(Oven oven, Shift shift, LocalDateTime start, LocalDateTime end) {
        this.oven = oven;
        this.shift = shift;
        this.start = start;
        this.end = end;
    }

    /** Присваивает номер партии внутри сменного задания. */
    public void numberInTask(String taskId, int number) {
        this.id = String.format("%s-%02d", taskId, number);
    }

    public boolean isLate() {
        return end != null && end.isAfter(deadline);
    }

    public Product getProduct() { return product; }
    public int getBatchNo() { return batchNo; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDeadline() { return deadline; }
    public Oven getOven() { return oven; }
    public Shift getShift() { return shift; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getId() { return id; }
}
