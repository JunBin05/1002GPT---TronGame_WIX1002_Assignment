package UI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:user_data.db";
    private static final String TABLE_NAME = "users";

    private boolean driverLoaded = false;

    public DatabaseManager() {
        // 1. Attempt to load the SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
            driverLoaded = true; 
            System.out.println("INFO: SQLite driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("!!! CRITICAL DATABASE ERROR: SQLite JDBC driver not found. Check your classpath.");
            driverLoaded = false; 
        }
        
        // 2. Only create the table if the driver loaded
        if (driverLoaded) {
            createTable();
        }
    }

    /**
     * Checks if the database driver loaded successfully.
     */
    public boolean isReady() {
        return driverLoaded;
    }

    private Connection connect() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("Database driver failed to load, connection aborted.");
        }
        return DriverManager.getConnection(DB_URL);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\n"
                   + " ID TEXT PRIMARY KEY,\n"
                   + " PASSWORD_HASH INTEGER NOT NULL\n"
                   + ");";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.execute();
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    public boolean registerUser(String userId, String password) {
        if (!isReady() || userId == null || userId.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        int passwordHash = password.hashCode(); 
        String sql = "INSERT INTO " + TABLE_NAME + "(ID, PASSWORD_HASH) VALUES(?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId.trim());
            pstmt.setInt(2, passwordHash);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Assumes error code 19 (duplicate ID) or other failure
        }
    }
    
    /**
     * Checks if the provided user ID and password match a stored record.
     */
    public boolean checkLogin(String userId, String password) {
        if (!isReady() || userId == null || userId.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        int inputPasswordHash = password.hashCode();
        String sql = "SELECT PASSWORD_HASH FROM " + TABLE_NAME + " WHERE ID = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int storedPasswordHash = rs.getInt("PASSWORD_HASH");
                    return inputPasswordHash == storedPasswordHash;
                } else {
                    return false; // ID was not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Login check failed: " + e.getMessage());
            return false;
        }
    }
}