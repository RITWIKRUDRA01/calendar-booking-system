package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        CalendarOwner owner = controller.createOwner("Alice", "alice@example.com");
        assertNotNull(owner.getId());
        assertEquals("Alice", owner.getName());
        assertEquals("alice@example.com", owner.getEmail());

        // Owner should be saved in repository
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testGetAllOwners() {
        controller.createOwner("Alice", "alice@example.com");
        controller.createOwner("Bob", "bob@example.com");

        List<CalendarOwner> owners = controller.getAllOwners();
        assertEquals(2, owners.size());
    }

    @Test
    void testGetSettings() {
        CalendarOwner owner = controller.createOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        Set<DayOfWeek> offDays = new HashSet<>();
        offDays.add(DayOfWeek.SATURDAY);
        offDays.add(DayOfWeek.SUNDAY);
        owner.setOffDays(offDays);

        Map<String, Object> settings = controller.getSettings(owner.getId());
        assertEquals(LocalTime.of(9, 0), settings.get("workDayStart"));
        assertEquals(LocalTime.of(17, 0), settings.get("workDayEnd"));
        assertEquals(offDays, settings.get("offDays"));
    }

    @Test
    void testGetSettings_OwnerNotFound() {
        Exception ex = assertThrows(RuntimeException.class, () -> controller.getSettings("invalid-id"));
        assertEquals("Owner not found", ex.getMessage());
    }

    @Test
    void testUpdateHours() {
        CalendarOwner owner = controller.createOwner("Alice", "alice@example.com");
        String response = controller.updateHours(owner.getId(), "08:30", "16:30");
        assertEquals("Working hours updated.", response);
        assertEquals(LocalTime.of(8, 30), owner.getWorkDayStart());
        assertEquals(LocalTime.of(16, 30), owner.getWorkDayEnd());
    }

    @Test
    void testUpdateHours_OwnerNotFound() {
        Exception ex = assertThrows(RuntimeException.class, () -> controller.updateHours("invalid-id", "09:00", "17:00"));
        assertEquals("Owner not found", ex.getMessage());
    }

    @Test
    void testUpdateOffDays() {
        CalendarOwner owner = controller.createOwner("Alice", "alice@example.com");
        Set<DayOfWeek> offDays = new HashSet<>();
        offDays.add(DayOfWeek.MONDAY);
        offDays.add(DayOfWeek.FRIDAY);

        String response = controller.updateOffDays(owner.getId(), offDays);
        assertEquals("Off days updated.", response);
        assertEquals(offDays, owner.getOffDays());
    }

    @Test
    void testUpdateOffDays_OwnerNotFound() {
        Set<DayOfWeek> offDays = new HashSet<>();
        offDays.add(DayOfWeek.MONDAY);

        Exception ex = assertThrows(RuntimeException.class, () -> controller.updateOffDays("invalid-id", offDays));
        assertEquals("Owner not found", ex.getMessage());
    }
}