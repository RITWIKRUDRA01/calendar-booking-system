package com.example.calendar_booking_system.repository;

import com.example.calendar_booking_system.entity.CalendarOwner;
import java.util.List;
import java.util.ArrayList;

public class CalendarOwnerRepository {
    private static final List<CalendarOwner> owners = new ArrayList<>();

    public static List<CalendarOwner> findAll() {
        return owners;
    }

    public static void save(CalendarOwner owner) {
        owners.add(owner);
    }
}
