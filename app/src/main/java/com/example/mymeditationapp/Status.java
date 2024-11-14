package com.example.mymeditationapp;

public enum Status {
    COMPLETE("complete"),
    INCOMPLETE("incomplete"),
    AWAITING("awaiting");

    private String statusName;

    private Status(String statusName){
        this.statusName=statusName;
    }

    public String toString(){
        return statusName;
    }
}
