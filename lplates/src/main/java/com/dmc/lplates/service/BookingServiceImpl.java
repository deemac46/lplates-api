package com.dmc.lplates.service;

import com.dmc.lplates.database.repository.BookingRepository;
import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(@Autowired BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public String createBooking(Booking booking, long instructorId) {

        bookingRepository.insertRecord(booking, instructorId);
        return String.valueOf(booking.getBookingId());
    }

    @Override
    public void cancelBooking() {



    }

    @Override
    public Booking getBookingDetailsById(Long bookingId) {
        return bookingRepository.getBookingById(bookingId);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.getAllBookings();
    }

    @Override
    public List<Booking> getAllPendingBookings() {
        return bookingRepository.getAllBookings().stream().filter(booking ->
                "PENDING".equalsIgnoreCase(booking
                        .getStatus()))
                .toList();
    }

    @Override
    public Booking confirmBooking(Long bookingId) {

        return bookingRepository.confirmBooking(bookingId);
    }
}
