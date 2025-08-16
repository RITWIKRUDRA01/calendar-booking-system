package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/invitees")
public class InviteeController {

    // For simplicity, store a single invitee in memory
    private Invitee invitee;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM");

    @Autowired
    private CalendarService calendarService;


    // Create Invitee
    @PostMapping
    public Invitee createInvitee(@RequestBody Invitee requestInvitee) {
        // store in memory
        this.invitee = new Invitee(requestInvitee.getName(), requestInvitee.getEmail());
        return this.invitee;
    }

    // Get invitee info
    @GetMapping
    public Invitee getInvitee() {
        if (invitee == null) {
            throw new RuntimeException("No invitee created yet");
        }
        return invitee;
    }

    // ---------------- Available Slots ----------------
    @GetMapping("/{ownerId}/available-slots/{date}")
    public String getAvailableSlots(
            @PathVariable String ownerId,
            @PathVariable String date) {

        CalendarOwner owner = CalendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            throw new RuntimeException("CalendarOwner not found");
        }

        LocalDate queryDate;
        try {
            queryDate = LocalDate.parse(date, DATE_FORMATTER); // expects dd-MM
        } catch (Exception e) {
            return "Invalid date format. Use dd-MM.";
        }

        List<Integer> freeSlots = calendarService.getFreeSlots(owner, queryDate);

        if (freeSlots == null) {
            return "Too far ahead. Please choose a date within the next 15 days.";
        }

        if (freeSlots.isEmpty()) {
            return owner.getOffDays().contains(queryDate.getDayOfWeek()) ?
                    "It’s an off day. No appointment possible." :
                    "No free slots available on " + queryDate.format(DATE_FORMATTER) + ".";
        }

        return "On " + queryDate.format(DATE_FORMATTER) +
                " the available hour slots are: " +
                freeSlots.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", "));
    }
}