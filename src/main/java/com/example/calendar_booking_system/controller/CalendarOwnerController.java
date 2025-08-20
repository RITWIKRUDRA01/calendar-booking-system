package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.service.CalendarOwnerService;
import com.example.calendar_booking_system.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/owners")
public class CalendarOwnerController {

    private final CalendarOwnerService calendarOwnerService;
    private final CalendarService calendarService;

    public CalendarOwnerController(CalendarOwnerService calendarOwnerService,CalendarService calendarService) {
        this.calendarOwnerService = calendarOwnerService;
        this.calendarService=calendarService;
    }

    @PostMapping
    public ResponseEntity<?> createOwner(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        return calendarOwnerService.createOwner(name, email); // just return service's ResponseEntity
    }

    @GetMapping
    public ResponseEntity<?> getAllOwners() {
        return calendarOwnerService.getAllOwners();
    }

    @GetMapping("/settings/work-details/{id}")
    public ResponseEntity<?> getWorkDetails(@PathVariable String id) {
        return calendarOwnerService.getWorkDetails(id);
    }

    @PostMapping("/settings/work-details")
    public ResponseEntity<?> updateWorkDetails(@RequestBody Map<String, Object> request) {
        return calendarOwnerService.updateWorkDetails(request);
    }

    @GetMapping("/{id}/appointments/summary")
    public ResponseEntity<?> getFullSummary(@PathVariable String id) {
        return calendarOwnerService.getFullSummary(id);
    }

    @GetMapping("/{id}/appointments/today")
    public ResponseEntity<?> getTodaySummary(@PathVariable String id) {
        return calendarOwnerService.getTodaySummary(id);
    }
}