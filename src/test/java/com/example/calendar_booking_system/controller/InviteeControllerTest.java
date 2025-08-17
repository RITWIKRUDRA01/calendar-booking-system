package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class InviteeControllerTest {

    private InviteeController controller;
    private CalendarService calendarService;
    private CalendarOwner owner;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @BeforeEach
    void setUp() {
        // Use real CalendarService
        calendarService = new CalendarService();

        // Initialize controller
        controller = new InviteeController(calendarService);

        // Setup a CalendarOwner
        owner = new CalendarOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        owner.setOffDays(Collections.singleton(DayOfWeek.SATURDAY));
        owner.setCalendar(new Calendar());

        CalendarOwnerRepository.save(owner);

        // Reset the invitee for each test
        controller.createInvitee(new Invitee("Bob", "bob@example.com"));
    }

    @Test
    void testCreateAndGetInvitee() {
        Invitee invitee = controller.getInvitee();
        assertNotNull(invitee);
        assertEquals("Bob", invitee.getName());
        assertEquals("bob@example.com", invitee.getEmail());
    }

    @Test
    void testAvailableSlotsNormalDay() {
        LocalDate date = LocalDate.now().plusDays(1);

        // Add some appointments to occupy slots
        owner.getCalendar().addAppointment(new Appointment(date.atTime(9, 0), "Meeting1", controller.getInvitee(), owner));
        owner.getCalendar().addAppointment(new Appointment(date.atTime(13, 0), "Meeting2", controller.getInvitee(), owner));

        String result = controller.getAvailableSlots(owner.getId(), date.format(DATE_FORMATTER));

        // Free slots should be all except 9 and 13
        assertTrue(result.contains("10"));
        assertTrue(result.contains("11"));
        assertTrue(result.contains("12"));
        assertTrue(result.contains("14"));
        assertFalse(result.contains("9"));
        assertFalse(result.contains("13"));
    }

    @Test
    void testAvailableSlotsOffDay() {
        // Pick the next Saturday
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        String dateStr = nextSaturday.format(DATE_FORMATTER);

        String result = controller.getAvailableSlots(owner.getId(), dateStr);
        assertEquals("It’s an off day. No appointment possible.", result);
    }

    @Test
    void testAvailableSlotsTooFarAhead() {
        LocalDate farDate = LocalDate.now().plusDays(16);
        String dateStr = farDate.format(DATE_FORMATTER);

        String result = controller.getAvailableSlots(owner.getId(), dateStr);
        assertEquals("Too far ahead. Please choose a date within the next 15 days.", result);
    }

    @Test
    void testAvailableSlotsInvalidDateFormat() {
        String invalidDate = "2025-08-17"; // wrong format dd-MM
        String result = controller.getAvailableSlots(owner.getId(), invalidDate);
        assertEquals("Invalid date format. Use dd-MM.", result);
    }

    @Test
    void testAvailableSlotsNoFreeHours() {
        LocalDate date = LocalDate.now().plusDays(2);

        // Occupy all slots
        for (int h = 9; h < 17; h++) {
            owner.getCalendar().addAppointment(new Appointment(date.atTime(h, 0), "Full", controller.getInvitee(), owner));
        }

        String result = controller.getAvailableSlots(owner.getId(), date.format(DATE_FORMATTER));
        assertEquals("No free slots available on " + date.format(DATE_FORMATTER) + ".", result);
    }
}