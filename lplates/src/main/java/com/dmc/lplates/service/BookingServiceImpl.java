package com.dmc.lplates.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dmc.lplates.database.repository.BookingRepository;
import com.dmc.lplates.inbound.models.Booking;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(@Autowired BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public String createBooking(Booking lesson) {
        bookingRepository.insertRecord(lesson);
        return String.valueOf(lesson.getLessonId());
    }

    @Override
    public Booking updateBooking(Booking lesson) {
        return bookingRepository.updateBooking(lesson);
    }

    @Override
    public Booking getBookingDetailsById(Long lessonId) {
        return bookingRepository.getBookingById(lessonId);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.getAllBookings();
    }

    @Override
    public List<Booking> getAllPendingBookings() {
        return bookingRepository.getAllBookings().stream()
                .filter(lesson -> "pending".equalsIgnoreCase(lesson.getStatus()))
                .toList();
    }

    @Override
    public List<Booking> getLessonsByInstructorId(long instructorId) {
        return bookingRepository.getLessonsByInstructorId(instructorId);
    }

    @Override
    public List<Booking> getLessonsByStudentId(long studentId) {
        return bookingRepository.getLessonsByStudentId(studentId);
    }

    @Override
    public Booking confirmBooking(Long lessonId) {
        return bookingRepository.confirmBooking(lessonId);
    }
}
