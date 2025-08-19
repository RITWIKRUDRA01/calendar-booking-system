package com.example.calendar_booking_system.repository;

import com.example.calendar_booking_system.entity.CalendarOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarOwnerRepositoryTest {
    private CalendarOwnerRepository repository;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        repository = new CalendarOwnerRepository();
    }

    @Test
    void testSaveAndFindById() {
        CalendarOwner owner = new CalendarOwner("Alice", "alice@example.com");
        repository.save(owner);

        CalendarOwner retrieved = repository.findById(owner.getId());
        assertNotNull(retrieved);
        assertEquals(owner.getName(), retrieved.getName());
        assertEquals(owner.getEmail(), retrieved.getEmail());
    }

    @Test
    void testFindByIdNotFound() {
        CalendarOwner retrieved = repository.findById(UUID.randomUUID().toString());
        assertNull(retrieved);
    }

    @Test
    void testFindAll() {
        CalendarOwner owner1 = new CalendarOwner("Alice", "alice@example.com");
        CalendarOwner owner2 = new CalendarOwner("Bob", "bob@example.com");

        repository.save(owner1);
        repository.save(owner2);

        List<CalendarOwner> allOwners = repository.findAll();
        assertEquals(2, allOwners.size());
        assertTrue(allOwners.contains(owner1));
        assertTrue(allOwners.contains(owner2));
    }
}