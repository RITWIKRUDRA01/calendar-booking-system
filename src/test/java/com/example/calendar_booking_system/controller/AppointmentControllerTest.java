package com.example.calendar_booking_system.controller;

import com.example.calendar_booking_system.entity.Appointment;
import com.example.calendar_booking_system.entity.CalendarOwner;
import com.example.calendar_booking_system.entity.Invitee;
import com.example.calendar_booking_system.repository.CalendarOwnerRepository;
import com.example.calendar_booking_system.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentControllerTest {

    private AppointmentController controller;
    private CalendarService calendarService;
    private Invitee invitee;
    private CalendarOwner owner;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarService();

        // Setup invitee
        invitee = new Invitee("Bob", "bob@example.com");

        // Setup calendar owner
        owner = new CalendarOwner("Alice", "alice@example.com");
        owner.setWorkHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        owner.setOffDays(Collections.singleton(DayOfWeek.SATURDAY));

        // Save owner in repository for dynamic lookup
        CalendarOwnerRepository.save(owner);

        // Initialize controller with only invitee and service
        controller = new AppointmentController(invitee, calendarService);
    }

    @Test
    void testScheduleAppointmentSuccessful() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment appt = controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "Team Meeting");

        assertNotNull(appt);
        assertEquals("Team Meeting", appt.getSubject());
        assertTrue(invitee.getAppointments().contains(appt));
        assertTrue(owner.getCalendar().getAppointments().contains(appt));
    }

    @Test
    void testScheduleAppointmentAlreadyOccupied() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);
        controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "First Meeting");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "Second Meeting"));
        assertEquals("Already occupied, try another slot.", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentOutsideWorkingHours() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "Early Meeting"));
        assertEquals("Appointment outside working hours.", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentOnOffDay() {
        LocalDate nextSaturday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDateTime apptTime = nextSaturday.atTime(10, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "Weekend Meeting"));
        assertEquals("Appointments not allowed on off days.", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentWrongMinute() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "Invalid Minute Meeting"));
        assertEquals("Appointments must start exactly on the hour (e.g., 10:00).", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.scheduleAppointment(owner.getId(), pastTime.format(ISO_FORMATTER), "Past Meeting"));
        assertEquals("Appointments must be within the next 15 days (including today).", ex.getMessage());
    }

    @Test
    void testScheduleAppointmentAt15DayLimit() {
        LocalDateTime limitTime = LocalDate.now().plusDays(15).atTime(16, 0);
        Appointment appt = controller.scheduleAppointment(owner.getId(), limitTime.format(ISO_FORMATTER), "Limit Meeting");

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
                controller.scheduleAppointment(owner.getId(), apptTime.format(ISO_FORMATTER), "Concurrent Meeting");
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

        // Only one thread should succeed
        assertEquals(1, successCount.get());
        assertEquals(1, owner.getCalendar().getAppointments().size());
    }

    @Test
    void testOwnerNotFound() {
        LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.scheduleAppointment("invalid-id", apptTime.format(ISO_FORMATTER), "Test"));
        assertEquals("CalendarOwner not found for id: invalid-id", ex.getMessage());
    }

    @Test
    void testGetInviteeInfo() {
        Invitee returnedInvitee = controller.getInvitee();
        assertNotNull(returnedInvitee);
        assertEquals(invitee.getName(), returnedInvitee.getName());
        assertEquals(invitee.getEmail(), returnedInvitee.getEmail());
    }

    @Test
    void testGetOwnerInfo() {
        CalendarOwner returnedOwner = controller.getOwner(owner.getId()); // pass owner ID
        assertNotNull(returnedOwner);
        assertEquals(owner.getName(), returnedOwner.getName());
        assertEquals(owner.getEmail(), returnedOwner.getEmail());
    }

    @Test
    void testGetOwnerInfoNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.getOwner("invalid-id")); // invalid ID
        assertEquals("CalendarOwner not found for id: invalid-id", ex.getMessage());
    }
}