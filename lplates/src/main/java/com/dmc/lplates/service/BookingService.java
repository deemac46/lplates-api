package com.dmc.lplates.service;

import java.util.List;

import com.dmc.lplates.inbound.models.Booking;

public interface BookingService {

    String createBooking(Booking lesson);
    Booking updateBooking(Booking lesson);
    Booking getBookingDetailsById(Long lessonId);
    List<Booking> getAllBookings();
    List<Booking> getAllPendingBookings();
    List<Booking> getLessonsByInstructorId(long instructorId);
    List<Booking> getLessonsByStudentId(long studentId);
    Booking confirmBooking(Long lessonId);

}
