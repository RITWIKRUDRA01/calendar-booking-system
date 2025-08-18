package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.datatransferobject.SlotRequest;
import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        // occupy some slots
        owner.getCalendar().addAppointment(new Appointment(date.atTime(9, 0), "Meeting1", controller.getInvitee(), owner));
        owner.getCalendar().addAppointment(new Appointment(date.atTime(13, 0), "Meeting2", controller.getInvitee(), owner));

        SlotRequest req = new SlotRequest(owner.getId(),
                date.getYear(), date.getMonthValue(), date.getDayOfMonth());

        ResponseEntity<String> response = controller.getAvailableSlots(req);
        String result = response.getBody();
        // Extract only the slot part after colon
        String slotsPart = result.split(":")[1].trim(); // "10, 11, 12, 14, 15, 16"
        List<String> slots = Arrays.asList(slotsPart.split(", "));

        // Assert present slots
        assertTrue(slots.contains("10"));
        assertTrue(slots.contains("11"));
        assertTrue(slots.contains("12"));
        assertTrue(slots.contains("14"));

        // Assert absent slots
        assertFalse(slots.contains("9"));
        assertFalse(slots.contains("13"));
    }


    @Test
    void testAvailableSlotsOffDay() {
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

        SlotRequest req = new SlotRequest(
                owner.getId(),
                nextSaturday.getYear(),
                nextSaturday.getMonthValue(),
                nextSaturday.getDayOfMonth()
        );

        ResponseEntity<String> response = controller.getAvailableSlots(req);
        String result = response.getBody();
        assertEquals("It’s an off day. No appointment possible.", result);
    }

    @Test
    void testAvailableSlotsTooFarAhead() {
        LocalDate farDate = LocalDate.now().plusDays(16);

        SlotRequest req = new SlotRequest(
                owner.getId(),
                farDate.getYear(),
                farDate.getMonthValue(),
                farDate.getDayOfMonth()
        );

        ResponseEntity<String> response = controller.getAvailableSlots(req);
        String result = response.getBody();
        assertEquals("Too far ahead. Please choose a date within the next 15 days.", result);
    }

    @Test
    void testAvailableSlotsInvalidDateFormat() {
        // Simulate impossible date: month 13, day 40
        SlotRequest req = new SlotRequest(owner.getId(), 2025, 13, 40);

        ResponseEntity<String> response = controller.getAvailableSlots(req);
        String result = response.getBody();
        assertEquals("Invalid date provided. Please provide a valid year, month, and day.", result);
    }

    @Test
    void testAvailableSlotsNoFreeHours() {
        LocalDate date = LocalDate.now().plusDays(2);

        // Occupy all slots
        for (int h = 9; h < 17; h++) {
            owner.getCalendar().addAppointment(new Appointment(date.atTime(h, 0), "Full", controller.getInvitee(), owner));
        }

        SlotRequest req = new SlotRequest(
                owner.getId(),
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        ResponseEntity<String> response = controller.getAvailableSlots(req);
        String result = response.getBody();
        assertEquals("No free slots available on " +
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".", result);
    }
}