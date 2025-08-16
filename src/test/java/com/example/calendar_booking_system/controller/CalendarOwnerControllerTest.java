package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarOwnerController.class)
public class CalendarOwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Clear in-memory repo before each test
        CalendarOwnerRepository.findAll().clear();
    }

    @Test
    void testCreateOwner() throws Exception {
        mockMvc.perform(post("/api/owners")
                        .param("name", "Alice")
                        .param("email", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testGetOwners() throws Exception {
        // Add one manually
        CalendarOwner owner = new CalendarOwner("Bob", "bob@example.com");
        CalendarOwnerRepository.save(owner);

        mockMvc.perform(get("/api/owners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bob"))
                .andExpect(jsonPath("$[0].email").value("bob@example.com"));
    }
}