package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.datatransferobject.SlotRequest;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/invitees")
public class InviteeController {

    // For simplicity, store a single invitee in memory
    private Invitee invitee;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final CalendarOwnerRepository calendarOwnerRepository;
    private final CalendarService calendarService;


    public InviteeController(CalendarService calendarService,
                             CalendarOwnerRepository calendarOwnerRepository) {
        this.calendarService = calendarService;
        this.calendarOwnerRepository = calendarOwnerRepository;
    }


    // Create Invitee
    @PostMapping
    public ResponseEntity<?> createInvitee(@RequestBody Invitee requestInvitee) {
        if (requestInvitee.getName() == null || requestInvitee.getName().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Name must not be empty"));
        }
        if (requestInvitee.getEmail() == null || requestInvitee.getEmail().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email must not be empty"));
        }

        this.invitee = new Invitee(requestInvitee.getName(), requestInvitee.getEmail());
        return ResponseEntity.ok(this.invitee);
    }

    // Get invitee info
    @GetMapping
    public ResponseEntity<?> getInvitee() {
        if (invitee == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No invitee created yet"));
        }
        return ResponseEntity.ok(invitee);
    }

    // ---------------- Available Slots ----------------
    @PostMapping("/available-slots")
    public ResponseEntity<String> getAvailableSlots(@RequestBody SlotRequest req) {
        CalendarOwner owner = calendarOwnerRepository.findById(req.getOwnerId());
        if (owner == null) {
            throw new RuntimeException("CalendarOwner not found"); // handled by GlobalExceptionHandler
        }

        LocalDate queryDate;
        try {
            queryDate = LocalDate.of(req.getYear(), req.getMonth(), req.getDay());
        } catch (DateTimeException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date provided. Please provide a valid year, month, and day."); // test passes
        }
        // if invalid, DateTimeException will be thrown and caught by GlobalExceptionHandler

        List<Integer> freeSlots = calendarService.getFreeSlots(owner, queryDate);

        if (freeSlots == null) {
            return ResponseEntity.ok("Too far ahead. Please choose a date within the next 15 days.");
        }

        if (freeSlots.isEmpty()) {
            return ResponseEntity.ok(
                    owner.getOffDays().contains(queryDate.getDayOfWeek())
                            ? "It’s an off day. No appointment possible."
                            : "No free slots available on " + queryDate + "."
            );
        }

        return ResponseEntity.ok("On " + queryDate +
                " the available hour slots are: " +
                freeSlots.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}