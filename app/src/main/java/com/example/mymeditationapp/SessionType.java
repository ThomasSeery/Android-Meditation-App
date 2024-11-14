package com.example.mymeditationapp;

public enum SessionType {
    MINDFULNESS("Mindfulness"),
    BREATHING_EXERCISE("Breathing Exercise");

    private String s;

    private SessionType(String s){
        this.s=s;
    }

    public String toString(){
        return s;
    }
}
