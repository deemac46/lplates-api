package com.dmc.lplates.database.repository;

import com.dmc.lplates.inbound.models.Role;
import com.dmc.lplates.inbound.models.User;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryHandler implements UserRepository {

    private final String dbUrl;
    private static final String USERS_TABLE = "accounts_user";

    private final PasswordEncoder passwordEncoder;

    public UserRepositoryHandler(PasswordEncoder passwordEncoder,
                                 @Value("${app.database.url}") String dbUrl) {
        this.passwordEncoder = passwordEncoder;
        this.dbUrl = dbUrl;
    }

    @PostConstruct
    public void migrateSchema() {
        String ddl = """
            CREATE TABLE IF NOT EXISTS "accounts_user" (
                "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                "username" VARCHAR(150) NOT NULL UNIQUE,
                "first_name" VARCHAR(150) NOT NULL DEFAULT '',
                "last_name" VARCHAR(150) NOT NULL DEFAULT '',
                "email" VARCHAR(254) NOT NULL UNIQUE,
                "password" VARCHAR(255) NOT NULL,
                "role" VARCHAR(20) NOT NULL DEFAULT 'LEARNER',
                "active" INTEGER NOT NULL DEFAULT 1,
                "created_at" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(ddl)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DDL error (accounts_user): " + e.getMessage());
        }

        // Create default admin if no users exist
        if (getAllUsers().isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@lplates.ie");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            insertUser(admin);
            System.out.println("UserRepositoryHandler: created default admin (username=admin, password=admin123)");
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    @Override
    public User insertUser(User user) {
        String query = "INSERT INTO \"" + USERS_TABLE + "\" " +
                "(username, first_name, last_name, email, password, role, active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFirstName() != null ? user.getFirstName() : "");
            stmt.setString(3, user.getLastName() != null ? user.getLastName() : "");
            stmt.setString(4, user.getEmail() != null ? user.getEmail() : "");
            stmt.setString(5, user.getPassword());
            stmt.setString(6, user.getRole() != null ? user.getRole().name() : Role.LEARNER.name());
            stmt.setInt(7, Boolean.TRUE.equals(user.getActive()) ? 1 : 1);
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }

        return user;
    }

    @Override
    public User insertUserWithId(long id, User user) {
        User existing = findById(id);
        if (existing != null) {
            return existing;
        }

        String query = "INSERT INTO \"" + USERS_TABLE + "\" " +
                "(id, username, first_name, last_name, email, password, role, active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, id);
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFirstName() != null ? user.getFirstName() : "");
            stmt.setString(4, user.getLastName() != null ? user.getLastName() : "");
            stmt.setString(5, user.getEmail() != null ? user.getEmail() : "");
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getRole() != null ? user.getRole().name() : Role.LEARNER.name());
            stmt.setInt(8, Boolean.TRUE.equals(user.getActive()) ? 1 : 1);
            stmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

            stmt.executeUpdate();
            user.setId(id);
        } catch (SQLException e) {
            System.err.println("Error inserting user with explicit id " + id + ": " + e.getMessage());
        }

        return user;
    }

    @Override
    public User findByUsername(String username) {
        String query = "SELECT * FROM \"" + USERS_TABLE + "\" WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String query = "SELECT * FROM \"" + USERS_TABLE + "\" WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findById(long id) {
        String query = "SELECT * FROM \"" + USERS_TABLE + "\" WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (SQLException e) {
            System.err.println("Error finding user by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        String query = "SELECT * FROM \"" + USERS_TABLE + "\" ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) users.add(mapResultSetToUser(rs));
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getInt("active") == 1);
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
