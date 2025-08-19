package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM");

    private final CalendarService calendarService;
    private final CalendarOwnerRepository calendarOwnerRepository;

    public CalendarController(CalendarService calendarService, CalendarOwnerRepository calendarOwnerRepository) {
        this.calendarService = calendarService;
        this.calendarOwnerRepository=calendarOwnerRepository;
    }

    // Full summary for next 15 days
    @GetMapping("/{id}/appointments/summary")
    public ResponseEntity<?> getFullSummary(@PathVariable String id) {
        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Calendar owner not found for id: " + id); // handled by GlobalExceptionHandler
        }
        Calendar calendar = owner.getCalendar();
        if (calendar == null) {
            throw new RuntimeException("Calendar not found"); // handled by GlobalExceptionHandler
        }

        // cleanup expired appointments
        calendarService.cleanupPastAppointments(calendar);

        if (calendar.getAppointments().isEmpty()) {
            return ResponseEntity.ok("You have no upcoming appointments.");
        }

        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(15);

        String summary = buildSummary(calendar, today, cutoff);
        return ResponseEntity.ok(summary);
    }

    // Summary for today only
    @GetMapping("/{id}/appointments/today")
    public ResponseEntity<?> getTodaySummary(@PathVariable String id) {
        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Calendar owner not found for id: " + id); // handled by GlobalExceptionHandler
        }
        Calendar calendar = owner.getCalendar();
        if (calendar == null) {
            throw new RuntimeException("Calendar not found"); // handled by GlobalExceptionHandler
        }

        // cleanup expired appointments
        calendarService.cleanupPastAppointments(calendar);

        LocalDate today = LocalDate.now();
        String summary = buildSummary(calendar, today, today);
        return ResponseEntity.ok(summary);
    }


    // ----------------- helper -----------------
    private String buildSummary(Calendar calendar, LocalDate from, LocalDate to) {
        // Take a snapshot to safely iterate
        Set<Appointment> snapshot;
        synchronized (calendar.getAppointments()) {
            snapshot = new TreeSet<>(calendar.getAppointments());
        }

        // Group appointments by date within [from, to]
        Map<LocalDate, List<Appointment>> grouped = snapshot.stream()
                .filter(app -> {
                    LocalDate d = app.getStartTime().toLocalDate();
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .collect(Collectors.groupingBy(
                        app -> app.getStartTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        if (grouped.isEmpty()) {
            if (from.equals(to)) {
                return "You have no appointments today.";
            }
            return "You have no appointments in the given range.";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<LocalDate, List<Appointment>> entry : grouped.entrySet()) {
            LocalDate date = entry.getKey();
            List<Appointment> apps = entry.getValue();

            if (from.equals(to)) {
                sb.append("Today you have ").append(apps.size())
                        .append(" meeting").append(apps.size() > 1 ? "s" : "")
                        .append(" in the following order:\n");
            } else {
                sb.append("On ").append(date.format(DATE_FORMATTER))
                        .append(" you have ").append(apps.size())
                        .append(" meeting").append(apps.size() > 1 ? "s" : "")
                        .append(":\n");
            }

            int idx = 1;
            for (Appointment app : apps) {
                sb.append(idx++)
                        .append(". At ")
                        .append(app.getStartTime().format(TIME_FORMATTER))
                        .append(" an appointment with ")
                        .append(app.getInvitee().getName())
                        .append(" on the subject ")
                        .append(app.getSubject())
                        .append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
