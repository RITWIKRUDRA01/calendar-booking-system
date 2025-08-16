package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AppointmentControllerTest {

    private MockMvc mockMvc;
    private Invitee invitee;
    private CalendarOwner owner;

    @BeforeEach
    public void setUp() {
        // Initialize real objects
        invitee = new Invitee("Bob Invitee", "bob@example.com");
        owner = new CalendarOwner("Alice Owner", "alice@example.com");

        // Pass real objects into controller
        AppointmentController controller = new AppointmentController(invitee, owner);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testScheduleAppointment() throws Exception {
        String startTime = "2025-08-16T14:00:00";
        String subject = "Project Discussion";

        mockMvc.perform(post("/api/schedule/appointment")
                        .param("startTime", startTime)
                        .param("subject", subject)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value(subject))
                .andExpect(jsonPath("$.startTime").value(startTime))
                .andExpect(jsonPath("$.invitee.name").value("Bob Invitee"))
                .andExpect(jsonPath("$.invitee.email").value("bob@example.com"))
                .andExpect(jsonPath("$.owner.name").value("Alice Owner"))
                .andExpect(jsonPath("$.owner.email").value("alice@example.com"))
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