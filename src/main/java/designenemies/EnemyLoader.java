package designenemies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnemyLoader {

    // Helper class to hold one row of data from the text file
    public static class EnemyStats {
        public String name;
        public String rank; // "Boss" or "Minion"
        public String color;
        public String difficulty;
        public int xp;
        public String speed;
        public String handling;
        public String intelligence;
        public String description;

        public EnemyStats(String[] parts) {
            // Parses the CSV line: Name,Rank,Color,Difficulty,XP,Speed,Handling,Intelligence,Description
            this.name = parts[0].trim();
            this.rank = parts[1].trim();
            this.color = parts[2].trim();
            this.difficulty = parts[3].trim();
            this.xp = Integer.parseInt(parts[4].trim());
            this.speed = parts[5].trim();
            this.handling = parts[6].trim();
            this.intelligence = parts[7].trim();
            this.description = parts[8].trim();
        }
    }

    // Stores data. Key = "Name_Rank" (e.g., "Clu_Minion")
    private static Map<String, EnemyStats> database = new HashMap<>();
    private static boolean isLoaded = false;

    public static void loadData() {
        if (isLoaded) return; 

        
        File file = new File("data/enemies.txt"); 
        if (!file.exists()) {
            System.err.println("CRITICAL: enemies.txt not found at " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip header
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    EnemyStats stats = new EnemyStats(parts);
                    String key = stats.name + "_" + stats.rank;
                    database.put(key, stats);
                }
            }
            isLoaded = true;
            System.out.println("Enemy Database loaded successfully. Entries: " + database.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EnemyStats getStats(String name, boolean isBoss) {
        if (!isLoaded) loadData();

        String rank = isBoss ? "Boss" : "Minion";
        String key = name + "_" + rank;
        
        return database.get(key);
    }
}