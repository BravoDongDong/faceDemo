package com.arcsoft.arcfacedemo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class attendanceInfo {
    private String id;
    private List<Date> Time = new ArrayList<>();
    private int status;
    private static final int normal = 1;
    private static final int late = 2;
    private static final int LeaveEarly = 3;
    private static final int Truancy = 4;


    public attendanceInfo(String id, Date Time) {
        this.id = id;
        this.Time.add(Time);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Date> getTime() {
        return Time;
    }

    public void addTime(Date time) {
        for ( int i = 1; i < Time.size(); i++) {
            Time.remove(Time.get(i));
        }

        Time.add(time);
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
