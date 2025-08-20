package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarOwnerService;
import com.example.calendar_booking_system.service.CalendarOwnerServiceImpl;
import com.example.calendar_booking_system.service.CalendarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CalendarOwnerControllerTest {

    private CalendarOwnerController controller;
    private CalendarOwnerService calendarOwnerService;
    private CalendarOwnerRepository repository;
    private CalendarServiceImpl calendarServiceImpl;

    @BeforeEach
    void setUp() {
        repository = new CalendarOwnerRepository();
        calendarServiceImpl = new CalendarServiceImpl(repository);
        calendarOwnerService = new CalendarOwnerServiceImpl(repository, calendarServiceImpl);
        controller = new CalendarOwnerController(calendarOwnerService, calendarServiceImpl);
    }

    // --- Owner CRUD tests ---

    @Test
    void testCreateOwner() {
        Map<String, String> request = Map.of("name", "Alice", "email", "alice@example.com");
        ResponseEntity<?> response = controller.createOwner(request);

        assertEquals(200, response.getStatusCodeValue());
        CalendarOwner owner = (CalendarOwner) response.getBody();
        assertNotNull(owner.getId());
        assertEquals("Alice", owner.getName());
        assertEquals("alice@example.com", owner.getEmail());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testCreateOwner_MissingName() {
        Map<String, String> request = Map.of("email", "alice@example.com");
        ResponseEntity<?> response = controller.createOwner(request);

        assertEquals(400, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Name is required", body.get("error"));
    }

    @Test
    void testGetAllOwners() {
        // Create two owners via controller
        controller.createOwner(Map.of("name", "Alice", "email", "alice@example.com"));
        controller.createOwner(Map.of("name", "Bob", "email", "bob@example.com"));

        // Get all owners via controller
        ResponseEntity<?> response = controller.getAllOwners();
        Set<CalendarOwner> owners = (Set<CalendarOwner>) response.getBody();

        assertEquals(2, owners.size());

        Set<String> names = owners.stream().map(CalendarOwner::getName).collect(Collectors.toSet());
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Bob"));
    }

    @Test
    void testGetWorkDetails() {
        CalendarOwner owner = (CalendarOwner) controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
        assertNotNull(owner);

        owner.setWorkHours(LocalTime.of(9,0), LocalTime.of(17,0));
        owner.setOffDays(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        ResponseEntity<?> response = controller.getWorkDetails(owner.getId());
        String body = (String) response.getBody();

        assertNotNull(body);
        assertTrue(body.contains("09:00"));
        assertTrue(body.contains("17:00"));
        assertTrue(body.contains("SATURDAY"));
        assertTrue(body.contains("SUNDAY"));
    }

    @Test
    void testGetWorkDetails_OwnerNotFound() {
        ResponseEntity<?> response = controller.getWorkDetails("invalid-id");
        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Owner not found with id: invalid-id", body.get("error"));
    }

    @Test
    void testUpdateWorkDetails() {
        CalendarOwner owner = (CalendarOwner) controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
        assertNotNull(owner);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", owner.getId());
        updateRequest.put("start", "08:30");
        updateRequest.put("end", "16:30");
        updateRequest.put("offDays", List.of("MONDAY", "FRIDAY"));

        ResponseEntity<?> response = controller.updateWorkDetails(updateRequest);
        String body = (String) response.getBody();

        assertNotNull(body);
        assertTrue(body.contains("08:30"));
        assertTrue(body.contains("16:30"));
        assertTrue(body.contains("MONDAY"));
        assertTrue(body.contains("FRIDAY"));

        assertEquals(LocalTime.of(8,30), owner.getWorkDayStart());
        assertEquals(LocalTime.of(16,30), owner.getWorkDayEnd());
        assertEquals(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), owner.getOffDays());
    }

    @Test
    void testUpdateWorkDetails_OwnerNotFound() {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", "invalid-id");
        updateRequest.put("start", "09:00");
        updateRequest.put("end", "17:00");
        updateRequest.put("offDays", List.of("MONDAY"));

        ResponseEntity<?> response = controller.updateWorkDetails(updateRequest);
        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Owner not found with id: invalid-id", body.get("error"));
    }

    // --- Calendar summary tests ---

    @Test
    void testGetTodaySummary_WithAppointments() {
        CalendarOwner owner = (CalendarOwner) controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
        Invitee invitee = new Invitee("Bob","bob@example.com");
        owner.getCalendar().addAppointment(new Appointment(LocalDateTime.now().plusHours(1),
                "Meeting 1", invitee, owner));

        ResponseEntity<?> response = controller.getTodaySummary(owner.getId());
        String result = (String) response.getBody();
        assertTrue(result.contains("Today you have"));
        assertTrue(result.contains("Meeting 1"));
    }

    @Test
    void testGetTodaySummary_NoAppointments() {
        CalendarOwner owner = (CalendarOwner) controller.createOwner(Map.of("name","David","email","david@example.com")).getBody();
        ResponseEntity<?> response = controller.getTodaySummary(owner.getId());
        String result = (String) response.getBody();
        assertEquals("You have no appointments today.", result);
    }

    @Test
    void testGetFullSummary_UpcomingAppointments() {
        CalendarOwner owner = (CalendarOwner) controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
        Invitee invitee = new Invitee("Bob","bob@example.com");
        owner.getCalendar().addAppointment(new Appointment(LocalDateTime.now().plusHours(1),
                "Meeting 1", invitee, owner));
        owner.getCalendar().addAppointment(new Appointment(LocalDateTime.now().plusDays(1).withHour(10),
                "Meeting 2", invitee, owner));

        ResponseEntity<?> response = controller.getFullSummary(owner.getId());
        String result = (String) response.getBody();
        assertTrue(result.contains("On"));
        assertTrue(result.contains("Meeting 1") || result.contains("Meeting 2"));
    }

    @Test
    void testGetFullSummary_NoCalendar() {
        CalendarOwner owner = (CalendarOwner) controller.createOwner(Map.of("name","Eve","email","eve@example.com")).getBody();
        owner.setCalendar(null);

        ResponseEntity<?> response = controller.getFullSummary(owner.getId());
        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Calendar not found for owner with id: " + owner.getId(), body.get("error"));
    }

    @Test
    void testGetFullSummary_InvalidId() {
        ResponseEntity<?> response = controller.getFullSummary("invalid-id");
        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(((String) body.get("error")).contains("Calendar owner not found for id: invalid-id"));
    }
}
