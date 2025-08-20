package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.GenericRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.example.calendar_booking_system.entity.Calendar;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarOwnerServiceImpl implements CalendarOwnerService {

    private final GenericRepository<CalendarOwner, String> calendarOwnerRepository;
    private final CalendarService calendarService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM");

    public CalendarOwnerServiceImpl(GenericRepository<CalendarOwner, String> calendarOwnerRepository,
                                    CalendarService calendarService) {
        this.calendarOwnerRepository = calendarOwnerRepository;
        this.calendarService = calendarService;
    }

    @Override
    public ResponseEntity<?> createOwner(String name, String email) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        CalendarOwner owner = new CalendarOwner(name, email);
        calendarOwnerRepository.save(owner);
        return ResponseEntity.ok(owner);
    }

    @Override
    public ResponseEntity<?> getAllOwners() {
        return ResponseEntity.ok(new HashSet<>(calendarOwnerRepository.findAll()));
    }

    @Override
    public ResponseEntity<?> getWorkDetails(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Owner ID is required"));
        }
        CalendarOwner owner = calendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Owner not found with id: " + ownerId));
        }
        return ResponseEntity.ok(String.format(
                "The work details of owner with id = %s are as follows: " +
                        "Working hours: %s to %s, Off days: %s",
                ownerId,
                owner.getWorkDayStart() != null ? owner.getWorkDayStart() : "Not set",
                owner.getWorkDayEnd() != null ? owner.getWorkDayEnd() : "Not set",
                (owner.getOffDays() != null && !owner.getOffDays().isEmpty()) ? owner.getOffDays() : "None"
        ));
    }

    @Override
    public ResponseEntity<?> updateWorkDetails(Map<String, Object> request) {
        String ownerId = (String) request.get("id");
        if (ownerId == null || ownerId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Owner ID is required"));
        }

        CalendarOwner owner = calendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Owner not found with id: " + ownerId));
        }

        String startStr = (String) request.get("start");
        String endStr = (String) request.get("end");
        if (startStr == null || endStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Both start and end times are required"));
        }

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startStr);
            end = LocalTime.parse(endStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid time format. Use HH:mm (e.g., 09:00)"));
        }

        if (!start.isBefore(end)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Start time must be before end time"));
        }
        owner.setWorkHours(start, end);

        // Off days
        Set<DayOfWeek> offDays = new HashSet<>();
        Object offDaysObj = request.get("offDays");
        if (offDaysObj instanceof List<?>) {
            for (Object dayObj : (List<?>) offDaysObj) {
                if (dayObj instanceof String dayStr) {
                    try {
                        offDays.add(DayOfWeek.valueOf(dayStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Invalid day: " + dayStr + ". Use MONDAY, TUESDAY, etc."));
                    }
                }
            }
        }
        owner.setOffDays(offDays);

        return ResponseEntity.ok(String.format(
                "The work details of owner with id = %s have been updated and are as follows: " +
                        "Working hours: %s to %s, Off days: %s",
                ownerId, start, end, offDays.isEmpty() ? "None" : offDays
        ));
    }

    @Override
    public ResponseEntity<?> getFullSummary(String ownerId) {
        CalendarOwner owner = calendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Calendar owner not found for id: " + ownerId));
        }
        Calendar calendar = owner.getCalendar();
        if (calendar == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Calendar not found for owner with id: " + ownerId));
        }

        calendarService.cleanupPastAppointments(calendar);

        if (calendar.getAppointments().isEmpty()) {
            return ResponseEntity.ok("You have no upcoming appointments.");
        }

        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(15);

        return ResponseEntity.ok(buildSummary(calendar, today, cutoff));
    }

    @Override
    public ResponseEntity<?> getTodaySummary(String ownerId) {
        CalendarOwner owner = calendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Calendar owner not found for id: " + ownerId));
        }
        Calendar calendar = owner.getCalendar();
        if (calendar == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Calendar not found for owner with id: " + ownerId));
        }

        calendarService.cleanupPastAppointments(calendar);

        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(buildSummary(calendar, today, today));
    }

    // ----------------- helper -----------------
    private String buildSummary(Calendar calendar, LocalDate from, LocalDate to) {
        Set<Appointment> snapshot;
        synchronized (calendar.getAppointments()) {
            snapshot = new TreeSet<>(calendar.getAppointments());
        }

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
            if (from.equals(to)) return "You have no appointments today.";
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