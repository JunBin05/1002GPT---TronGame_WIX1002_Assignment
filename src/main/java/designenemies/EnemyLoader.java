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
        // Legacy descriptive fields (kept for compatibility)
        public String speedDesc;
        public String handlingDesc;
        public String intelligenceDesc;
        public String description;

        // Base numeric defaults (derived from descriptors if explicit per-tier not provided)
        public double baseSpeed = 0.4;
        public double baseHandling = 0.7;
        public double baseAggression = 0.2;

        // Per-tier values: index 0 => chapters 1-2, 1 => chapters 3-4, 2 => chapter 5
        public double[] tierHp = new double[] {1.0, 1.5, 2.0};
        public double[] tierSpeed = new double[] {0.33, 0.66, 1.0};
        public double[] tierHandling = new double[] {0.70, 0.75, 0.80};
        public double[] tierAggression = new double[] {0.20, 0.35, 0.50};
        public int[]    tierTrail = new int[] {7, 9, 11};

        public EnemyStats(String[] parts) {
            // Parses the CSV line; first 9 fields are compatible with older format
            // Name,Rank,Color,Difficulty,XP,Speed,Handling,Intelligence,Description
            this.name = parts[0].trim();
            this.rank = parts[1].trim();
            this.color = parts[2].trim();
            this.difficulty = parts[3].trim();
            try { this.xp = Integer.parseInt(parts[4].trim()); } catch (Exception e) { this.xp = 50; }
            this.speedDesc = parts.length>5?parts[5].trim():"Normal";
            this.handlingDesc = parts.length>6?parts[6].trim():"Standard";
            this.intelligenceDesc = parts.length>7?parts[7].trim():"Basic";
            this.description = parts.length>8?parts[8].trim():"";

            // Derive base numeric values from descriptors
            this.baseSpeed = descriptorToSpeed(this.speedDesc);
            this.baseHandling = descriptorToHandling(this.handlingDesc);
            this.baseAggression = descriptorToAggression(this.intelligenceDesc);

            // Parse optional per-tier numeric columns if present (order expected as T1_HP,T1_Speed,T1_Handling,T1_AggRESSION,T1_Trail, ...)
            try {
                int idx = 9;
                if (parts.length > idx+4) {
                    tierHp[0] = Double.parseDouble(parts[idx+0].trim());
                    tierSpeed[0] = Double.parseDouble(parts[idx+1].trim());
                    tierHandling[0] = Double.parseDouble(parts[idx+2].trim());
                    tierAggression[0] = Double.parseDouble(parts[idx+3].trim());
                    tierTrail[0] = Integer.parseInt(parts[idx+4].trim());
                }
                if (parts.length > idx+9) {
                    tierHp[1] = Double.parseDouble(parts[idx+5].trim());
                    tierSpeed[1] = Double.parseDouble(parts[idx+6].trim());
                    tierHandling[1] = Double.parseDouble(parts[idx+7].trim());
                    tierAggression[1] = Double.parseDouble(parts[idx+8].trim());
                    tierTrail[1] = Integer.parseInt(parts[idx+9].trim());
                }
                if (parts.length > idx+14) {
                    tierHp[2] = Double.parseDouble(parts[idx+10].trim());
                    tierSpeed[2] = Double.parseDouble(parts[idx+11].trim());
                    tierHandling[2] = Double.parseDouble(parts[idx+12].trim());
                    tierAggression[2] = Double.parseDouble(parts[idx+13].trim());
                    tierTrail[2] = Integer.parseInt(parts[idx+14].trim());
                }
            } catch (Exception e) {
                // If parsing fails, leave defaults
            }
        }

        private double descriptorToSpeed(String d) {
            String s = d.toLowerCase();
            if (s.contains("very high") || s.contains("very_high")) return 0.9;
            if (s.contains("high")) return 0.7;
            if (s.contains("medium") || s.contains("med")) return 0.45;
            if (s.contains("low")) return 0.25;
            return 0.4; // normal
        }
        private double descriptorToHandling(String d) {
            String s = d.toLowerCase();
            if (s.contains("sharp") || s.contains("excellent")) return 0.9;
            if (s.contains("standard") || s.contains("std")) return 0.7;
            if (s.contains("erratic")) return 0.5;
            return 0.7;
        }
        private double descriptorToAggression(String d) {
            String s = d.toLowerCase();
            if (s.contains("brilliant") || s.contains("aggressive") || s.contains("clever")) return 0.7;
            if (s.contains("moderate") || s.contains("predictable")) return 0.45;
            if (s.contains("low") || s.contains("weak")) return 0.25;
            return 0.2;
        }

        // Accessors for tier-specific values (0=chapters1-2,1=3-4,2=5)
        public double getTierHp(int tier) { return tierHp[Math.max(0, Math.min(2, tier))]; }
        public double getTierSpeed(int tier) { return tierSpeed[Math.max(0, Math.min(2, tier))]; }
        public double getTierHandling(int tier) { return tierHandling[Math.max(0, Math.min(2, tier))]; }
        public double getTierAggression(int tier) { return tierAggression[Math.max(0, Math.min(2, tier))]; }
        public int    getTierTrail(int tier) { return tierTrail[Math.max(0, Math.min(2, tier))]; }
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