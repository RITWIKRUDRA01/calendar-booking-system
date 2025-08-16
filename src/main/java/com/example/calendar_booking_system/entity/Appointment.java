package com.example.calendar_booking_system.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private final String id;           // auto-generated UUID
    private final LocalDateTime startTime;
    private final Invitee invitee;     // who created the appointment
    private final CalendarOwner owner; // whose calendar is being booked

    public Appointment(LocalDateTime startTime, Invitee invitee, CalendarOwner owner) {
        this.id = UUID.randomUUID().toString();
        this.startTime = startTime;
        this.invitee = invitee;
        this.owner = owner;
    }

    // Getters
    public String getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    // Derived value: endTime = startTime + 1 hour
    public LocalDateTime getEndTime() {
        return startTime.plusHours(1);
    }

    public Invitee getInvitee() {
        return invitee;
    }

    public CalendarOwner getOwner() {
        return owner;
    }
}