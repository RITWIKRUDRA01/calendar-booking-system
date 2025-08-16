package com.example.calendar_booking_system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetEndpoint() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("GET API is working!"));
    }

    @Test
    public void testPostEndpoint() throws Exception {
        mockMvc.perform(post("/api/test")
                        .content("hello")
                        .contentType("text/plain"))
                .andExpect(status().isOk())
                .andExpect(content().string("POST API received: hello"));
    }

    @Test
    public void testAddEndpoint() throws Exception {
        mockMvc.perform(post("/api/test/add")
                        .param("num1", "5")
                        .param("num2", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sum is: 12"));
    }
}
