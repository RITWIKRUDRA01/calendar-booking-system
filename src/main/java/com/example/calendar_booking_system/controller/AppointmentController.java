package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/schedule")
public class AppointmentController {

    // In-memory single Invitee and CalendarOwner for simplicity
    private final Invitee invitee;
    private final CalendarOwner owner;

    public AppointmentController() {
        this.owner = new CalendarOwner("Alice Owner", "alice@example.com");
        this.invitee = new Invitee("Bob Invitee", "bob@example.com");
    }

    // Endpoint to schedule an appointment
    @PostMapping("/appointment")
    public Appointment scheduleAppointment(@RequestParam String startTime,
                                           @RequestParam String subject) {
        LocalDateTime start = LocalDateTime.parse(startTime);

        Appointment appt = new Appointment(start, subject, invitee, owner);

        // Add appointment to invitee and owner's calendar
        invitee.addAppointment(appt);
        owner.getCalendar().addAppointment(appt);

        return appt;
    }

    // Endpoint to view invitee info
    @GetMapping("/invitee")
    public Invitee getInvitee() {
        return invitee;
    }

    // Endpoint to view calendar owner info
    @GetMapping("/owner")
    public CalendarOwner getOwner() {
        return owner;
    }
}