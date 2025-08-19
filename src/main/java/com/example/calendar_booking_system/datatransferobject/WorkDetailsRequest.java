package com.example.calendar_booking_system.datatransferobject;

import java.util.List;

public class WorkDetailsRequest {
    private String id;
    private String start;
    private String end;
    private List<String> offDays;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }

    public List<String> getOffDays() { return offDays; }
    public void setOffDays(List<String> offDays) { this.offDays = offDays; }
}
