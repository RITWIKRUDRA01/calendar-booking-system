package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/schedule")
public class AppointmentController {

    private final Invitee invitee;
    private final CalendarOwner owner;

    // Constructor injection of dependencies
    public AppointmentController(Invitee invitee, CalendarOwner owner) {
        this.invitee = invitee;
        this.owner = owner;
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

    // Endpoint to get Invitee info
    @GetMapping("/invitee")
    public Invitee getInvitee() {
        return invitee;
    }

    // Endpoint to get CalendarOwner info
    @GetMapping("/owner")
    public CalendarOwner getOwner() {
        return owner;
    }
}