package UI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:user_data.db";
    private static final String TABLE_NAME = "users";

    private boolean driverLoaded = false;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            driverLoaded = true; 
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: SQLite JDBC driver not found.");
            driverLoaded = false; 
        }
        
        if (driverLoaded) {
            createTable();
        }
    }

    public boolean isReady() { return driverLoaded; }

    private Connection connect() throws SQLException {
        if (!driverLoaded) throw new SQLException("Driver failed.");
        return DriverManager.getConnection(DB_URL);
    }

    private void createTable() {
        // Updated SQL to include PROFILE_IMAGE
        // --- NEW: Added AC1 to AC6 columns for achievements (0=Locked, 1=Unlocked) ---
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\n"
                   + " ID TEXT PRIMARY KEY,\n"
                   + " PASSWORD_HASH INTEGER NOT NULL,\n"
                   + " PROFILE_IMAGE TEXT DEFAULT 'images/default_profile.png',\n"
                   + " AC1 INTEGER DEFAULT 0,\n" 
                   + " AC2 INTEGER DEFAULT 0,\n"
                   + " AC3 INTEGER DEFAULT 0,\n"
                   + " AC4 INTEGER DEFAULT 0,\n"
                   + " AC5 INTEGER DEFAULT 0,\n"
                   + " AC6 INTEGER DEFAULT 0\n" 
                   + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // 1. Create table if it doesn't exist
            stmt.execute(sql);

            // 2. CRITICAL: Attempt to add the column if the table ALREADY exists 
            // (This prevents errors if you already have users saved)
            try {
                stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN PROFILE_IMAGE TEXT DEFAULT 'images/default_profile.png'");
            } catch (SQLException ignored) {
                // Column likely already exists, ignore this error
            }

            // --- NEW: Safely add Achievement Columns if they are missing ---
            String[] achCols = {"AC1", "AC2", "AC3", "AC4", "AC5", "AC6"};
            for (String col : achCols) {
                try {
                    stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + col + " INTEGER DEFAULT 0");
                } catch (SQLException ignored) {
                    // Column likely exists
                }
            }
            // ---------------------------------------------------------------

        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    // --- NEW: GET IMAGE PATH ---
    public String getProfileImage(String userId) {
        String sql = "SELECT PROFILE_IMAGE FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String path = rs.getString("PROFILE_IMAGE");
                // If null, return default
                if (path == null || path.isEmpty()) return "images/default_profile.png";
                return path;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "images/default_profile.png"; 
    }

    // --- NEW: SAVE IMAGE PATH ---
    public void setProfileImage(String userId, String imagePath) {
        String sql = "UPDATE " + TABLE_NAME + " SET PROFILE_IMAGE = ? WHERE ID = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, imagePath);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
            System.out.println("Saved profile image for " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (Keep your existing registerUser and checkLogin methods exactly the same)
    public boolean registerUser(String userId, String password) {
        if (!isReady() || userId == null || userId.trim().isEmpty() || password == null) return false;
        int passwordHash = password.hashCode(); 
        // Note: New users automatically get the DEFAULT 'default_profile.png' from the table definition
        String sql = "INSERT INTO " + TABLE_NAME + "(ID, PASSWORD_HASH) VALUES(?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId.trim());
            pstmt.setInt(2, passwordHash);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }
    
    public boolean checkLogin(String userId, String password) {
        if (!isReady() || userId == null || password == null) return false;
        int inputPasswordHash = password.hashCode();
        String sql = "SELECT PASSWORD_HASH FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return inputPasswordHash == rs.getInt("PASSWORD_HASH");
                else return false;
            }
        } catch (SQLException e) { return false; }
    }

    // --- NEW ACHIEVEMENT METHODS ---

    /**
     * Helper method to unlock a specific achievement (1-6)
     */
    public void unlockAchievement(String userId, int achIndex) {
        if (achIndex < 1 || achIndex > 6) return; // Safety check
        
        String sql = "UPDATE " + TABLE_NAME + " SET AC" + achIndex + " = 1 WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            System.out.println("Unlocked AC" + achIndex + " for user " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of booleans: [true, false, true...]
     * Index 0 = AC1, Index 1 = AC2, etc.
     */
    public List<Boolean> getAchievements(String userId) {
        List<Boolean> statusList = new ArrayList<>();
        String sql = "SELECT AC1, AC2, AC3, AC4, AC5, AC6 FROM " + TABLE_NAME + " WHERE ID = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                for (int i = 1; i <= 6; i++) {
                    // getInt returns 1 if true, 0 if false
                    statusList.add(rs.getInt("AC" + i) == 1);
                }
            } else {
                // User not found? Default all to false
                for (int i = 0; i < 6; i++) statusList.add(false);
            }
        } catch (SQLException e) {
            // Error? Default all to false
            for (int i = 0; i < 6; i++) statusList.add(false);
        }
        return statusList;
    }
}