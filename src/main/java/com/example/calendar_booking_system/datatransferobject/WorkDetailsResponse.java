package com.example.calendar_booking_system.datatransferobject;

import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Set;

public class WorkDetailsResponse {
    private String id;
    private LocalTime start;
    private LocalTime end;
    private Set<DayOfWeek> offDays;

    // Constructor
    public WorkDetailsResponse(String id, LocalTime start, LocalTime end, Set<DayOfWeek> offDays) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.offDays = offDays;
    }

    // Getters
    public String getId() { return id; }
    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }
    public Set<DayOfWeek> getOffDays() { return offDays; }
}
