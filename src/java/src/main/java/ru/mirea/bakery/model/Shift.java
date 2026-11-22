package ru.mirea.bakery.model;

import java.time.LocalDateTime;

/** Рабочая смена планового периода. */
public class Shift {
    private final String id;
    private final LocalDateTime start;
    private final LocalDateTime end;

    public Shift(String id, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public boolean contains(LocalDateTime moment) {
        return !moment.isBefore(start) && moment.isBefore(end);
    }

    public String getId() { return id; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
}
