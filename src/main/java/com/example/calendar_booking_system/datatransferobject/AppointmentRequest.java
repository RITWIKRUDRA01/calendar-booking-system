package com.example.calendar_booking_system.datatransferobject;

public class AppointmentRequest {
    private String ownerId;
    private String subject;
    private int day;
    private int month;
    private int year;
    private int hour;

    public AppointmentRequest() {}

    public AppointmentRequest(String ownerId, String subject, int day, int month, int year, int hour) {
        this.ownerId = ownerId;
        this.subject = subject;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
    }

    // Getters and Setters
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
}