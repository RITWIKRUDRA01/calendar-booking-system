package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.datatransferobject.SlotRequest;
import com.example.calendar_booking_system.datatransferobject.AppointmentRequest;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.service.InviteeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitees")
public class InviteeController {

    private final InviteeService inviteeService;

    public InviteeController(InviteeService inviteeService) {
        this.inviteeService = inviteeService;
    }

    @PostMapping
    public ResponseEntity<?> createInvitee(@RequestBody Invitee requestInvitee) {
        return inviteeService.createInvitee(requestInvitee);
    }

    @GetMapping
    public ResponseEntity<?> getInvitee() {
        return inviteeService.getInvitee();
    }

    @PostMapping("/available-slots")
    public ResponseEntity<String> getAvailableSlots(@RequestBody SlotRequest req) {
        return inviteeService.getAvailableSlots(req);
    }

    @PostMapping("/book-appointment")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentRequest req) {
        return inviteeService.bookAppointment(req);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getOwner(@PathVariable String ownerId) {
        return inviteeService.getOwner(ownerId);
    }

    @GetMapping("/invitee")
    public ResponseEntity<?> getMeetingInvitee() {
        return inviteeService.getInvitee();
    }
}