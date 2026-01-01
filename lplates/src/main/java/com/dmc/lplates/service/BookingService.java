package com.dmc.lplates.service;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;

import java.util.List;

public interface BookingService {

    String createBooking(Booking booking, long instructorId);
    void cancelBooking();
    Booking getBookingDetailsById(Long bookingId);
    List<Booking> getAllBookings();
    List<Booking> getAllPendingBookings();
    Booking confirmBooking(Long bookingId);

}
