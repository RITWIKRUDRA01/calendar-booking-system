package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        // Define boundaries
        LocalDateTime now = LocalDateTime.now();
        LocalDate todayStart = now.toLocalDate();
        LocalDateTime dayStart = todayStart.atStartOfDay();
        LocalDateTime limit = dayStart.plusDays(15).withHour(23).withMinute(59);

        // Rule 1: must be within today + 15 days
        if (start.isBefore(dayStart) || start.isAfter(limit)) {
            throw new IllegalArgumentException("Appointments must be within the next 15 days (including today).");
        }

        // Rule 2: if today, time must be in future
        if (start.toLocalDate().isEqual(todayStart) && start.isBefore(now)) {
            throw new IllegalArgumentException("Cannot schedule appointment in the past.");
        }

        // Rule 3: must be on the hour (no minutes/seconds)
        if (start.getMinute() != 0 || start.getSecond() != 0 || start.getNano() != 0) {
            throw new IllegalArgumentException("Appointments must start exactly on the hour (e.g., 10:00, 11:00).");
        }

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