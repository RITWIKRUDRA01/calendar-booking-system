package com.example.calendar_booking_system.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Calendar {
    private final String id;  // auto-generated, immutable
    private List<Appointment> appointments;

    public Calendar() {
        this.id = UUID.randomUUID().toString();
        this.appointments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
    }
}
