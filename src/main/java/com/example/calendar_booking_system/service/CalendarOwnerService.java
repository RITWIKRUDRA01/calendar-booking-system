package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.CalendarOwner;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.Map;

public interface CalendarOwnerService {

    ResponseEntity<?> createOwner(String name, String email);

    ResponseEntity<?> getAllOwners();

    ResponseEntity<?> getWorkDetails(String ownerId);

    ResponseEntity<?> updateWorkDetails(Map<String, Object> request);

    ResponseEntity<?> getFullSummary(String ownerId);

    ResponseEntity<?> getTodaySummary(String ownerId);
}