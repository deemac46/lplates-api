package com.dmc.lplates.service;

import java.util.List;

import com.dmc.lplates.inbound.models.Booking;

public interface BookingService {

    Booking createBooking(Booking lesson);
    Booking updateBooking(Booking lesson);
    Booking getBookingDetailsById(Long lessonId);
    List<Booking> getAllBookings();
    List<Booking> getAllPendingBookings();
    List<Booking> getLessonsByInstructorId(long instructorId);
    List<Booking> getLessonsByStudentId(long studentId);
    Booking confirmBooking(Long lessonId);
    Booking completeBooking(Booking lesson, Integer edtModuleNumber, String edtNote, Long loggedByInstructorId);

}
