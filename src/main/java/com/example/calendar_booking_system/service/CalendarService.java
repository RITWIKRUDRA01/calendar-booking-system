package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Calendar;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {

    /**
     * Returns the list of free hour slots for a given owner and date.
     * Returns null if the date is too far ahead.
     */
    List<Integer> getFreeSlots(CalendarOwner owner, LocalDate queryDate);

    /**
     * Removes past appointments from a calendar.
     */
    void cleanupPastAppointments(Calendar calendar);
}