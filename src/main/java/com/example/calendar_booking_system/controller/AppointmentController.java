package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/schedule")
public class AppointmentController {

    private final Invitee invitee;
    private final CalendarOwner owner;

    private final ReentrantLock lock = new ReentrantLock(); // For thread-safe booking

    @Autowired
    private CalendarService calendarService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AppointmentController(Invitee invitee, CalendarOwner owner, CalendarService calendarService) {
        this.invitee = invitee;
        this.owner = owner;
        this.calendarService = calendarService;
    }

    @PostMapping("/appointment")
    public Appointment scheduleAppointment(@RequestParam String startTime,
                                           @RequestParam String subject) {

        LocalDateTime appointmentTime = LocalDateTime.parse(startTime, ISO_FORMATTER);
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxLimit = today.plusDays(15).atTime(23, 59);

        // Basic validations
        if (appointmentTime.isBefore(now) || appointmentTime.isAfter(maxLimit)) {
            throw new IllegalArgumentException("Appointments must be within the next 15 days (including today).");
        }

        if (appointmentTime.getMinute() != 0 || appointmentTime.getSecond() != 0 || appointmentTime.getNano() != 0) {
            throw new IllegalArgumentException("Appointments must start exactly on the hour (e.g., 10:00).");
        }

        if (owner.getOffDays().contains(appointmentTime.getDayOfWeek())) {
            throw new IllegalArgumentException("Appointments not allowed on off days.");
        }

        LocalTime time = appointmentTime.toLocalTime();
        if (time.isBefore(owner.getWorkDayStart()) || time.isAfter(owner.getWorkDayEnd().minusHours(1))) {
            throw new IllegalArgumentException("Appointment outside working hours.");
        }

        Appointment appt = new Appointment(appointmentTime, subject, invitee, owner);

        // ---------------- Thread-safe booking ----------------
        lock.lock();
        try {
            // Check for slot availability directly within the lock
            boolean isSlotTaken = owner.getCalendar().getAppointments().stream()
                    .anyMatch(app -> app.getStartTime().toLocalDate().equals(appointmentTime.toLocalDate())
                            && app.getStartTime().getHour() == appointmentTime.getHour());
            if (isSlotTaken) {
                throw new IllegalArgumentException("Already occupied, try another slot.");
            }

            // Add appointment
            invitee.addAppointment(appt);
            owner.getCalendar().addAppointment(appt);

        } finally {
            lock.unlock();
        }

        return appt;
    }

    @GetMapping("/invitee")
    public Invitee getInvitee() {
        return invitee;
    }

    @GetMapping("/owner")
    public CalendarOwner getOwner() {
        return owner;
    }
}