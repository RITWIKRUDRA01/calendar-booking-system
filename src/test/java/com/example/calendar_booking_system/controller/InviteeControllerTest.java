package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Invitee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InviteeController.class)
public class InviteeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateInvitee() throws Exception {
        String jsonBody = "{\"name\":\"Bob Invitee\",\"email\":\"bob@example.com\"}";

        mockMvc.perform(post("/api/invitees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob Invitee"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void testGetInvitee() throws Exception {
        // First create invitee
        String jsonBody = "{\"name\":\"Bob Invitee\",\"email\":\"bob@example.com\"}";
        mockMvc.perform(post("/api/invitees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        // Then get invitee
        mockMvc.perform(get("/api/invitees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob Invitee"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }
}