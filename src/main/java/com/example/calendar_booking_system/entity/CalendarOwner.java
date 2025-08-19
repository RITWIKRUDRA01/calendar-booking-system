package com.example.calendar_booking_system.entity;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.UUID;
import java.time.DayOfWeek;
import java.util.Set;

public class CalendarOwner {
    private final String id;     // auto-generated UUID
    private String name;
    private String email;
    private Calendar calendar;   // one-to-one relation
    private LocalTime workDayStart;
    private LocalTime workDayEnd;
    private Set<DayOfWeek> offDays;

    public CalendarOwner() {
        this.id = UUID.randomUUID().toString();
        this.name = "Bob";
        this.email = "example@com";
        this.calendar = new Calendar(); // auto-generate calendar
        this.workDayStart = LocalTime.of(9, 0); // default 9 AM
        this.workDayEnd = LocalTime.of(17, 0);  // default 5 PM
        this.offDays = new HashSet<>();
    }
    public CalendarOwner(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.calendar = new Calendar(); // auto-generate calendar
        this.workDayStart = LocalTime.of(9, 0); // default 9 AM
        this.workDayEnd = LocalTime.of(17, 0);  // default 5 PM
        this.offDays = new HashSet<>();
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

    public LocalTime getWorkDayStart() { return workDayStart; }

    public void setWorkHours(LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.workDayStart = start;
        this.workDayEnd = end;
    }

    public LocalTime getWorkDayEnd() { return workDayEnd; }

    public Set<DayOfWeek> getOffDays() { return offDays; }

    public void setOffDays(Set<DayOfWeek> offDays) {
        this.offDays = offDays;
    }

    public void addOffDay(DayOfWeek day) {
        this.offDays.add(day);
    }

    public void removeOffDay(DayOfWeek day) {
        this.offDays.remove(day);
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

