package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.repository.GenericRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarServiceTest {

    private CalendarService calendarService;
    private CalendarOwner owner;
    private Invitee invitee;

    @BeforeEach
    void setUp() {
        // Use GenericRepository interface instead of concrete class
        GenericRepository<CalendarOwner, String> repo = new CalendarOwnerRepository();

        // Inject via interface constructor
        calendarService = new CalendarServiceImpl(repo);

        invitee = new Invitee("Bob", "bob@example.com");

        owner = new CalendarOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        owner.setOffDays(Collections.singleton(DayOfWeek.SATURDAY));
    }

    @Test
    void testFreeSlotsNormalDay() {
        LocalDate date = LocalDate.now().plusDays(1);
        Calendar calendar = owner.getCalendar();

        // Occupy 10 AM and 14 PM
        calendar.addAppointment(new Appointment(date.atTime(10, 0), "Meeting1", invitee, owner));
        calendar.addAppointment(new Appointment(date.atTime(14, 0), "Meeting2", invitee, owner));

        List<Integer> freeSlots = calendarService.getFreeSlots(owner, date);

        // Free slots should exclude 10 and 14
        assertTrue(freeSlots.contains(9));
        assertTrue(freeSlots.contains(11));
        assertTrue(freeSlots.contains(12));
        assertTrue(freeSlots.contains(13));
        assertTrue(freeSlots.contains(15));
        assertTrue(freeSlots.contains(16));
        assertFalse(freeSlots.contains(10));
        assertFalse(freeSlots.contains(14));
    }

    @Test
    void testFreeSlotsOnOffDay() {
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        List<Integer> freeSlots = calendarService.getFreeSlots(owner, nextSaturday);

        assertTrue(freeSlots.isEmpty(), "Free slots should be empty on an off day");
    }

    @Test
    void testFreeSlotsWithNoAppointments() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<Integer> freeSlots = calendarService.getFreeSlots(owner, date);

        // Should include all working hours
        assertEquals(8, freeSlots.size());
        for (int h = 9; h < 17; h++) {
            assertTrue(freeSlots.contains(h));
        }
    }

    @Test
    void testFreeSlotsDateTooFar() {
        LocalDate farFuture = LocalDate.now().plusDays(20);
        List<Integer> freeSlots = calendarService.getFreeSlots(owner, farFuture);

        assertNull(freeSlots, "Free slots should be null for dates beyond 15-day limit");
    }

    @Test
    void testFreeSlotsDateInPast() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        List<Integer> freeSlots = calendarService.getFreeSlots(owner, pastDate);

        assertNull(freeSlots, "Free slots should be null for dates in the past");
    }
}