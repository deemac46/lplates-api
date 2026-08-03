package com.dmc.lplates.database.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.dmc.lplates.inbound.models.Booking;

class BookingRepositoryHandlerTest {

    @Test
    void insertRecordPersistsInstructorAndStudentIds() throws Exception {
        BookingRepositoryHandler repository = new BookingRepositoryHandler();
        repository.migrateSchema();

        Booking booking = new Booking();
        booking.setInstructorId(42L);
        booking.setStudentId(99L);
        booking.setScheduledDate(LocalDate.of(2026, 1, 15));
        booking.setScheduledTime(LocalTime.of(10, 30));
        booking.setStatus("pending");
        booking.setPaymentStatus("unpaid");
        booking.setPrice(new BigDecimal("15.00"));
        booking.setCurrency("EUR");
        booking.setLessonType("lesson");
        booking.setNotes("test booking");
        booking.setEdtModule("");
        booking.setCancellationReason("");
        booking.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        booking.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        repository.insertRecord(booking);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:/Users/deemc/Documents/Workspace/databases/sql_lite/lplates_bookings.db");
             PreparedStatement statement = connection.prepareStatement("SELECT instructor_id, student_id FROM bookings_lesson WHERE id = ?")) {
            statement.setLong(1, booking.getLessonId());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(42L, resultSet.getLong("instructor_id"));
                assertEquals(99L, resultSet.getLong("student_id"));
            }
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:C:/Users/deemc/Documents/Workspace/databases/sql_lite/lplates_bookings.db");
             PreparedStatement statement = connection.prepareStatement("DELETE FROM bookings_lesson WHERE id = ?")) {
            statement.setLong(1, booking.getLessonId());
            statement.executeUpdate();
        }
    }
}
