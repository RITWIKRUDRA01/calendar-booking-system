package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CalendarOwnerControllerTest {

    private CalendarOwnerController controller;
    private CalendarOwnerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CalendarOwnerRepository();
        controller = new CalendarOwnerController(repository);
    }

    @Test
    void testCreateOwner() {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Alice");
        request.put("email", "alice@example.com");

        ResponseEntity<CalendarOwner> response = controller.createOwner(request); // updated
        CalendarOwner owner = response.getBody(); // updated

        assertNotNull(owner.getId());
        assertEquals("Alice", owner.getName());
        assertEquals("alice@example.com", owner.getEmail());

        // Owner should be saved in repository
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testGetAllOwners() {
        // update createOwner to pass Map
        Map<String, String> req1 = Map.of("name", "Alice", "email", "alice@example.com");
        Map<String, String> req2 = Map.of("name", "Bob", "email", "bob@example.com");

        controller.createOwner(req1);
        controller.createOwner(req2);

        List<CalendarOwner> owners = controller.getAllOwners();
        assertEquals(2, owners.size());
    }

    @Test
    void testGetWorkDetails() {
        // create owner first
        Map<String, String> request = Map.of("name", "Alice", "email", "alice@example.com");
        CalendarOwner owner = controller.createOwner(request).getBody(); // updated

        assertNotNull(owner);
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        Set<DayOfWeek> offDays = new HashSet<>();
        offDays.add(DayOfWeek.SATURDAY);
        offDays.add(DayOfWeek.SUNDAY);
        owner.setOffDays(offDays);

        ResponseEntity<String> response = controller.getWorkDetails(owner.getId()); // updated
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
                () -> controller.getWorkDetails("invalid-id")); // updated method name
        assertEquals("Owner not found with id: invalid-id", ex.getMessage());
    }

    @Test
    void testUpdateWorkDetails() {
        // create owner
        Map<String, String> request = Map.of("name", "Alice", "email", "alice@example.com");
        CalendarOwner owner = controller.createOwner(request).getBody(); // updated

        assertNotNull(owner);

        // prepare request body for update
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", owner.getId());
        updateRequest.put("start", "08:30");
        updateRequest.put("end", "16:30");
        updateRequest.put("offDays", List.of("MONDAY", "FRIDAY"));

        ResponseEntity<String> response = controller.updateWorkDetails(updateRequest); // updated
        String body = response.getBody();

        assertNotNull(body);
        assertTrue(body.contains("08:30"));
        assertTrue(body.contains("16:30"));
        assertTrue(body.contains("MONDAY"));
        assertTrue(body.contains("FRIDAY"));

        // verify entity was updated
        assertEquals(LocalTime.of(8, 30), owner.getWorkDayStart());
        assertEquals(LocalTime.of(16, 30), owner.getWorkDayEnd());
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
                () -> controller.updateWorkDetails(updateRequest)); // updated
        assertEquals("Owner not found with id: invalid-id", ex.getMessage());
    }
}