package com.example.calendar_booking_system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testScheduleAppointment() throws Exception {
        // Hardcoded values matching what AppointmentController will return
        String startTime = "2025-08-16T14:00:00";
        String subject = "Project Discussion";
        String inviteeName = "Bob Invitee";
        String inviteeEmail = "bob@example.com";
        String ownerName = "Alice Owner";
        String ownerEmail = "alice@example.com";

        mockMvc.perform(post("/api/schedule/appointment")
                        .param("startTime", startTime)
                        .param("subject", subject))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value(subject))
                .andExpect(jsonPath("$.startTime").exists()) // just check field exists
                .andExpect(jsonPath("$.invitee.name").value(inviteeName))
                .andExpect(jsonPath("$.invitee.email").value(inviteeEmail))
                .andExpect(jsonPath("$.owner.name").value(ownerName))
                .andExpect(jsonPath("$.owner.email").value(ownerEmail))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void testGetInviteeInfo() throws Exception {
        mockMvc.perform(get("/api/schedule/invitee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob Invitee"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void testGetOwnerInfo() throws Exception {
        mockMvc.perform(get("/api/schedule/owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Owner"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }
}