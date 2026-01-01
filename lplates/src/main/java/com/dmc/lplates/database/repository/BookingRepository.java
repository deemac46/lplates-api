package com.dmc.lplates.database.repository;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;

import java.util.List;

public interface BookingRepository {

    void insertRecord(Booking booking, long instructorId);
    Booking getBookingById(long bookingId);
    List<Booking> getAllBookings();
    Booking confirmBooking(Long bookingId);


}
