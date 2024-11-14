package com.example.mymeditationapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MeditationSession {

    private int sessionId;
    private SessionType type;
    private Date dueDate;
    private int duration;
    private Status status;

    public MeditationSession(int sessionId, String type, Date dueDate, int duration, String status) {
        this.sessionId=sessionId;
        this.type= SessionType.valueOf(type.toUpperCase().replace(" ", "_"));
        this.dueDate = dueDate;
        this.duration = duration;
        this.status = Status.valueOf(status.toUpperCase());
    }

    public MeditationSession(SessionType type, Date dueDate, int duration, Status status) {
        this.type = type;
        this.dueDate = dueDate;
        this.duration = duration;
        this.status = status;
    }

    public MeditationSession(String type, Date dueDate, String status){
        this.type= SessionType.valueOf(type.toUpperCase().replace(" ", "_"));
        this.dueDate=dueDate;
        this.status = Status.valueOf(status.toUpperCase());
    }

    public MeditationSession(Date dueDate, String status){
        this.dueDate=dueDate;
        this.status = Status.valueOf(status.toUpperCase());
    }

    public MeditationSession(String type, Date dueDate, int duration, String status){
        this.type= SessionType.valueOf(type.toUpperCase().replace(" ", "_"));
        this.dueDate=dueDate;
        this.duration=duration;
        this.status = Status.valueOf(status.toUpperCase());
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getDueDateToString(){
        return new SimpleDateFormat("yyyy-MM-dd").format(dueDate);
    }

    public String getDueDateAndTimeToString(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(dueDate);
    }

    public static String dueDateToString(Date date){
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static Date dueDateToDate(String date){
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTimeFromDate(){
        return new SimpleDateFormat("HH:mm").format(dueDate);
    }

    public int getSessionId() {
        return sessionId;
    }


    public String getType() {
        return type.toString();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getStatus() {
        return status.toString();
    }


    @Override
    public String toString() {
        return "MeditationSession{" +
                "sessionId=" + sessionId +
                ", type=" + type +
                ", duration=" + duration +
                ", status=" + status +
                ", dueDate=" + dueDateToString(dueDate) +
                "}\n";
    }

}
