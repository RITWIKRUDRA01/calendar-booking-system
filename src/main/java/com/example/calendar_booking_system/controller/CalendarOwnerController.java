package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/owners")
public class CalendarOwnerController {

    private final CalendarOwnerRepository calendarOwnerRepository;

    public CalendarOwnerController(CalendarOwnerRepository calendarOwnerRepository) {
        this.calendarOwnerRepository = calendarOwnerRepository;
    }

    // Create a new CalendarOwner-post
    @PostMapping
    public CalendarOwner createOwner(@RequestParam String name, @RequestParam String email) {
        CalendarOwner owner = new CalendarOwner(name, email);
        calendarOwnerRepository.save(owner);
        return owner;
    }

    //  List all CalendarOwners
    @GetMapping
    public List<CalendarOwner> getAllOwners() {
        return calendarOwnerRepository.findAll();
    }

    // Get working hours and off days
    @GetMapping("/{id}/settings")
    public Map<String, Object> getSettings(@PathVariable String id) {
        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Owner not found");
        }

        Map<String, Object> settings = new HashMap<>();
        settings.put("workDayStart", owner.getWorkDayStart());
        settings.put("workDayEnd", owner.getWorkDayEnd());
        settings.put("offDays", owner.getOffDays());
        return settings;
    }

    @PostMapping("/{id}/settings/hours")
    public String updateHours(@PathVariable String id,
                              @RequestParam String start,
                              @RequestParam String end) {
        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Owner not found");
        }

        owner.setWorkHours(LocalTime.parse(start), LocalTime.parse(end));
        return "Working hours updated.";
    }

    @PostMapping("/{id}/settings/offdays")
    public String updateOffDays(@PathVariable String id, @RequestBody Set<DayOfWeek> offDays) {
        CalendarOwner owner = calendarOwnerRepository.findById(id);
        if (owner == null) {
            throw new RuntimeException("Owner not found");
        }

        owner.setOffDays(offDays);
        return "Off days updated.";
    }
}