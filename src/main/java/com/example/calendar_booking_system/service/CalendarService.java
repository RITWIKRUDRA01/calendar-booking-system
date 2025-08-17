package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.Appointment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    public List<Integer> getFreeSlots(CalendarOwner owner, LocalDate queryDate) {
        Calendar calendar = owner.getCalendar();
        if (calendar == null) return Collections.emptyList();

        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(15);
        if (queryDate.isBefore(today) || queryDate.isAfter(cutoff)) return null; // too far
        if (owner.getOffDays().contains(queryDate.getDayOfWeek())) return Collections.emptyList();

        calendar.cleanupPastAppointments();

        int startHour = owner.getWorkDayStart().getHour();
        int endHour = owner.getWorkDayEnd().getHour();

        Set<Integer> taken;
        synchronized (calendar.getAppointments()) {
            taken = calendar.getAppointments().stream()
                    .filter(app -> app.getStartTime().toLocalDate().equals(queryDate))
                    .map(app -> app.getStartTime().getHour())
                    .collect(Collectors.toSet());
        }

        List<Integer> free = new ArrayList<>();
        for (int h = startHour; h < endHour; h++) {
            if (!taken.contains(h)) free.add(h);
        }
        return free;
    }
}