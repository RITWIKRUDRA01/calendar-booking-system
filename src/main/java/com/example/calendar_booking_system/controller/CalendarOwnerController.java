package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owners")
public class CalendarOwnerController {

    // Create a new CalendarOwner
    @PostMapping
    public CalendarOwner createOwner(@RequestParam String name, @RequestParam String email) {
        CalendarOwner owner = new CalendarOwner(name, email);
        CalendarOwnerRepository.save(owner);
        return owner;
    }

    //  List all CalendarOwners
    @GetMapping
    public List<CalendarOwner> getAllOwners() {
        return CalendarOwnerRepository.findAll();
    }
}