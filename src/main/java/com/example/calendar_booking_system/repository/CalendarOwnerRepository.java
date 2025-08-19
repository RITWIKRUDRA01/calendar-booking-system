package com.example.calendar_booking_system.repository;

import com.example.calendar_booking_system.entity.CalendarOwner;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Repository;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class CalendarOwnerRepository implements GenericRepository<CalendarOwner, String>{
    private final List<CalendarOwner> owners = new CopyOnWriteArrayList<>();

    @Override
    public List<CalendarOwner> findAll() {
        return List.copyOf(owners); // return an immutable copy
    }

    @Override
    public CalendarOwner findById(String id) {
        return owners.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(CalendarOwner owner) {
        owners.add(owner);
    }
}
