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

    // Use a busy timeout (ms) to wait for locks instead of immediately failing
    private static final String DB_URL = "jdbc:sqlite:user_data.db?busy_timeout=5000";
    private static final String TABLE_NAME = "users";

    // Global write lock across instances to serialize concurrent writers (reduces SQLITE_BUSY)
    private static final Object WRITE_LOCK = new Object();

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
            // Try to enable WAL journal mode to reduce locking contention
            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
            } catch (SQLException e) {
                System.err.println("Warning: Failed to set WAL mode: " + e.getMessage());
            }
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
                   + " AC6 INTEGER DEFAULT 0,\n"
                   + " HIGHEST_CHAPTER INTEGER DEFAULT 0,\n"
                   + " HIGHEST_SCORE INTEGER DEFAULT 0,\n"
                   + " TRON_LEVEL INTEGER DEFAULT 0,\n"
                   + " KEVIN_LEVEL INTEGER DEFAULT 0,\n"
                   + " TRON_XP INTEGER DEFAULT 0,\n"
                   + " KEVIN_XP INTEGER DEFAULT 0,\n"
                   + " LAST_COMPLETED TEXT DEFAULT NULL\n"
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
            // --- New: Add tracking columns if missing ---
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN HIGHEST_CHAPTER INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN HIGHEST_SCORE INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN TRON_LEVEL INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN KEVIN_LEVEL INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN TRON_XP INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN KEVIN_XP INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN LAST_COMPLETED TEXT DEFAULT NULL"); } catch (SQLException ignored) {}
            // --- NEW: Per-chapter resume columns (C1_STAGE .. C5_STAGE) ---
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN C1_STAGE INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN C2_STAGE INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN C3_STAGE INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN C4_STAGE INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN C5_STAGE INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
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
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                try (Connection conn = connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, imagePath);
                    pstmt.setString(2, userId);
                    pstmt.executeUpdate();
                    System.out.println("Saved profile image for " + userId);
                    break;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    // (Keep your existing registerUser and checkLogin methods exactly the same)
    public boolean registerUser(String userId, String password) {
        if (!isReady() || userId == null || userId.trim().isEmpty() || password == null) return false;
        int passwordHash = password.hashCode(); 
        // Note: New users automatically get the DEFAULT 'default_profile.png' from the table definition
        String sql = "INSERT INTO " + TABLE_NAME + "(ID, PASSWORD_HASH) VALUES(?,?)";
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userId.trim());
                    pstmt.setInt(2, passwordHash);
                    pstmt.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    return false;
                }
            }
        }
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
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userId);
                    pstmt.executeUpdate();
                    System.out.println("Unlocked AC" + achIndex + " for user " + userId);
                    break;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    e.printStackTrace();
                    break;
                }
            }
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

    // --- New: completion tracking ---
    public void updateCompletion(String userId, int chapter, long score, String completionDate, int tronLevel, int kevinLevel) {
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                String select = "SELECT HIGHEST_CHAPTER, HIGHEST_SCORE, TRON_LEVEL, KEVIN_LEVEL FROM " + TABLE_NAME + " WHERE ID = ?";
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(select)) {
                    pstmt.setString(1, userId);
                    ResultSet rs = pstmt.executeQuery();
                    int prevChapter = 0; long prevScore = 0; int prevTron = 0; int prevKevin = 0;
                    if (rs.next()) {
                        prevChapter = rs.getInt("HIGHEST_CHAPTER");
                        prevScore = rs.getLong("HIGHEST_SCORE");
                        prevTron = rs.getInt("TRON_LEVEL");
                        prevKevin = rs.getInt("KEVIN_LEVEL");
                    } else {
                        // No such user; nothing to update
                        return;
                    }

                    int newChapter = Math.max(prevChapter, chapter);
                    long newScore = Math.max(prevScore, score);
                    int newTron = Math.max(prevTron, tronLevel);
                    int newKevin = Math.max(prevKevin, kevinLevel);
                    String update = "UPDATE " + TABLE_NAME + " SET HIGHEST_CHAPTER = ?, HIGHEST_SCORE = ?, LAST_COMPLETED = ?, TRON_LEVEL = ?, KEVIN_LEVEL = ? WHERE ID = ?";
                    try (PreparedStatement up = conn.prepareStatement(update)) {
                        up.setInt(1, newChapter);
                        up.setLong(2, newScore);
                        up.setString(3, completionDate);
                        up.setInt(4, newTron);
                        up.setInt(5, newKevin);
                        up.setString(6, userId);
                        up.executeUpdate();
                        System.out.println("Updated completion for " + userId + ": chapter=" + newChapter + ", score=" + newScore + ", date=" + completionDate + ", tron=" + newTron + ", kevin=" + newKevin);
                    }
                    break;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    // --- NEW: LAST PLAYED PROGRESS (consolidated) ---




    /**
     * Set the stage for a specific chapter (1..5)
     */
    public void setChapterStage(String userId, int chapter, int stage) {
        if (chapter < 1 || chapter > 5) return; // safety
        String col = "C" + chapter + "_STAGE";
        String sql = "UPDATE " + TABLE_NAME + " SET " + col + " = ? WHERE ID = ?";
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, stage);
                    pstmt.setString(2, userId);
                    int updated = pstmt.executeUpdate();
                    if (updated == 0) {
                        System.out.println("Warning: setChapterStage - user not found: " + userId);
                    } else {
                        System.out.println("[DB] setChapterStage(" + userId + ", C" + chapter + ", " + stage + ")");
                    }
                    break;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    /**
     * Get the saved stage for a specific chapter (1..5)
     */
    public int getChapterStage(String userId, int chapter) {
        if (chapter < 1 || chapter > 5) return 0;
        String col = "C" + chapter + "_STAGE";
        String sql = "SELECT " + col + " FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(col);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // NOTE: Legacy LAST_PLAYED_* columns were removed in favor of per-chapter Cx_STAGE columns.

    public int getHighestChapter(String userId) {
        String sql = "SELECT HIGHEST_CHAPTER FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("HIGHEST_CHAPTER");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long getHighestScore(String userId) {
        String sql = "SELECT HIGHEST_SCORE FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getLong("HIGHEST_SCORE");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTronLevel(String userId) {
        String sql = "SELECT TRON_LEVEL FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("TRON_LEVEL");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getKevinLevel(String userId) {
        String sql = "SELECT KEVIN_LEVEL FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("KEVIN_LEVEL");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long getTronXp(String userId) {
        String sql = "SELECT TRON_XP FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long v = rs.getLong("TRON_XP");
                System.out.println("[DB READ] getTronXp(user=" + userId + ") = " + v);
                return v;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        System.out.println("[DB READ] getTronXp(user=" + userId + ") = 0 (missing or error)");
        return 0L;
    }

    public long getKevinXp(String userId) {
        String sql = "SELECT KEVIN_XP FROM " + TABLE_NAME + " WHERE ID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long v = rs.getLong("KEVIN_XP");
                System.out.println("[DB READ] getKevinXp(user=" + userId + ") = " + v);
                return v;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        System.out.println("[DB READ] getKevinXp(user=" + userId + ") = 0 (missing or error)");
        return 0L;
    }

    public void setTronXp(String userId, long xp) {
        String sql = "UPDATE " + TABLE_NAME + " SET TRON_XP = ? WHERE ID = ?";
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, xp);
                    pstmt.setString(2, userId);
                    int updated = pstmt.executeUpdate();
                    System.out.println("[DB WRITE] setTronXp(user=" + userId + ", xp=" + xp + ") attempts=" + (attempts + 1) + " rows=" + updated);
                    break;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        System.out.println("[DB WRITE] SQLITE_BUSY on setTronXp(user=" + userId + "), retrying (" + attempts + ")");
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    System.out.println("[DB WRITE] FAILED setTronXp(user=" + userId + ", xp=" + xp + ") attempts=" + (attempts + 1) + " error=" + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public void setKevinXp(String userId, long xp) {
        String sql = "UPDATE " + TABLE_NAME + " SET KEVIN_XP = ? WHERE ID = ?";
        synchronized (WRITE_LOCK) {
            int attempts = 0;
            while (true) {
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, xp);
                    pstmt.setString(2, userId);
                    int updated = pstmt.executeUpdate();
                    System.out.println("[DB WRITE] setKevinXp(user=" + userId + ", xp=" + xp + ") attempts=" + (attempts + 1) + " rows=" + updated);
                    break;
                } catch (SQLException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("SQLITE_BUSY") && attempts < 3) {
                        attempts++;
                        System.out.println("[DB WRITE] SQLITE_BUSY on setKevinXp(user=" + userId + "), retrying (" + attempts + ")");
                        try { Thread.sleep(100 * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    System.out.println("[DB WRITE] FAILED setKevinXp(user=" + userId + ", xp=" + xp + ") attempts=" + (attempts + 1) + " error=" + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    
    // --- UPDATED: GET TOP 10 (FIXED DATE DISPLAY) ---
    public List<String[]> getTop10Scores() {
        List<String[]> results = new ArrayList<>();
        
        String sql = "SELECT ID, HIGHEST_CHAPTER, HIGHEST_SCORE, LAST_COMPLETED " +
                     "FROM " + TABLE_NAME + " " +
                     "ORDER BY HIGHEST_SCORE DESC, HIGHEST_CHAPTER DESC LIMIT 10";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("ID");
                String level = String.valueOf(rs.getInt("HIGHEST_CHAPTER"));
                String score = String.valueOf(rs.getLong("HIGHEST_SCORE"));
                String date = rs.getString("LAST_COMPLETED");
                
                // --- FIX 2: Handle empty or long dates when reading ---
                if (date == null) {
                    date = "-";
                } else if (date.contains("T")) {
                    // Split at "T" and take the first part (The Date)
                    date = date.split("T")[0];
                }
                // -----------------------------------------------------

                results.add(new String[]{name, level, score, date});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}