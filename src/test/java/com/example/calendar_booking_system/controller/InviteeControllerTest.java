package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.datatransferobject.SlotRequest;
import com.example.calendar_booking_system.datatransferobject.AppointmentRequest;
import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import com.example.calendar_booking_system.service.InviteeService;
import com.example.calendar_booking_system.service.InviteeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InviteeControllerTest {

    private InviteeController controller;
    private CalendarOwnerRepository repository;
    private CalendarService calendarService;
    private InviteeService inviteeService;
    private CalendarOwner owner;
    private Invitee invitee;

    @BeforeEach
    void setUp() {
        repository = new CalendarOwnerRepository();
        calendarService = new CalendarService(repository);
        inviteeService = new InviteeServiceImpl(calendarService, repository);
        controller = new InviteeController(inviteeService);

        // Create an owner with working hours 9-17 and off-days Saturday & Sunday
        owner = new CalendarOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        owner.setOffDays(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        repository.save(owner);

        // Create an invitee
        invitee = new Invitee("Bob", "bob@example.com");
        controller.createInvitee(invitee);
    }

    // ---------------- Invitee Tests ----------------
    @Test
    void testCreateAndGetInvitee() {
        ResponseEntity<?> response = controller.getInvitee();
        assertNotNull(response);
        Invitee returnedInvitee = (Invitee) response.getBody();
        assertNotNull(returnedInvitee);
        assertEquals("Bob", returnedInvitee.getName());
        assertEquals("bob@example.com", returnedInvitee.getEmail());
    }

    // ---------------- Available Slot Tests ----------------
    @Test
    void testAvailableSlotsNormalDay() {
        LocalDate date = LocalDate.now().plusDays(1);

        owner.getCalendar().addAppointment(new Appointment(date.atTime(9, 0), "Meeting1", invitee, owner));
        owner.getCalendar().addAppointment(new Appointment(date.atTime(13, 0), "Meeting2", invitee, owner));

        SlotRequest req = new SlotRequest(owner.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        ResponseEntity<String> response = controller.getAvailableSlots(req);

        String slotsPart = response.getBody().split(":")[1].trim();
        List<String> slots = Arrays.asList(slotsPart.split(", "));

        assertTrue(slots.contains("10"));
        assertTrue(slots.contains("11"));
        assertTrue(slots.contains("12"));
        assertTrue(slots.contains("14"));
        assertFalse(slots.contains("9"));
        assertFalse(slots.contains("13"));
    }

    @Test
    void testAvailableSlotsOffDay() {
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        SlotRequest req = new SlotRequest(owner.getId(), nextSaturday.getYear(),
                nextSaturday.getMonthValue(), nextSaturday.getDayOfMonth());
        ResponseEntity<String> response = controller.getAvailableSlots(req);
        assertEquals("It’s an off day. No appointment possible.", response.getBody());
    }

    @Test
    void testAvailableSlotsTooFarAhead() {
        LocalDate farDate = LocalDate.now().plusDays(16);
        SlotRequest req = new SlotRequest(owner.getId(), farDate.getYear(),
                farDate.getMonthValue(), farDate.getDayOfMonth());
        ResponseEntity<String> response = controller.getAvailableSlots(req);
        assertEquals("Too far ahead. Please choose a date within the next 15 days.", response.getBody());
    }

    @Test
    void testAvailableSlotsInvalidDateFormat() {
        SlotRequest req = new SlotRequest(owner.getId(), 2025, 13, 40);
        ResponseEntity<String> response = controller.getAvailableSlots(req);
        assertEquals("Invalid date provided. Please provide a valid year, month, and day.", response.getBody());
    }

    @Test
    void testAvailableSlotsNoFreeHours() {
        LocalDate date = LocalDate.now().plusDays(2);
        for (int h = 9; h < 17; h++) {
            owner.getCalendar().addAppointment(new Appointment(date.atTime(h, 0), "Full", invitee, owner));
        }
        SlotRequest req = new SlotRequest(owner.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        ResponseEntity<String> response = controller.getAvailableSlots(req);
        assertEquals("No free slots available on " + date + ".", response.getBody());
    }

    // ---------------- Booking Tests ----------------
    @Test
    void testScheduleAppointmentAlreadyOccupied() {
        LocalDate date = LocalDate.now().plusDays(1);

        controller.getAvailableSlots(new SlotRequest(owner.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth()));

        // Book first appointment at 11
        controller.bookAppointment(new AppointmentRequest(owner.getId(), "First Meeting", date.getDayOfMonth(),
                date.getMonthValue(), date.getYear(), 11));

        AppointmentRequest req2 = new AppointmentRequest(owner.getId(), "Second Meeting", date.getDayOfMonth(),
                date.getMonthValue(), date.getYear(), 11);
        ResponseEntity<?> response = controller.bookAppointment(req2);

        assertEquals("No booking for this time. Book appointment in one of the available slots.", response.getBody());
    }

    @Test
    void testScheduleAppointmentOutsideWorkingHours() {
        LocalDate date = LocalDate.now().plusDays(1);
        controller.getAvailableSlots(new SlotRequest(owner.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth()));

        AppointmentRequest req = new AppointmentRequest(owner.getId(), "Early Meeting", date.getDayOfMonth(),
                date.getMonthValue(), date.getYear(), 8);

        ResponseEntity<?> response = controller.bookAppointment(req);
        assertEquals("No booking for this time. Book appointment in one of the available slots.", response.getBody());
    }

    @Test
    void testScheduleAppointmentOnOffDay() {
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        controller.getAvailableSlots(new SlotRequest(owner.getId(), nextSaturday.getYear(), nextSaturday.getMonthValue(), nextSaturday.getDayOfMonth()));

        AppointmentRequest req = new AppointmentRequest(owner.getId(), "Weekend Meeting", nextSaturday.getDayOfMonth(),
                nextSaturday.getMonthValue(), nextSaturday.getYear(), 10);

        ResponseEntity<?> response = controller.bookAppointment(req);
        assertEquals("No available slots on this date. Try another date.", response.getBody());
    }

    @Test
    void testScheduleAppointmentAt15DayLimit() {
        LocalDate limitDate = LocalDate.now().plusDays(15);
        controller.getAvailableSlots(new SlotRequest(owner.getId(), limitDate.getYear(), limitDate.getMonthValue(), limitDate.getDayOfMonth()));

        AppointmentRequest req = new AppointmentRequest(owner.getId(), "Limit Meeting", limitDate.getDayOfMonth(),
                limitDate.getMonthValue(), limitDate.getYear(), 16);

        ResponseEntity<?> response = controller.bookAppointment(req);
        Appointment appt = (Appointment) response.getBody();
        assertNotNull(appt);
        assertEquals("Limit Meeting", appt.getSubject());
    }

    @Test
    void testConcurrentBookingSameSlot() throws InterruptedException {
        LocalDate date = LocalDate.now().plusDays(1);
        controller.getAvailableSlots(new SlotRequest(owner.getId(), date.getYear(), date.getMonthValue(), date.getDayOfMonth()));

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable bookTask = () -> {
            AppointmentRequest req = new AppointmentRequest(owner.getId(), "Concurrent Meeting", date.getDayOfMonth(),
                    date.getMonthValue(), date.getYear(), 10);

            ResponseEntity<?> resp = controller.bookAppointment(req);
            if (resp.getStatusCode().is2xxSuccessful()) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        };

        for (int i = 0; i < threadCount; i++) new Thread(bookTask).start();
        latch.await();

        assertEquals(1, successCount.get());
        assertEquals(1, owner.getCalendar().getAppointments().size());
    }

    // ---------------- Owner Info Tests ----------------
    @Test
    void testGetOwnerInfo() {
        ResponseEntity<?> response = controller.getOwner(owner.getId());
        assertNotNull(response);
        CalendarOwner returnedOwner = (CalendarOwner) response.getBody();
        assertEquals(owner.getName(), returnedOwner.getName());
    }

    @Test
    void testGetOwnerInfoNotFound() {
        ResponseEntity<?> response = controller.getOwner("invalid-id");
        assertEquals("CalendarOwner not found for id: invalid-id", ((Map<?, ?>) response.getBody()).get("error"));
    }
}