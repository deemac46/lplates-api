package com.dmc.lplates.database.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.EdtProgress;
import com.dmc.lplates.inbound.models.Feedback;
import com.dmc.lplates.inbound.models.Instructor;
import com.dmc.lplates.inbound.models.InstructorPricing;

import jakarta.annotation.PostConstruct;

@Repository
public class BookingRepositoryHandler implements BookingRepository, InstructorRepository,
        InstructorPricingRepository, FeedbackRepository, EdtProgressRepository {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/deemc/Documents/Workspace/databases/sql_lite/lplates_bookings.db";
    private static final String LESSONS_TABLE = "bookings_lesson";
    private static final String INSTRUCTORS_TABLE = "accounts_instructor";
    private static final String PRICING_TABLE = "bookings_instructorpricing";
    private static final String FEEDBACK_TABLE = "bookings_feedback";
    private static final String EDT_PROGRESS_TABLE = "bookings_edtprogress";

    @PostConstruct
    public void migrateSchema() {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS "accounts_instructor" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "first_name" VARCHAR(150) NOT NULL DEFAULT '',
                "last_name" VARCHAR(150) NOT NULL DEFAULT '',
                "email" VARCHAR(254) NOT NULL DEFAULT '',
                "approval_status" VARCHAR(20) NOT NULL DEFAULT 'pending',
                "gender" VARCHAR(20) NOT NULL DEFAULT '',
                "phone_number" VARCHAR(30) NOT NULL DEFAULT '',
                "adi_number" VARCHAR(80) NULL,
                "transmission" VARCHAR(32) NULL,
                "years_experience" INTEGER NULL,
                "rating" DECIMAL(3, 2) NULL,
                "car_make" VARCHAR(100) NULL,
                "car_model" VARCHAR(100) NULL,
                "locations" TEXT NULL,
                "profile_picture" VARCHAR(100) NULL,
                "description" TEXT NULL,
                "reviews_count" INTEGER NOT NULL DEFAULT 0,
                "agree_terms" INTEGER NOT NULL DEFAULT 0,
                "offers_test_car_hire" INTEGER NOT NULL DEFAULT 0,
                "test_car_hire_price" DECIMAL(6, 2) NULL,
                "created_at" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                "updated_at" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                "user_id" INTEGER NOT NULL DEFAULT 0,
                "api_instructor_id" INTEGER NULL UNIQUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS "bookings_lesson" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "scheduled_date" DATE NOT NULL,
                "scheduled_time" TIME NOT NULL,
                "duration_minutes" INTEGER NOT NULL DEFAULT 60,
                "status" VARCHAR(20) NOT NULL DEFAULT 'pending',
                "payment_status" VARCHAR(20) NOT NULL DEFAULT 'unpaid',
                "price" DECIMAL(6, 2) NOT NULL DEFAULT 0.00,
                "currency" VARCHAR(3) NOT NULL DEFAULT 'EUR',
                "lesson_type" VARCHAR(20) NOT NULL DEFAULT 'lesson',
                "notes" TEXT NOT NULL DEFAULT '',
                "edt_module" VARCHAR(10) NOT NULL DEFAULT '',
                "edt_completed" INTEGER NOT NULL DEFAULT 0,
                "cancellation_reason" TEXT NOT NULL DEFAULT '',
                "created_at" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                "updated_at" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                "instructor_id" INTEGER NULL,
                "student_id" INTEGER NOT NULL DEFAULT 0
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS "bookings_instructorpricing" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "duration_minutes" INTEGER NOT NULL,
                "price" DECIMAL(6, 2) NOT NULL,
                "instructor_id" INTEGER NOT NULL,
                "api_pricing_id" INTEGER NULL UNIQUE,
                UNIQUE ("instructor_id", "duration_minutes")
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS "bookings_feedback" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "rating" INTEGER NOT NULL,
                "comment" TEXT NOT NULL DEFAULT '',
                "created_at" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                "author_id" INTEGER NOT NULL,
                "lesson_id" INTEGER NOT NULL UNIQUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS "bookings_edtprogress" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "student_id" INTEGER NOT NULL,
                "module_number" INTEGER NOT NULL,
                "module_name" VARCHAR(100) NOT NULL DEFAULT '',
                "completed" INTEGER NOT NULL DEFAULT 0,
                "lesson_id" INTEGER NULL,
                "completed_at" DATETIME NULL,
                UNIQUE ("student_id", "module_number")
            )
            """
        };

        try (Connection connection = connect()) {
            for (String sql : ddl) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("DDL error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Schema migration failed: " + e.getMessage());
        }
    }

    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
        return connection;
    }

    // =========================================================================
    // BookingRepository (Lessons)
    // =========================================================================

    @Override
    public void insertRecord(Booking lesson) {
        String query = "INSERT INTO \"" + LESSONS_TABLE + "\" " +
                "(scheduled_date, scheduled_time, duration_minutes, status, payment_status, " +
                "price, currency, lesson_type, notes, edt_module, edt_completed, cancellation_reason, " +
                "created_at, updated_at, instructor_id, student_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            java.time.LocalDate scheduledDate = lesson.getScheduledDate() != null ? lesson.getScheduledDate() : java.time.LocalDate.now();
            java.time.LocalTime scheduledTime = lesson.getScheduledTime() != null ? lesson.getScheduledTime() : java.time.LocalTime.MIDNIGHT;
            statement.setDate(1, Date.valueOf(scheduledDate));
            statement.setTime(2, Time.valueOf(scheduledTime));
            statement.setInt(3, lesson.getDurationMinutes() > 0 ? lesson.getDurationMinutes() : 60);
            statement.setString(4, lesson.getStatus() != null ? lesson.getStatus() : "pending");
            statement.setString(5, lesson.getPaymentStatus() != null ? lesson.getPaymentStatus() : "unpaid");
            statement.setBigDecimal(6, lesson.getPrice() != null ? lesson.getPrice() : java.math.BigDecimal.ZERO);
            statement.setString(7, lesson.getCurrency() != null ? lesson.getCurrency() : "EUR");
            statement.setString(8, lesson.getLessonType() != null ? lesson.getLessonType() : "lesson");
            statement.setString(9, lesson.getNotes() != null ? lesson.getNotes() : "");
            statement.setString(10, lesson.getEdtModule() != null ? lesson.getEdtModule() : "");
            statement.setInt(11, Boolean.TRUE.equals(lesson.getEdtCompleted()) ? 1 : 0);
            statement.setString(12, lesson.getCancellationReason() != null ? lesson.getCancellationReason() : "");
            statement.setTimestamp(13, lesson.getCreatedAt() != null ? lesson.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            statement.setTimestamp(14, lesson.getUpdatedAt() != null ? lesson.getUpdatedAt() : new Timestamp(System.currentTimeMillis()));
            if (lesson.getInstructorId() != null) {
                statement.setLong(15, lesson.getInstructorId());
            } else {
                statement.setNull(15, Types.INTEGER);
            }
            if (lesson.getStudentId() != null) {
                statement.setLong(16, lesson.getStudentId());
            } else {
                statement.setLong(16, 0L);
            }

            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    lesson.setLessonId(generatedKeys.getLong(1));
                }
            }
            System.out.println("Lesson inserted successfully with ID: " + lesson.getLessonId());
        } catch (SQLException e) {
            System.err.println("Error inserting lesson: " + e.getMessage());
        }
    }

    @Override
    public Booking getBookingById(long lessonId) {
        String query = "SELECT * FROM \"" + LESSONS_TABLE + "\" WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, lessonId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToLesson(resultSet);
            } else {
                System.out.println("No lesson found with ID: " + lessonId);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lesson: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Booking> getAllBookings() {
        String query = "SELECT * FROM \"" + LESSONS_TABLE + "\"";
        List<Booking> lessons = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                lessons.add(mapResultSetToLesson(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lessons: " + e.getMessage());
        }

        return lessons;
    }

    @Override
    public List<Booking> getLessonsByInstructorId(long instructorId) {
        String query = "SELECT * FROM \"" + LESSONS_TABLE + "\" WHERE instructor_id = ?";
        List<Booking> lessons = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructorId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                lessons.add(mapResultSetToLesson(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lessons for instructor: " + e.getMessage());
        }

        return lessons;
    }

    @Override
    public List<Booking> getLessonsByStudentId(long studentId) {
        String query = "SELECT * FROM \"" + LESSONS_TABLE + "\" WHERE student_id = ?";
        List<Booking> lessons = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, studentId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                lessons.add(mapResultSetToLesson(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lessons for student: " + e.getMessage());
        }

        return lessons;
    }

    @Override
    public Booking confirmBooking(Long lessonId) {
        String query = "UPDATE \"" + LESSONS_TABLE + "\" SET status = ? WHERE id = ?";
        Booking lesson = getBookingById(lessonId);
        if (lesson == null) {
            System.err.println("Cannot confirm non-existent lesson with ID: " + lessonId);
            return null;
        }
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "confirmed");
            statement.setLong(2, lessonId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                lesson.setStatus("confirmed");
            }
        } catch (SQLException e) {
            System.err.println("Error confirming lesson: " + e.getMessage());
        }
        return lesson;
    }

    @Override
    public Booking updateBooking(Booking lesson) {
        String query = "UPDATE \"" + LESSONS_TABLE + "\" SET " +
                "scheduled_date = ?, scheduled_time = ?, duration_minutes = ?, " +
                "status = ?, payment_status = ?, price = ?, currency = ?, lesson_type = ?, " +
                "notes = ?, edt_module = ?, edt_completed = ?, cancellation_reason = ?, updated_at = ?, " +
                "instructor_id = ?, student_id = ? " +
                "WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, lesson.getScheduledDate() != null ? Date.valueOf(lesson.getScheduledDate()) : null);
            statement.setTime(2, lesson.getScheduledTime() != null ? Time.valueOf(lesson.getScheduledTime()) : null);
            statement.setInt(3, lesson.getDurationMinutes());
            statement.setString(4, lesson.getStatus());
            statement.setString(5, lesson.getPaymentStatus());
            statement.setBigDecimal(6, lesson.getPrice() != null ? lesson.getPrice() : java.math.BigDecimal.ZERO);
            statement.setString(7, lesson.getCurrency());
            statement.setString(8, lesson.getLessonType());
            statement.setString(9, lesson.getNotes());
            statement.setString(10, lesson.getEdtModule());
            statement.setInt(11, Boolean.TRUE.equals(lesson.getEdtCompleted()) ? 1 : 0);
            statement.setString(12, lesson.getCancellationReason());
            statement.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            statement.setObject(14, lesson.getInstructorId());
            statement.setLong(15, lesson.getStudentId() != null ? lesson.getStudentId() : 0);
            statement.setLong(16, lesson.getLessonId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                return getBookingById(lesson.getLessonId());
            } else {
                System.err.println("No lesson found with ID: " + lesson.getLessonId());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error updating lesson: " + e.getMessage());
            return null;
        }
    }

    private Booking mapResultSetToLesson(ResultSet rs) throws SQLException {
        Booking lesson = new Booking();
        lesson.setLessonId(rs.getLong("id"));
        Date scheduledDate = rs.getDate("scheduled_date");
        if (scheduledDate != null) lesson.setScheduledDate(scheduledDate.toLocalDate());
        Time scheduledTime = rs.getTime("scheduled_time");
        if (scheduledTime != null) lesson.setScheduledTime(scheduledTime.toLocalTime());
        lesson.setDurationMinutes(rs.getInt("duration_minutes"));
        lesson.setStatus(rs.getString("status"));
        lesson.setPaymentStatus(rs.getString("payment_status"));
        lesson.setPrice(rs.getBigDecimal("price"));
        lesson.setCurrency(rs.getString("currency"));
        lesson.setLessonType(rs.getString("lesson_type"));
        lesson.setNotes(rs.getString("notes"));
        lesson.setEdtModule(rs.getString("edt_module"));
        lesson.setEdtCompleted(rs.getInt("edt_completed") == 1);
        lesson.setCancellationReason(rs.getString("cancellation_reason"));
        lesson.setCreatedAt(rs.getTimestamp("created_at"));
        lesson.setUpdatedAt(rs.getTimestamp("updated_at"));
        lesson.setInstructorId(getNullableLong(rs, "instructor_id"));
        lesson.setStudentId(getNullableLong(rs, "student_id"));
        return lesson;
    }

    private Long getNullableLong(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.parseLong(text);
    }

    // =========================================================================
    // InstructorRepository
    // =========================================================================

    @Override
    public List<Instructor> getAllInstructors() {
        String query = "SELECT * FROM \"" + INSTRUCTORS_TABLE + "\"";
        List<Instructor> instructors = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                instructors.add(mapResultSetToInstructor(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving instructors: " + e.getMessage());
        }

        return instructors;
    }

    @Override
    public Instructor getInstructorById(Long instructorId) {
        String query = "SELECT * FROM \"" + INSTRUCTORS_TABLE + "\" WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructorId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToInstructor(resultSet);
            } else {
                System.out.println("No instructor found with ID: " + instructorId);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving instructor: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String createInstructor(Instructor instructor) {
        String query = "INSERT INTO \"" + INSTRUCTORS_TABLE + "\" " +
                "(first_name, last_name, email, approval_status, gender, phone_number, adi_number, transmission, years_experience, " +
                "rating, car_make, car_model, locations, profile_picture, description, reviews_count, " +
                "agree_terms, offers_test_car_hire, test_car_hire_price, created_at, updated_at, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, instructor.getFirstName() != null ? instructor.getFirstName() : "");
            statement.setString(2, instructor.getLastName() != null ? instructor.getLastName() : "");
            statement.setString(3, instructor.getEmail() != null ? instructor.getEmail() : "");
            statement.setString(4, instructor.getApprovalStatus() != null ? instructor.getApprovalStatus() : "pending");
            statement.setString(5, instructor.getGender() != null ? instructor.getGender() : "");
            statement.setString(6, instructor.getPhoneNumber() != null ? instructor.getPhoneNumber() : "");
            statement.setString(7, instructor.getAdiNumber());
            statement.setString(8, instructor.getTransmission());
            statement.setObject(9, instructor.getYearsExperience());
            statement.setObject(10, instructor.getRating());
            statement.setString(11, instructor.getCarMake());
            statement.setString(12, instructor.getCarModel());
            statement.setString(13, instructor.getLocations() != null ? String.join(",", instructor.getLocations()) : null);
            statement.setString(14, instructor.getProfilePicture());
            statement.setString(15, instructor.getDescription());
            statement.setInt(16, instructor.getReviewsCount() != null ? instructor.getReviewsCount().intValue() : 0);
            statement.setInt(17, Boolean.TRUE.equals(instructor.getAgreeTerms()) ? 1 : 0);
            statement.setInt(18, Boolean.TRUE.equals(instructor.getOffersTestCarHire()) ? 1 : 0);
            statement.setBigDecimal(19, instructor.getTestCarHirePrice());
            statement.setTimestamp(20, instructor.getCreatedAt() != null ? instructor.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            statement.setTimestamp(21, instructor.getUpdatedAt() != null ? instructor.getUpdatedAt() : new Timestamp(System.currentTimeMillis()));
            statement.setLong(22, instructor.getUserId() != null ? instructor.getUserId().longValue() : 0L);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        instructor.setInstructorId(generatedKeys.getLong(1));
                    }
                }
                System.out.println("Instructor created successfully with ID: " + instructor.getInstructorId());
                return "Instructor created successfully.";
            }
        } catch (SQLException e) {
            System.err.println("Error creating instructor: " + e.getMessage());
        }

        return "Failed to create instructor.";
    }

    @Override
    public Instructor getInstructorWithLessons(Long instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        if (instructor == null) {
            System.err.println("Instructor with ID " + instructorId + " not found.");
            return null;
        }
        instructor.setLessons(getLessonsByInstructorId(instructorId));
        return instructor;
    }

    @Override
    public Instructor getInstructorByUserId(Long userId) {
        String query = "SELECT * FROM \"" + INSTRUCTORS_TABLE + "\" WHERE user_id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return mapResultSetToInstructor(rs);
        } catch (SQLException e) {
            System.err.println("Error retrieving instructor by userId: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Instructor> getPendingInstructors() {
        String query = "SELECT * FROM \"" + INSTRUCTORS_TABLE + "\" WHERE approval_status = ?";
        List<Instructor> instructors = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "pending");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                instructors.add(mapResultSetToInstructor(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pending instructors: " + e.getMessage());
        }

        return instructors;
    }

    @Override
    public Instructor updateApprovalStatus(Long instructorId, String approvalStatus) {
        String query = "UPDATE \"" + INSTRUCTORS_TABLE + "\" SET approval_status = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, approvalStatus);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setLong(3, instructorId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                return getInstructorById(instructorId);
            }
            System.err.println("No instructor found with ID: " + instructorId);
            return null;
        } catch (SQLException e) {
            System.err.println("Error updating approval status: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Instructor updateProfilePicture(Long instructorId, String profilePicture) {
        String query = "UPDATE \"" + INSTRUCTORS_TABLE + "\" SET profile_picture = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, profilePicture);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setLong(3, instructorId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                return getInstructorById(instructorId);
            }
            System.err.println("No instructor found with ID: " + instructorId);
            return null;
        } catch (SQLException e) {
            System.err.println("Error updating profile picture: " + e.getMessage());
            return null;
        }
    }

    private Instructor mapResultSetToInstructor(ResultSet rs) throws SQLException {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(rs.getLong("id"));
        instructor.setUserId(rs.getObject("user_id", Long.class));
        instructor.setFirstName(rs.getString("first_name"));
        instructor.setLastName(rs.getString("last_name"));
        instructor.setEmail(rs.getString("email"));
        instructor.setApprovalStatus(rs.getString("approval_status"));
        instructor.setGender(rs.getString("gender"));
        instructor.setPhoneNumber(rs.getString("phone_number"));
        instructor.setAdiNumber(rs.getString("adi_number"));
        instructor.setTransmission(rs.getString("transmission"));
        instructor.setYearsExperience(rs.getObject("years_experience", Integer.class));
        instructor.setRating(rs.getObject("rating", Double.class));
        instructor.setCarMake(rs.getString("car_make"));
        instructor.setCarModel(rs.getString("car_model"));
        String locations = rs.getString("locations");
        instructor.setLocations(locations != null ? List.of(locations.split(",")) : new ArrayList<>());
        instructor.setProfilePicture(rs.getString("profile_picture"));
        instructor.setDescription(rs.getString("description"));
        instructor.setReviewsCount(rs.getInt("reviews_count"));
        instructor.setAgreeTerms(rs.getInt("agree_terms") == 1);
        instructor.setOffersTestCarHire(rs.getInt("offers_test_car_hire") == 1);
        instructor.setTestCarHirePrice(rs.getBigDecimal("test_car_hire_price"));
        instructor.setCreatedAt(rs.getTimestamp("created_at"));
        instructor.setUpdatedAt(rs.getTimestamp("updated_at"));
        return instructor;
    }

    // =========================================================================
    // InstructorPricingRepository
    // =========================================================================

    @Override
    public InstructorPricing insertPricing(InstructorPricing pricing) {
        String query = "INSERT INTO \"" + PRICING_TABLE + "\" (duration_minutes, price, instructor_id) " +
                "VALUES (?, ?, ?) ON CONFLICT(instructor_id, duration_minutes) DO UPDATE SET price = excluded.price";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, pricing.getDurationMinutes());
            statement.setBigDecimal(2, pricing.getPrice());
            statement.setLong(3, pricing.getInstructorId());

            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pricing.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting instructor pricing: " + e.getMessage());
        }

        return pricing;
    }

    @Override
    public InstructorPricing getPricingById(long pricingId) {
        String query = "SELECT * FROM \"" + PRICING_TABLE + "\" WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, pricingId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapResultSetToPricing(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pricing: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<InstructorPricing> getPricingByInstructorId(long instructorId) {
        String query = "SELECT * FROM \"" + PRICING_TABLE + "\" WHERE instructor_id = ?";
        List<InstructorPricing> pricingList = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructorId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                pricingList.add(mapResultSetToPricing(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pricing for instructor: " + e.getMessage());
        }

        return pricingList;
    }

    @Override
    public void deletePricing(long pricingId) {
        String query = "DELETE FROM \"" + PRICING_TABLE + "\" WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, pricingId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting pricing: " + e.getMessage());
        }
    }

    private InstructorPricing mapResultSetToPricing(ResultSet rs) throws SQLException {
        InstructorPricing pricing = new InstructorPricing();
        pricing.setId(rs.getLong("id"));
        pricing.setDurationMinutes(rs.getInt("duration_minutes"));
        pricing.setPrice(rs.getBigDecimal("price"));
        pricing.setInstructorId(rs.getLong("instructor_id"));
        return pricing;
    }

    // =========================================================================
    // FeedbackRepository
    // =========================================================================

    @Override
    public Feedback insertFeedback(Feedback feedback) {
        String query = "INSERT INTO \"" + FEEDBACK_TABLE + "\" (rating, comment, created_at, author_id, lesson_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, feedback.getRating());
            statement.setString(2, feedback.getComment() != null ? feedback.getComment() : "");
            statement.setTimestamp(3, feedback.getCreatedAt() != null ? feedback.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            statement.setLong(4, feedback.getAuthorId());
            statement.setLong(5, feedback.getLessonId());

            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting feedback: " + e.getMessage());
        }

        return feedback;
    }

    @Override
    public Feedback getFeedbackById(long feedbackId) {
        String query = "SELECT * FROM \"" + FEEDBACK_TABLE + "\" WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, feedbackId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapResultSetToFeedback(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feedback: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Feedback getFeedbackByLessonId(long lessonId) {
        String query = "SELECT * FROM \"" + FEEDBACK_TABLE + "\" WHERE lesson_id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, lessonId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return mapResultSetToFeedback(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feedback for lesson: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Feedback> getFeedbackByInstructorId(long instructorId) {
        String query = "SELECT f.* FROM \"" + FEEDBACK_TABLE + "\" f " +
                "JOIN \"" + LESSONS_TABLE + "\" l ON f.lesson_id = l.id " +
                "WHERE l.instructor_id = ?";
        List<Feedback> feedbackList = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructorId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                feedbackList.add(mapResultSetToFeedback(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feedback for instructor: " + e.getMessage());
        }

        return feedbackList;
    }

    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setId(rs.getLong("id"));
        feedback.setRating(rs.getInt("rating"));
        feedback.setComment(rs.getString("comment"));
        feedback.setCreatedAt(rs.getTimestamp("created_at"));
        feedback.setAuthorId(rs.getLong("author_id"));
        feedback.setLessonId(rs.getLong("lesson_id"));
        return feedback;
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    // =========================================================================
    // EdtProgressRepository
    // =========================================================================

    @Override
    public EdtProgress insertEdtProgress(EdtProgress progress) {
        String query = "INSERT INTO \"" + EDT_PROGRESS_TABLE + "\" " +
                "(student_id, module_number, module_name, completed, lesson_id, completed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(student_id, module_number) DO UPDATE SET " +
                "completed = excluded.completed, lesson_id = excluded.lesson_id, completed_at = excluded.completed_at";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, progress.getStudentId());
            statement.setInt(2, progress.getModuleNumber());
            statement.setString(3, progress.getModuleName() != null ? progress.getModuleName() : "");
            statement.setInt(4, Boolean.TRUE.equals(progress.getCompleted()) ? 1 : 0);
            statement.setObject(5, progress.getLessonId());
            statement.setTimestamp(6, progress.getCompletedAt());

            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    progress.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting EDT progress: " + e.getMessage());
        }

        return progress;
    }

    @Override
    public EdtProgress getEdtProgressById(long id) {
        String query = "SELECT * FROM \"" + EDT_PROGRESS_TABLE + "\" WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return mapResultSetToEdtProgress(rs);
        } catch (SQLException e) {
            System.err.println("Error retrieving EDT progress: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<EdtProgress> getEdtProgressByStudentId(long studentId) {
        String query = "SELECT * FROM \"" + EDT_PROGRESS_TABLE + "\" WHERE student_id = ? ORDER BY module_number";
        List<EdtProgress> list = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, studentId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) list.add(mapResultSetToEdtProgress(rs));
        } catch (SQLException e) {
            System.err.println("Error retrieving EDT progress for student: " + e.getMessage());
        }

        return list;
    }

    @Override
    public EdtProgress getEdtProgressByStudentAndModule(long studentId, int moduleNumber) {
        String query = "SELECT * FROM \"" + EDT_PROGRESS_TABLE + "\" WHERE student_id = ? AND module_number = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, studentId);
            statement.setInt(2, moduleNumber);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return mapResultSetToEdtProgress(rs);
        } catch (SQLException e) {
            System.err.println("Error retrieving EDT progress by module: " + e.getMessage());
        }
        return null;
    }

    @Override
    public EdtProgress markModuleCompleted(long studentId, int moduleNumber, long lessonId) {
        String query = "UPDATE \"" + EDT_PROGRESS_TABLE + "\" SET completed = 1, lesson_id = ?, completed_at = ? " +
                "WHERE student_id = ? AND module_number = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, lessonId);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setLong(3, studentId);
            statement.setInt(4, moduleNumber);

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking EDT module completed: " + e.getMessage());
        }

        return getEdtProgressByStudentAndModule(studentId, moduleNumber);
    }

    @Override
    public List<EdtProgress> getAllEdtProgress() {
        String query = "SELECT * FROM \"" + EDT_PROGRESS_TABLE + "\" ORDER BY student_id, module_number";
        List<EdtProgress> list = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToEdtProgress(rs));
        } catch (SQLException e) {
            System.err.println("Error retrieving all EDT progress: " + e.getMessage());
        }

        return list;
    }

    private EdtProgress mapResultSetToEdtProgress(ResultSet rs) throws SQLException {
        EdtProgress p = new EdtProgress();
        p.setId(rs.getLong("id"));
        p.setStudentId(rs.getLong("student_id"));
        p.setModuleNumber(rs.getInt("module_number"));
        p.setModuleName(rs.getString("module_name"));
        p.setCompleted(rs.getInt("completed") == 1);
        p.setLessonId(rs.getObject("lesson_id", Long.class));
        p.setCompletedAt(rs.getTimestamp("completed_at"));
        return p;
    }
}
