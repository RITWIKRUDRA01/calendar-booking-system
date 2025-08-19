package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.Calendar;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarControllerTest {

    private CalendarController controller;
    private CalendarOwner owner;
    private Invitee invitee;

    private CalendarOwnerRepository ownerRepository;

    @BeforeEach
    void setup() {
        ownerRepository = new CalendarOwnerRepository();
        CalendarService service = new CalendarService(ownerRepository);
        controller = new CalendarController(service,ownerRepository);

        // Create a calendar owner
        owner = new CalendarOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        owner.setOffDays(new HashSet<>());

        // Add owner to repository
        ownerRepository.save(owner);

        // Create an invitee
        invitee = new Invitee("Bob", "bob@example.com");

        // Add a few appointments
        owner.getCalendar().addAppointment(
                new Appointment(LocalDateTime.now().plusHours(1), "Meeting 1", invitee, owner));
        owner.getCalendar().addAppointment(
                new Appointment(LocalDateTime.now().plusDays(1).withHour(10), "Meeting 2", invitee, owner));
    }

    @Test
    void testGetTodaySummary_WithAppointments() {
        ResponseEntity<?> response = controller.getTodaySummary(owner.getId()); // changed
        String result = (String) response.getBody(); // unwrap the body
        assertTrue(result.contains("Today you have"));
        assertTrue(result.contains("Meeting 1"));
    }

    @Test
    void testGetTodaySummary_NoAppointments() {
        // Create owner with empty calendar
        CalendarOwner emptyOwner = new CalendarOwner("Charlie", "charlie@example.com");
        ownerRepository.save(emptyOwner);

        ResponseEntity<?> response = controller.getTodaySummary(emptyOwner.getId()); // changed
        String result = (String) response.getBody(); // unwrap the body
        assertEquals("You have no appointments today.", result); // updated message as per controller
    }

    @Test
    void testGetFullSummary_UpcomingAppointments() {
        ResponseEntity<?> response = controller.getFullSummary(owner.getId()); // changed
        String result = (String) response.getBody(); // unwrap the body
        assertTrue(result.contains("On"));
        assertTrue(result.contains("Meeting 1") || result.contains("Meeting 2"));
    }

    @Test
    void testGetFullSummary_NoCalendar() {
        CalendarOwner noCalOwner = new CalendarOwner("Dave", "dave@example.com");
        noCalOwner.setCalendar(null);
        ownerRepository.save(noCalOwner);

        Exception ex = assertThrows(RuntimeException.class, () ->
                controller.getFullSummary(noCalOwner.getId()));
        assertEquals("Calendar not found", ex.getMessage());
    }

    @Test
    void testGetFullSummary_InvalidId() {
        Exception ex = assertThrows(RuntimeException.class, () ->
                controller.getFullSummary("invalid-id"));
        assertTrue(ex.getMessage().contains("Calendar owner not found"));
    }
}