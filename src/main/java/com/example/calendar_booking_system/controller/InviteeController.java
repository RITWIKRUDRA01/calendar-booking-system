package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Invitee;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitees")
public class InviteeController {

    // For simplicity, store a single invitee in memory
    private Invitee invitee;

    // Create Invitee
    @PostMapping
    public Invitee createInvitee(@RequestBody Invitee requestInvitee) {
        // store in memory
        this.invitee = new Invitee(requestInvitee.getName(), requestInvitee.getEmail());
        return this.invitee;
    }

    // Get invitee info
    @GetMapping
    public Invitee getInvitee() {
        if (invitee == null) {
            throw new RuntimeException("No invitee created yet");
        }
        return invitee;
    }
}