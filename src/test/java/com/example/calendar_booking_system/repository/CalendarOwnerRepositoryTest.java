package com.example.calendar_booking_system.repository;

import com.example.calendar_booking_system.entity.CalendarOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarOwnerRepositoryTest {

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        CalendarOwnerRepository.findAll().clear();
    }

    @Test
    void testSaveAndFindById() {
        CalendarOwner owner = new CalendarOwner("Alice", "alice@example.com");
        CalendarOwnerRepository.save(owner);

        CalendarOwner retrieved = CalendarOwnerRepository.findById(owner.getId());
        assertNotNull(retrieved);
        assertEquals(owner.getName(), retrieved.getName());
        assertEquals(owner.getEmail(), retrieved.getEmail());
    }

    @Test
    void testFindByIdNotFound() {
        CalendarOwner retrieved = CalendarOwnerRepository.findById(UUID.randomUUID().toString());
        assertNull(retrieved);
    }

    @Test
    void testFindAll() {
        CalendarOwner owner1 = new CalendarOwner("Alice", "alice@example.com");
        CalendarOwner owner2 = new CalendarOwner("Bob", "bob@example.com");

        CalendarOwnerRepository.save(owner1);
        CalendarOwnerRepository.save(owner2);

        List<CalendarOwner> allOwners = CalendarOwnerRepository.findAll();
        assertEquals(2, allOwners.size());
        assertTrue(allOwners.contains(owner1));
        assertTrue(allOwners.contains(owner2));
    }
}