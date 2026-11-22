package ru.mirea.bakery.model;

import java.time.Duration;

/** Хлебопекарная печь. */
public class Oven {
    private final String id;
    private final String name;
    private final String ovenType;
    private final Duration changeover;

    public Oven(String id, String name, String ovenType, int changeoverMin) {
        this.id = id;
        this.name = name;
        this.ovenType = ovenType;
        this.changeover = Duration.ofMinutes(changeoverMin);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOvenType() { return ovenType; }
    public Duration getChangeover() { return changeover; }
}
