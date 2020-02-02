package com.example.android.eventtimerfinish;

public class Division {
    private String name;
    private int fences;
    private int riders;

    Division(String name, int fences, int riders) {
        this.name = name;
        this.fences = fences;
        this.riders = riders;
    }

    public String getName () { return name; }

    public int getFences () { return fences; }

    public int getRiders () { return riders; }
}
