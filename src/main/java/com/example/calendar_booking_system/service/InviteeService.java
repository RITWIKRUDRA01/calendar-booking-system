package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.datatransferobject.SlotRequest;
import com.example.calendar_booking_system.datatransferobject.AppointmentRequest;
import com.example.calendar_booking_system.entity.Invitee;
import org.springframework.http.ResponseEntity;

public interface InviteeService {

    ResponseEntity<?> createInvitee(Invitee invitee);

    ResponseEntity<?> getInvitee();

    ResponseEntity<String> getAvailableSlots(SlotRequest request);

    ResponseEntity<?> bookAppointment(AppointmentRequest request);

    ResponseEntity<?> getOwner(String ownerId);
}