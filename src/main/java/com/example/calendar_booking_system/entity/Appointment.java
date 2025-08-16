package com.example.calendar_booking_system.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment implements Comparable<Appointment>{
    private final String id;           // auto-generated UUID
    private final LocalDateTime startTime;
    private final String subject;      // new field for appointment subject
    private final Invitee invitee;     // who created the appointment
    private final CalendarOwner owner; // whose calendar is being booked

    public Appointment(LocalDateTime startTime, String subject, Invitee invitee, CalendarOwner owner) {
        this.id = UUID.randomUUID().toString();
        this.startTime = startTime;
        this.subject = subject;
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

    public String getSubject() {
        return subject;
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

    @Override
    public int compareTo(Appointment other) {
        return this.startTime.compareTo(other.startTime);
    }
}