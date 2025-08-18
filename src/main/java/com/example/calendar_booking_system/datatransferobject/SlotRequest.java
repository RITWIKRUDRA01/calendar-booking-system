package com.example.calendar_booking_system.datatransferobject;

public class SlotRequest {
    private String ownerId;
    private int year;
    private int month;
    private int day;

    public SlotRequest(String ownerId, int year, int month, int day) {
        this.ownerId = ownerId;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    // getters and setters
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
}