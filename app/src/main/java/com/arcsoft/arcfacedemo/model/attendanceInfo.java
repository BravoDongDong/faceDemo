package com.arcsoft.arcfacedemo.model;

import java.util.Date;

public class attendanceInfo {
    private String id;
    private Date firstTime;
    private Date secondTime;
    private int status;
    private static final int normal = 1;
    private static final int late = 2;
    private static final int LeaveEarly = 3;
    private static final int Truancy = 4;


    public attendanceInfo(String id, Date firstTime) {
        this.id = id;
        this.firstTime = firstTime;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(Date firstTime) {
        this.firstTime = firstTime;
    }

    public Date getSecondTime() {
        return secondTime;
    }

    public void setSecondTime(Date secondTime) {
        this.secondTime = secondTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
