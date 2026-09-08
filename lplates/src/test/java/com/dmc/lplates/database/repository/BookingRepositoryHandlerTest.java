package com.dmc.lplates.database.repository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;

class BookingRepositoryHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void insertRecordPersistsInstructorAndStudentIds() throws Exception {
        String databaseUrl = "jdbc:sqlite:" + tempDir.resolve("repository-test.db");
        BookingRepositoryHandler repository = new BookingRepositoryHandler(databaseUrl);
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

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement("SELECT instructor_id, student_id FROM bookings_lesson WHERE id = ?")) {
            statement.setLong(1, booking.getLessonId());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(42L, resultSet.getLong("instructor_id"));
                assertEquals(99L, resultSet.getLong("student_id"));
            }
        }

    }

    @Test
    void completeBookingMarksLessonAndUpsertsEdtProvenance() throws Exception {
        String databaseUrl = "jdbc:sqlite:" + tempDir.resolve("complete-lesson-test.db");
        BookingRepositoryHandler repository = new BookingRepositoryHandler(databaseUrl);
        repository.migrateSchema();

        Booking booking = new Booking();
        booking.setInstructorId(42L);
        booking.setStudentId(99L);
        booking.setScheduledDate(LocalDate.of(2026, 1, 15));
        booking.setScheduledTime(LocalTime.of(10, 30));
        booking.setStatus("confirmed");
        booking.setPaymentStatus("paid");
        booking.setPrice(new BigDecimal("55.00"));
        booking.setCurrency("EUR");
        booking.setLessonType("edt");
        booking.setNotes("test booking");
        booking.setEdtModule("");
        booking.setEdtCompleted(false);
        booking.setCancellationReason("");
        booking.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        booking.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        repository.insertRecord(booking);

        Booking completed = repository.completeBooking(booking, 3, "Covered changing direction.", 42L);

        assertNotNull(completed);
        assertEquals("completed", completed.getStatus());
        assertEquals("edt_03", completed.getEdtModule());
        assertTrue(completed.getEdtCompleted());
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT completed, lesson_id, note, logged_by_instructor_id, logged_at FROM bookings_edtprogress " +
                             "WHERE student_id = ? AND module_number = ?")) {
            statement.setLong(1, 99L);
            statement.setInt(2, 3);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt("completed"));
                assertEquals(booking.getLessonId(), resultSet.getLong("lesson_id"));
                assertEquals("Covered changing direction.", resultSet.getString("note"));
                assertEquals(42L, resultSet.getLong("logged_by_instructor_id"));
                assertNotNull(resultSet.getTimestamp("logged_at"));
            }
        }
    }

    @Test
    void availabilityDefaultsToTrueAndCanBeToggled() {
        String databaseUrl = "jdbc:sqlite:" + tempDir.resolve("availability-test.db");
        BookingRepositoryHandler repository = new BookingRepositoryHandler(databaseUrl);
        repository.migrateSchema();

        Instructor instructor = new Instructor();
        instructor.setUserId(42L);
        instructor.setFirstName("Ava");
        instructor.setLastName("Able");
        instructor.setCounty("Dublin");
        instructor.setAreasCovered("");
        instructor.setAdaptedVehicleTypes("");
        instructor.setDisabilityExperience("");
        Long instructorId = repository.createInstructor(instructor);
        assertNotNull(instructorId);
        assertTrue(repository.getInstructorById(instructorId).getAvailable());
        assertEquals(1, repository.getAvailableInstructors().size());

        Instructor updated = repository.updateAvailability(instructorId, false);
        assertNotNull(updated);
        assertFalse(updated.getAvailable());
        assertFalse(repository.getInstructorById(instructorId).getAvailable());
        assertTrue(repository.getAvailableInstructors().isEmpty());
    }
}
