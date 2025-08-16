package com.example.calendar_booking_system.entity;

import java.util.UUID;

public class CalendarOwner {
    private final String id;     // auto-generated UUID
    private String name;
    private String email;
    private Calendar calendar;   // one-to-one relation

    public CalendarOwner(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.calendar = new Calendar(); // auto-generate calendar
    }

    // Getters only for id
    public String getId() {
        return id;
    }

    // Normal getters & setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
