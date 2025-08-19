package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarOwnerService;
import com.example.calendar_booking_system.service.CalendarOwnerServiceImpl;
import com.example.calendar_booking_system.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CalendarOwnerControllerTest {

    private CalendarOwnerController controller;
    private CalendarOwnerService calendarOwnerService;
    private CalendarOwnerRepository repository;
    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        repository = new CalendarOwnerRepository();
        calendarService = new CalendarService(repository);
        calendarOwnerService = new CalendarOwnerServiceImpl(repository, calendarService);
        controller = new CalendarOwnerController(calendarOwnerService, calendarService);
    }

    // --- Owner CRUD tests ---

    @Test
    void testCreateOwner() {
        Map<String, String> request = Map.of("name", "Alice", "email", "alice@example.com");
        ResponseEntity<CalendarOwner> response = controller.createOwner(request);
        CalendarOwner owner = response.getBody();

        assertNotNull(owner.getId());
        assertEquals("Alice", owner.getName());
        assertEquals("alice@example.com", owner.getEmail());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testGetAllOwners() {
        controller.createOwner(Map.of("name", "Alice", "email", "alice@example.com"));
        controller.createOwner(Map.of("name", "Bob", "email", "bob@example.com"));

        Set<CalendarOwner> owners = controller.getAllOwners();
        assertEquals(2, owners.size());
    }

    @Test
    void testGetWorkDetails() {
        CalendarOwner owner = controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
        assertNotNull(owner);

        owner.setWorkHours(LocalTime.of(9,0), LocalTime.of(17,0));
        owner.setOffDays(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        ResponseEntity<String> response = controller.getWorkDetails(owner.getId());
        String body = response.getBody();

        assertNotNull(body);
        assertTrue(body.contains("09:00"));
        assertTrue(body.contains("17:00"));
        assertTrue(body.contains("SATURDAY"));
        assertTrue(body.contains("SUNDAY"));
    }

    @Test
    void testGetWorkDetails_OwnerNotFound() {
        Exception ex = assertThrows(RuntimeException.class,
                () -> controller.getWorkDetails("invalid-id"));
        assertEquals("Owner not found with id: invalid-id", ex.getMessage());
    }

    @Test
    void testUpdateWorkDetails() {
        CalendarOwner owner = controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
        assertNotNull(owner);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", owner.getId());
        updateRequest.put("start", "08:30");
        updateRequest.put("end", "16:30");
        updateRequest.put("offDays", List.of("MONDAY", "FRIDAY"));

        ResponseEntity<String> response = controller.updateWorkDetails(updateRequest);
        String body = response.getBody();

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

        Exception ex = assertThrows(RuntimeException.class,
                () -> controller.updateWorkDetails(updateRequest));
        assertEquals("Owner not found with id: invalid-id", ex.getMessage());
    }

    // --- Calendar summary tests ---

    @Test
    void testGetTodaySummary_WithAppointments() {
        CalendarOwner owner = controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
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
        CalendarOwner owner = controller.createOwner(Map.of("name","David","email","david@example.com")).getBody();
        ResponseEntity<?> response = controller.getTodaySummary(owner.getId());
        String result = (String) response.getBody();
        assertEquals("You have no appointments today.", result);
    }

    @Test
    void testGetFullSummary_UpcomingAppointments() {
        CalendarOwner owner = controller.createOwner(Map.of("name","Alice","email","alice@example.com")).getBody();
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
        CalendarOwner owner = controller.createOwner(Map.of("name","Eve","email","eve@example.com")).getBody();
        owner.setCalendar(null);

        Exception ex = assertThrows(RuntimeException.class,
                () -> controller.getFullSummary(owner.getId()));
        assertEquals("Calendar not found", ex.getMessage());
    }

    @Test
    void testGetFullSummary_InvalidId() {
        Exception ex = assertThrows(RuntimeException.class,
                () -> controller.getFullSummary("invalid-id"));
        assertTrue(ex.getMessage().contains("Calendar owner not found"));
    }
}