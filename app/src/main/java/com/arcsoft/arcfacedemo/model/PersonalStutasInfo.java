package com.arcsoft.arcfacedemo.model;

public class PersonalStutasInfo {
    private int finallyStatus;
    private int num;

    public PersonalStutasInfo(int finallyStatus, int num){
        this.finallyStatus = finallyStatus;
        this.num = num;
    }

    public int getFinallyStatus() {
        return finallyStatus;
    }

    public void setFinallyStatus(int finallyStatus) {
        this.finallyStatus = finallyStatus;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
