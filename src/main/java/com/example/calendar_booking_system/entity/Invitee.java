package com.example.calendar_booking_system.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Invitee {
    private final String id;      // auto-generated UUID
    private String name;
    private String email;
    private List<Appointment> appointments;  // one-to-many relation

    public Invitee() {
        this.id = UUID.randomUUID().toString();
        this.name = "Bob Invitee";
        this.email = "bob@example.com";
        this.appointments = new ArrayList<>();
    }

    public Invitee(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.appointments = new ArrayList<>(); // start with empty list
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

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    // Convenience method to add an appointment
    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
    }
}