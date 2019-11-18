package com.arcsoft.arcfacedemo.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class attendanceInfo {
    private String id;
    private String firstTime;
    private String endTime;
    private attendanceInfo attendanceInfo;



    public attendanceInfo(String id, Date Time) {
        this.id = id;
        this.firstTime = new SimpleDateFormat("yy-mm-dd HH:mm:ss").format(Time);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(String firstTime) {
        this.firstTime = firstTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = new SimpleDateFormat("yy-mm-dd HH:mm:ss").format(endTime);
    }
}
