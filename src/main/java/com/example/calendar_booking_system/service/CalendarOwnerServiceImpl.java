package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.repository.GenericRepository;
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

    private final GenericRepository<CalendarOwner, String> repository;
    private final CalendarService calendarService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM");

    public CalendarOwnerServiceImpl(GenericRepository<CalendarOwner, String> calendarOwnerRepository,
                                    CalendarService calendarService) {
        this.repository = calendarOwnerRepository;
        this.calendarService = calendarService;
    }

    @Override
    public CalendarOwner createOwner(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        CalendarOwner owner = new CalendarOwner(name, email);
        repository.save(owner);
        return owner;
    }

    @Override
    public Set<CalendarOwner> getAllOwners() {
        return new HashSet<>(repository.findAll());
    }

    @Override
    public String getWorkDetails(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner ID is required");
        }

        CalendarOwner owner = repository.findById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Owner not found with id: " + ownerId);
        }

        return String.format(
                "The work details of owner with id = %s are as follows: " +
                        "Working hours: %s to %s, Off days: %s",
                ownerId,
                owner.getWorkDayStart() != null ? owner.getWorkDayStart() : "Not set",
                owner.getWorkDayEnd() != null ? owner.getWorkDayEnd() : "Not set",
                (owner.getOffDays() != null && !owner.getOffDays().isEmpty()) ? owner.getOffDays() : "None"
        );
    }

    @Override
    public String updateWorkDetails(Map<String, Object> request) {
        String ownerId = (String) request.get("id");
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner ID is required");
        }

        CalendarOwner owner = repository.findById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Owner not found with id: " + ownerId);
        }

        String startStr = (String) request.get("start");
        String endStr = (String) request.get("end");
        if (startStr == null || endStr == null) {
            throw new IllegalArgumentException("Both start and end times are required");
        }

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startStr);
            end = LocalTime.parse(endStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format. Use HH:mm (e.g., 09:00)");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
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
                        throw new IllegalArgumentException("Invalid day: " + dayStr + ". Use MONDAY, TUESDAY, etc.");
                    }
                }
            }
        }
        owner.setOffDays(offDays);

        return String.format(
                "The work details of owner with id = %s have been updated and are as follows: " +
                        "Working hours: %s to %s, Off days: %s",
                ownerId, start, end, offDays.isEmpty() ? "None" : offDays
        );
    }

    @Override
    public String getFullSummary(String ownerId) {
        CalendarOwner owner = repository.findById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Calendar owner not found for id: " + ownerId);
        }
        Calendar calendar = owner.getCalendar();
        if (calendar == null) {
            throw new RuntimeException("Calendar not found");
        }

        calendarService.cleanupPastAppointments(calendar);

        if (calendar.getAppointments().isEmpty()) {
            return "You have no upcoming appointments.";
        }

        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(15);

        return buildSummary(calendar, today, cutoff);
    }

    @Override
    public String getTodaySummary(String ownerId) {
        CalendarOwner owner = repository.findById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Calendar owner not found for id: " + ownerId);
        }
        Calendar calendar = owner.getCalendar();
        if (calendar == null) {
            throw new RuntimeException("Calendar not found");
        }

        calendarService.cleanupPastAppointments(calendar);

        LocalDate today = LocalDate.now();
        return buildSummary(calendar, today, today);
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