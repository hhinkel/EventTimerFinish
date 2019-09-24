package com.example.android.eventtimerfinish;

public final class Rider {
    private int riderNumber;
    private String division;
    private int fenceNumber;
    private long startTime;
    private long finishTime;

    Rider(int riderNumber, String division, int fenceNumber, long startTime, long finishTime) {
        this.riderNumber = riderNumber;
        this.division = division;
        this.fenceNumber = fenceNumber;
        this.startTime = startTime;
        this.finishTime = finishTime;
    }

    public int getRiderNumber (){
        return riderNumber;
    }

    public String getDivision () { return division; }

    public int getFenceNumber () { return fenceNumber; }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }
}

