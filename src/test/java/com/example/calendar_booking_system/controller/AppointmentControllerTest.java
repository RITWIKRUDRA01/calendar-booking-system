package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.datatransferobject.AppointmentRequest;
import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentControllerTest {
/*
    private AppointmentController controller;
    private CalendarServiceImpl calendarService;
    private Invitee invitee;
    private CalendarOwner owner;
    private CalendarOwnerRepository ownerRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        ownerRepository = new CalendarOwnerRepository();

        calendarService = new CalendarServiceImpl(ownerRepository);

        // Setup invitee
        invitee = new Invitee("Bob", "bob@example.com");

        // Setup calendar owner
        owner = new CalendarOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        owner.setOffDays(Collections.singleton(DayOfWeek.SATURDAY));

        // Save owner in repository for dynamic lookup
        ownerRepository.save(owner);

        // Initialize controller with only invitee and service
        controller = new AppointmentController(invitee, calendarService,ownerRepository);
    }

    private AppointmentRequest buildRequest(String ownerId, LocalDateTime time, String subject) {
        AppointmentRequest req = new AppointmentRequest();
        req.setOwnerId(ownerId);
        req.setYear(time.getYear());
        req.setMonth(time.getMonthValue());
        req.setDay(time.getDayOfMonth());
        req.setHour(time.getHour());
        req.setSubject(subject);
        return req;
    }

    @Test
    void testScheduleAppointmentSuccessful() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        ResponseEntity<Appointment> response = controller.scheduleAppointment(
                buildRequest(owner.getId(), apptTime, "Team Meeting")
        );
        Appointment appt = response.getBody();
        assertNotNull(appt);
        assertEquals("Team Meeting", appt.getSubject());
        assertTrue(invitee.getAppointments().contains(appt));
        assertTrue(owner.getCalendar().getAppointments().contains(appt));
    }

    @Test
    void testScheduleAppointmentAlreadyOccupied() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);

        controller.scheduleAppointment(buildRequest(owner.getId(), apptTime, "First Meeting"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(buildRequest(owner.getId(), apptTime, "Second Meeting")));
        assertEquals("Already occupied, try another slot.", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentOutsideWorkingHours() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(buildRequest(owner.getId(), apptTime, "Early Meeting")));
        assertEquals("Appointment outside working hours.", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentOnOffDay() {
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDateTime apptTime = nextSaturday.atTime(10, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(buildRequest(owner.getId(), apptTime, "Weekend Meeting")));
        assertEquals("Appointments not allowed on off days.", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(buildRequest(owner.getId(), pastTime, "Past Meeting")));
        assertEquals("Appointments must be within the next 15 days (including today).", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentAt15DayLimit() {
        LocalDateTime limitTime = LocalDate.now().plusDays(15).atTime(16, 0);
        ResponseEntity<Appointment> response = controller.scheduleAppointment(
                buildRequest(owner.getId(), limitTime, "Limit Meeting")
        );
        Appointment appt = response.getBody();
        assertNotNull(appt);
        assertEquals("Limit Meeting", appt.getSubject());
    }

    @Test
    void testConcurrentBookingSameSlot() throws InterruptedException {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable bookTask = () -> {
            try {
                controller.scheduleAppointment(buildRequest(owner.getId(), apptTime, "Concurrent Meeting"));
                successCount.incrementAndGet();
            } catch (IllegalArgumentException ignored) {
            } finally {
                latch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            new Thread(bookTask).start();
        }

        latch.await();

        assertEquals(1, successCount.get());
        assertEquals(1, owner.getCalendar().getAppointments().size());
    }

    @Test
    void testOwnerNotFound() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.scheduleAppointment(buildRequest("invalid-id", apptTime, "Test")));
        assertEquals("CalendarOwner not found for id: invalid-id", ex.getMessage());
    }

    @Test
    void testGetInviteeInfo() {
        // ---------------- Call API and unwrap ResponseEntity ----------------
        ResponseEntity<?> response = controller.getInvitee();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Invitee returnedInvitee = (Invitee) response.getBody(); // Extract Invitee from body
        assertNotNull(returnedInvitee);
        assertEquals(invitee.getName(), returnedInvitee.getName());
        assertEquals(invitee.getEmail(), returnedInvitee.getEmail());
    }

    @Test
    void testGetOwnerInfo() {
        ResponseEntity<?> response = controller.getOwner(owner.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        CalendarOwner returnedOwner = (CalendarOwner) response.getBody();
        assertNotNull(returnedOwner);
        assertEquals(owner.getName(), returnedOwner.getName());
        assertEquals(owner.getEmail(), returnedOwner.getEmail());
    }

    @Test
    void testGetOwnerInfoNotFound() {
        ResponseEntity<?> response = controller.getOwner("invalid-id");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("CalendarOwner not found for id: invalid-id", body.get("error"));
    }

 */
}