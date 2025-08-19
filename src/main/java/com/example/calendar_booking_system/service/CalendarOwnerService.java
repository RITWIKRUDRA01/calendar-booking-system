package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.CalendarOwner;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.Map;

public interface CalendarOwnerService {

    CalendarOwner createOwner(String name, String email);

    Set<CalendarOwner> getAllOwners();

    String getWorkDetails(String ownerId);

    String updateWorkDetails(Map<String, Object> request);

    String getFullSummary(String ownerId);

    String getTodaySummary(String ownerId);
}