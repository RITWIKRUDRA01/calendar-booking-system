package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final CalendarOwnerRepository ownerRepository;

    public CalendarService(CalendarOwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public List<Integer> getFreeSlots(CalendarOwner owner, LocalDate queryDate) {
        Calendar calendar = owner.getCalendar();
        if (calendar == null) return Collections.emptyList();

        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(15);
        if (queryDate.isBefore(today) || queryDate.isAfter(cutoff)) return null; // too far
        if (owner.getOffDays().contains(queryDate.getDayOfWeek())) return Collections.emptyList();

        cleanupPastAppointments(calendar);

        LocalTime start = owner.getWorkDayStart(); // e.g., 09:30
        int startHour = owner.getWorkDayStart().getHour();
        int endHour = owner.getWorkDayEnd().getHour();

        if (start.getMinute() > 0) {
            startHour++;
        }

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

    public void cleanupPastAppointments(Calendar calendar) {
        LocalDateTime now = LocalDateTime.now();
        synchronized (calendar.getAppointments()) {
            calendar.getAppointments().removeIf(app -> !app.getEndTime().isAfter(now));
        }
    }
}