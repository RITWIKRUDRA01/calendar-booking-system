package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.datatransferobject.AppointmentRequest;
import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/schedule")
public class AppointmentController {
    /*

    private final Invitee invitee;
    private final ReentrantLock lock = new ReentrantLock(); // For thread-safe booking

    private final CalendarService calendarService;
    private final CalendarOwnerRepository calendarOwnerRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    public AppointmentController(
            Invitee invitee,
            CalendarService calendarService,
            CalendarOwnerRepository calendarOwnerRepository) {
        this.invitee = invitee;
        this.calendarService = calendarService;
        this.calendarOwnerRepository = calendarOwnerRepository;
    }

    @PostMapping("/appointment")
    public ResponseEntity<Appointment> scheduleAppointment(@RequestBody AppointmentRequest request) {
        // ---------------- Lookup calendar owner ----------------
        CalendarOwner owner = calendarOwnerRepository.findById(request.getOwnerId());
        if (owner == null) {
            throw new RuntimeException("CalendarOwner not found for id: " + request.getOwnerId());
        }

        // ---------------- Build LocalDateTime from DTO ----------------
        LocalDateTime appointmentTime;
        try {
            appointmentTime = LocalDateTime.of(
                    request.getYear(),
                    request.getMonth(),
                    request.getDay(),
                    request.getHour(),
                    0, 0, 0
            );
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid date/time provided.", e);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxLimit = today.plusDays(15).atTime(23, 59);

        // ---------------- Basic validations ----------------
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

        // ---------------- Create appointment ----------------
        Appointment appt = new Appointment(appointmentTime, request.getSubject(), invitee, owner);

        // ---------------- Thread-safe booking ----------------
        lock.lock();
        try {
            boolean isSlotTaken = owner.getCalendar().getAppointments().stream()
                    .anyMatch(app -> app.getStartTime().toLocalDate().equals(appointmentTime.toLocalDate())
                            && app.getStartTime().getHour() == appointmentTime.getHour());
            if (isSlotTaken) {
                throw new IllegalArgumentException("Already occupied, try another slot.");
            }

            invitee.addAppointment(appt);
            owner.getCalendar().addAppointment(appt);

        } finally {
            lock.unlock();
        }

        // ---------------- Return appointment in ResponseEntity ----------------
        return ResponseEntity.ok(appt); // Changed to ResponseEntity for consistent API responses
    }

    @GetMapping("/invitee")
    public ResponseEntity<?> getInvitee() {
        if (invitee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No invitee created yet"));
        }
        return ResponseEntity.ok(invitee);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getOwner(@PathVariable String ownerId) {
        CalendarOwner owner = calendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "CalendarOwner not found for id: " + ownerId));
        }
        return ResponseEntity.ok(owner);
    }

     */
}