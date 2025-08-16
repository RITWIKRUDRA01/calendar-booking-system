package com.example.calendar_booking_system.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.TreeSet;

public class Calendar {
    private final String id;
    private final Set<Appointment> appointments;

    public Calendar() {
        this.id = UUID.randomUUID().toString();
        this.appointments = new TreeSet<>();
    }

    public String getId() {
        return id;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
    }

    public void cleanupPastAppointments() {
        LocalDateTime now = LocalDateTime.now();
        appointments.removeIf(app -> !app.getEndTime().isAfter(now));
    }
}