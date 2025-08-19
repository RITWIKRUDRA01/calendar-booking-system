package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/owners")
public class CalendarOwnerController {

    private final CalendarOwnerRepository calendarOwnerRepository;

    public CalendarOwnerController(CalendarOwnerRepository calendarOwnerRepository) {
        this.calendarOwnerRepository = calendarOwnerRepository;
    }

    // Create a new CalendarOwner-post
    @PostMapping
    public ResponseEntity<CalendarOwner> createOwner(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        CalendarOwner owner = new CalendarOwner(name, email);
        calendarOwnerRepository.save(owner);

        return ResponseEntity.ok(owner);
    }

    //  List all CalendarOwners
    @GetMapping
    public List<CalendarOwner> getAllOwners() {
        return calendarOwnerRepository.findAll();
    }

    // Get working hours and off days
    @GetMapping("/settings/work-details/{id}")
    public ResponseEntity<String> getWorkDetails(@PathVariable String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Owner ID is required");
        }

        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Owner not found with id: " + id);
        }

        // Build response string
        String responseMessage = String.format(
                "The work details of owner with id = %s are as follows: " +
                        "Working hours: %s to %s, Off days: %s",
                id,
                owner.getWorkDayStart() != null ? owner.getWorkDayStart() : "Not set",
                owner.getWorkDayEnd() != null ? owner.getWorkDayEnd() : "Not set",
                (owner.getOffDays() != null && !owner.getOffDays().isEmpty())
                        ? owner.getOffDays()
                        : "None"
        );

        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping("/settings/work-details")
    public ResponseEntity<String> updateWorkDetails(@RequestBody Map<String, Object> request) {
        // Validate and extract ID
        String id = (String) request.get("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Owner ID is required");
        }

        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Owner not found with id: " + id);
        }

        // Validate and extract start/end times
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

        // Validate and extract off days
        Object offDaysObj = request.get("offDays");
        Set<DayOfWeek> offDays = new HashSet<>();
        if (offDaysObj instanceof List<?>) {
            for (Object dayObj : (List<?>) offDaysObj) {
                if (dayObj instanceof String dayStr) {
                    try {
                        offDays.add(DayOfWeek.valueOf(dayStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid day: " + dayStr + ". Use values like MONDAY, TUESDAY...");
                    }
                }
            }
        }
        owner.setOffDays(offDays);

        // Build response string
        String responseMessage = String.format(
                "The work details of owner with id = %s have been updated and are as follows: " +
                        "Working hours: %s to %s, Off days: %s",
                id, start, end, offDays.isEmpty() ? "None" : offDays
        );

        return ResponseEntity.ok(responseMessage);
    }
}