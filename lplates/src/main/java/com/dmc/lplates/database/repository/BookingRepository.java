package com.dmc.lplates.database.repository;

import java.util.List;

import com.dmc.lplates.inbound.models.Booking;

public interface BookingRepository {

    void insertRecord(Booking lesson);
    Booking getBookingById(long lessonId);
    List<Booking> getAllBookings();
    List<Booking> getLessonsByInstructorId(long instructorId);
    List<Booking> getLessonsByStudentId(long studentId);
    Booking confirmBooking(Long lessonId);
    Booking updateBooking(Booking lesson);

}
