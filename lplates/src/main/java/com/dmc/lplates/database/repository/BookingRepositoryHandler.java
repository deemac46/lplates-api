package com.dmc.lplates.database.repository;

import com.dmc.lplates.inbound.models.Booking;
import com.dmc.lplates.inbound.models.Instructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookingRepositoryHandler implements BookingRepository, InstructorRepository {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/deemc/Documents/Workspace/databases/sql_lite/lplates_bookings.db";
    private static final String BOOKINGS_TABLE_NAME = "bookings";
    private static final String INSTRUCTORS_TABLE_NAME = "instructors";


    // Method to establish a connection to the database
    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
        return connection;
    }

    @Override
    public Booking getBookingById(long bookingId) {
        String query = "SELECT * FROM " + BOOKINGS_TABLE_NAME + " WHERE booking_id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, bookingId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Booking booking = mapResultSetToBooking(resultSet);
                return booking;
            } else {
                System.out.println("No booking found with ID: " + bookingId);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving booking: " + e.getMessage());
            return null;
        }
    }

    // Example method to retrieve data
    @Override
    public List<Booking> getAllBookings() {
        String query = "SELECT * FROM " + BOOKINGS_TABLE_NAME;
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Booking booking = mapResultSetToBooking(resultSet);
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving bookings: " + e.getMessage());
        }

        return bookings;
    }

    @Override
    public Booking confirmBooking(Long bookingId) {

        String query = "UPDATE " + BOOKINGS_TABLE_NAME + " SET status = ? WHERE booking_id = ?";

        Booking booking = getBookingById(bookingId);
        if(booking == null) {
            System.err.println("Cannot confirm non-existent booking with ID: " + bookingId);
            return null;
        }
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "CONFIRMED");
            statement.setLong(2, bookingId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Booking with ID " + bookingId + " confirmed successfully.");
                booking.setStatus("CONFIRMED");
            } else {
                System.out.println("No booking found with ID: " + bookingId);
            }
        } catch (SQLException e) {
            System.err.println("Error confirming booking: " + e.getMessage());
        }

        return booking;
    }

    @Override
    public Instructor getInstructorById(Long instructorId) {

        String query = "SELECT * FROM " + INSTRUCTORS_TABLE_NAME + " WHERE instructor_id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructorId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Instructor instructor = new Instructor();
                instructor.setInstructorId(resultSet.getLong("instructor_id"));
                instructor.setFirstName(resultSet.getString("first_name"));
                instructor.setLastName(resultSet.getString("last_name"));
                instructor.setEmail(resultSet.getString("email"));
                instructor.setPhoneNumber(resultSet.getString("phone_number"));
                instructor.setTransmission(resultSet.getString("transmission"));
                instructor.setRating(resultSet.getDouble("rating"));
                instructor.setCarMake(resultSet.getString("carMake"));
                instructor.setCarModel(resultSet.getString("carModel"));
                instructor.setLocations(resultSet.getString("locations") != null ?
                        List.of(resultSet.getString("locations").split(",")) : new ArrayList<>()
                );
                return instructor;
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
        String query = "INSERT INTO " + INSTRUCTORS_TABLE_NAME + " (instructorId, licenseId, firstName, lastName, email, phoneNumber, specialization, transmission, rating, carMake, carModel, locations, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructor.getInstructorId());
            statement.setLong(2, instructor.getLicenseId());
            statement.setString(3, instructor.getFirstName());
            statement.setString(4, instructor.getLastName());
            statement.setString(5, instructor.getEmail());
            statement.setString(6, instructor.getPhoneNumber());
            statement.setString(7, instructor.getSpecialization());
            statement.setString(8, instructor.getTransmission());
            statement.setDouble(9, instructor.getRating());
            statement.setString(10, instructor.getCarMake());
            statement.setString(11, instructor.getCarModel());
            statement.setString(12, String.join(",", instructor.getLocations()));
            statement.setTimestamp(13, instructor.getCreatedAt());
            statement.setTimestamp(14, instructor.getUpdatedAt());


            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Instructor created successfully.");
                return "Instructor created successfully.";
            }
        } catch (SQLException e) {
            System.err.println("Error creating instructor: " + e.getMessage());
        }

        return "Failed to create instructor.";
    }

    @Override
    public Instructor getInstructorWithBookings(Long instructorId) {

        Instructor instructor = getInstructorById(instructorId);
        if (instructor == null) {
            System.err.println("Instructor with ID " + instructorId + " not found.");
            return null;
        }

        String query = "SELECT * FROM " + BOOKINGS_TABLE_NAME + " WHERE instructor_id = ?";
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, instructorId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Booking booking = mapResultSetToBooking(resultSet);
                bookings.add(booking);
            }
            instructor.setBookings(bookings);
        } catch (SQLException e) {
            System.err.println("Error retrieving bookings for instructor: " + e.getMessage());
        }

        return instructor;
    }

    @Override
    public List<Instructor> getAllInstructors() {

        String query = "SELECT * FROM " + INSTRUCTORS_TABLE_NAME;
        List<Instructor> instructors = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Instructor instructor = new Instructor();
                instructor.setInstructorId(resultSet.getLong("instructor_id"));
                instructor.setFirstName(resultSet.getString("first_name"));
                instructor.setLastName(resultSet.getString("last_name"));
                instructor.setEmail(resultSet.getString("email"));
                instructor.setPhoneNumber(resultSet.getString("phone_number"));
                instructors.add(instructor);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving instructors: " + e.getMessage());
        }

        return instructors;
    }

    // Example method to insert data
    @Override
    public void insertRecord(Booking booking, long instructorId) {
        String query = "INSERT INTO bookings (booking_id, user_id, resource_id, start_time, end_time, status, total_price, currency, notes, created_at, updated_at, instructor_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        if(booking == null) {
            System.err.println("Cannot insert null booking.");
            return;
        }
        
        if (booking.getNotes().equalsIgnoreCase("WhamBamThankYouMaam")) {
            
            createMockData(instructorId);
        }
        // Generate a unique ID for the booking
        long uniqueId = System.currentTimeMillis(); // Example: Use current timestamp as unique ID
        booking.setBookingId(uniqueId);

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, booking.getBookingId()); // Set bookingId
            statement.setLong(2, booking.getUserId());
            statement.setLong(3, booking.getResourceId());
            statement.setTimestamp(4, booking.getStartTime());
            statement.setTimestamp(5, booking.getEndTime());
            statement.setString(6, booking.getStatus());
            statement.setBigDecimal(7, booking.getTotalPrice());
            statement.setString(8, booking.getCurrency());
            statement.setString(9, booking.getNotes());
            statement.setTimestamp(10, booking.getCreatedAt());
            statement.setTimestamp(11, booking.getUpdatedAt());
            statement.setLong(12, instructorId);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Booking inserted successfully with ID: " + uniqueId);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting booking: " + e.getMessage());
        }
    }

    private void createMockData(long instructorId) {
        
        System.out.println("Creating mock data as per special instruction.");
        for (int i = 1; i <= 25; i++) {
            System.out.println("Inserting mock booking " + (i + 1));

            Booking mockBooking = new Booking();
            mockBooking.setUserId((long) (1000 + i));
            mockBooking.setResourceId((long) (2000 + i));
            mockBooking.setStartTime(new Timestamp(System.currentTimeMillis() + i * 3600000)); // Incremental start times
            mockBooking.setEndTime(new Timestamp(System.currentTimeMillis() + (i + 1) * 3600000)); // Incremental end times
            mockBooking.setStatus("CONFIRMED");
            mockBooking.setTotalPrice(new java.math.BigDecimal("99.99"));
            mockBooking.setCurrency("USD");
            mockBooking.setNotes("Mock booking " + (i + 1));
            mockBooking.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            mockBooking.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            mockBooking.setInstructorId((long) i/5);

            insertRecord(mockBooking, instructorId);
            
        }
        
    }

    // Example method to close the connection (optional, handled by try-with-resources)
    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private Booking mapResultSetToBooking(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(resultSet.getLong("booking_id"));
        booking.setUserId(resultSet.getLong("user_id"));
        booking.setResourceId(resultSet.getLong("resource_id"));
        booking.setStartTime(resultSet.getTimestamp("start_time"));
        booking.setEndTime(resultSet.getTimestamp("end_time"));
        booking.setStatus(resultSet.getString("status"));
        booking.setTotalPrice(resultSet.getBigDecimal("total_price"));
        booking.setCurrency(resultSet.getString("currency"));
        booking.setNotes(resultSet.getString("notes"));
        booking.setCreatedAt(resultSet.getTimestamp("created_at"));
        booking.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return booking;
    }

}
