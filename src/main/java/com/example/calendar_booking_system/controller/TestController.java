package com.example.calendar_booking_system.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    // Elementary Get API
    @GetMapping
    public String testGet() {
        return "GET API is working!";
    }

    // Elementary Post API
    @PostMapping
    public String testPost(@RequestBody String requestData) {
        return "POST API received: " + requestData;
    }

    @PostMapping("/add")
    public String addNumbers(@RequestParam int num1, @RequestParam int num2) {
        int sum = num1 + num2;
        return "Sum is: " + sum;
    }
}
