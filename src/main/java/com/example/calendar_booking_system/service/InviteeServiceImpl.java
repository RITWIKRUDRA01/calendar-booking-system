package com.example.calendar_booking_system.service;

import com.example.calendar_booking_system.datatransferobject.SlotRequest;
import com.example.calendar_booking_system.datatransferobject.AppointmentRequest;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.repository.GenericRepository;
import com.example.calendar_booking_system.service.CalendarService;
import com.example.calendar_booking_system.service.InviteeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class InviteeServiceImpl implements InviteeService {

    private Invitee invitee;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final GenericRepository<CalendarOwner, String> calendarOwnerRepository;
    private final CalendarService calendarService;
    private final ReentrantLock lock = new ReentrantLock();

    private String lastLookupOwnerId;
    private LocalDate lastLookupDate;
    private Set<Integer> lastAvailableSlots = new HashSet<>();

    public InviteeServiceImpl(CalendarService calendarService,
                              GenericRepository<CalendarOwner, String> calendarOwnerRepository) {
        this.calendarService = calendarService;
        this.calendarOwnerRepository = calendarOwnerRepository;
    }

    @Override
    public ResponseEntity<?> createInvitee(Invitee requestInvitee) {
        if (requestInvitee.getName() == null || requestInvitee.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name must not be empty"));
        }
        if (requestInvitee.getEmail() == null || requestInvitee.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email must not be empty"));
        }
        this.invitee = new Invitee(requestInvitee.getName(), requestInvitee.getEmail());
        return ResponseEntity.ok(this.invitee);
    }

    @Override
    public ResponseEntity<?> getInvitee() {
        if (invitee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No invitee created yet"));
        }
        return ResponseEntity.ok(invitee);
    }

    @Override
    public ResponseEntity<String> getAvailableSlots(SlotRequest req) {
        CalendarOwner owner = calendarOwnerRepository.findById(req.getOwnerId());
        if (owner == null) {
            throw new RuntimeException("CalendarOwner not found");
        }

        LocalDate queryDate;
        try {
            queryDate = LocalDate.of(req.getYear(), req.getMonth(), req.getDay());
        } catch (DateTimeException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date provided. Please provide a valid year, month, and day.");
        }

        List<Integer> freeSlots = calendarService.getFreeSlots(owner, queryDate);

        lastLookupOwnerId = owner.getId();
        lastLookupDate = queryDate;
        lastAvailableSlots = freeSlots != null ? new HashSet<>(freeSlots) : new HashSet<>();

        if (freeSlots == null) {
            return ResponseEntity.ok("Too far ahead. Please choose a date within the next 15 days.");
        }

        if (freeSlots.isEmpty()) {
            return ResponseEntity.ok(
                    owner.getOffDays().contains(queryDate.getDayOfWeek())
                            ? "It’s an off day. No appointment possible."
                            : "No free slots available on " + queryDate + "."
            );
        }

        return ResponseEntity.ok("On " + queryDate +
                " the available hour slots are: " +
                freeSlots.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public ResponseEntity<?> bookAppointment(AppointmentRequest req) {
        if (invitee == null) {
            return ResponseEntity.badRequest()
                    .body("Invitee not created yet. Please create invitee before booking.");
        }

        CalendarOwner owner = calendarOwnerRepository.findById(req.getOwnerId());
        if (owner == null) {
            return ResponseEntity.badRequest().body("CalendarOwner not found for id: " + req.getOwnerId());
        }

        LocalDateTime appointmentTime;
        try {
            appointmentTime = LocalDateTime.of(req.getYear(), req.getMonth(), req.getDay(), req.getHour(), 0);
        } catch (DateTimeException e) {
            return ResponseEntity.badRequest().body("Invalid date/time provided.");
        }

        if (!req.getOwnerId().equals(lastLookupOwnerId)) {
            return ResponseEntity.badRequest()
                    .body("Please check availability before booking appointment for this owner.");
        }

        if (!appointmentTime.toLocalDate().equals(lastLookupDate)) {
            return ResponseEntity.badRequest()
                    .body("Please check availability before booking appointment for this date.");
        }

        if (lastAvailableSlots.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("No available slots on this date. Try another date.");
        }

        if (!lastAvailableSlots.contains(appointmentTime.getHour())) {
            return ResponseEntity.badRequest()
                    .body("No booking for this time. Book appointment in one of the available slots.");
        }

        Appointment appt = new Appointment(appointmentTime, req.getSubject(), invitee, owner);

        lock.lock();
        try {
            boolean isSlotTaken = owner.getCalendar().getAppointments().stream()
                    .anyMatch(app -> app.getStartTime().toLocalDate().equals(appointmentTime.toLocalDate())
                            && app.getStartTime().getHour() == appointmentTime.getHour());
            if (isSlotTaken) {
                return ResponseEntity.badRequest().body("Already occupied, try another slot.");
            }

            invitee.addAppointment(appt);
            owner.getCalendar().addAppointment(appt);
            lastAvailableSlots.remove(appointmentTime.getHour());
        } finally {
            lock.unlock();
        }

        return ResponseEntity.ok(appt);
    }

    @Override
    public ResponseEntity<?> getOwner(String ownerId) {
        CalendarOwner owner = calendarOwnerRepository.findById(ownerId);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "CalendarOwner not found for id: " + ownerId));
        }
        return ResponseEntity.ok(owner);
    }
}