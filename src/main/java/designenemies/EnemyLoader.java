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
        public char trailSymbol = 'M';

        // Per-tier values: index 0 => chapters 1-2, 1 => chapters 3-4, 2 => chapter 5
        // Arrays are left empty and populated from file when available; getters provide
        // safe defaults.
        public double[] tierHp = new double[3];
        public double[] tierSpeed = new double[3];
        public double[] tierHandling = new double[3];
        public double[] tierAggression = new double[3];
        public int[] tierTrail = new int[3];

        // Default constructor to create reasonable defaults
        public EnemyStats() {
            this.name = "Unknown";
            this.rank = "Minion";
            this.color = "Gray";
            // defaults already initialized in field declarations
        }

        // Header-aware constructor: reads columns by header name (tolerant)
        public EnemyStats(java.util.Map<String, Integer> idx, String[] parts) {
            this();

            this.name = getString(parts, idx, "Name", this.name);
            this.rank = getString(parts, idx, "Rank", this.rank);
            this.color = getString(parts, idx, "Color", this.color);
            String trailStr = getString(parts, idx, "TrailSymbol", null);
            if (trailStr != null && !trailStr.isEmpty()) {
                this.trailSymbol = trailStr.trim().charAt(0);
            } else if (this.color != null && !this.color.isEmpty()) {
                this.trailSymbol = java.lang.Character.toUpperCase(this.color.trim().charAt(0));
            }

            // No per-row base numeric columns expected — per-tier T1/T2/T3 columns are used
            // instead (defaults set above).

            // Parse per-tier columns if present; otherwise keep defaults
            for (int t = 1; t <= 3; t++) {
                tierHp[t - 1] = getDouble(parts, idx, "T" + t + "_HP", tierHp[t - 1]);
                tierSpeed[t - 1] = getDouble(parts, idx, "T" + t + "_Speed", tierSpeed[t - 1]);
                tierHandling[t - 1] = getDouble(parts, idx, "T" + t + "_Handling", tierHandling[t - 1]);
                tierAggression[t - 1] = getDouble(parts, idx, "T" + t + "_AggRESSION", tierAggression[t - 1]);
                tierTrail[t - 1] = getInt(parts, idx, "T" + t + "_Trail", tierTrail[t - 1]);
            }
        }

        // Helper accessors for header-based parsing
        private static String getString(String[] parts, java.util.Map<String, Integer> idx, String key, String def) {
            Integer i = idx.get(key);
            if (i == null || i >= parts.length)
                return def;
            String v = parts[i].trim();
            return v.isEmpty() ? def : v;
        }

        private static int getInt(String[] parts, java.util.Map<String, Integer> idx, String key, int def) {
            String s = getString(parts, idx, key, null);
            if (s == null)
                return def;
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                try {
                    return (int) Double.parseDouble(s);
                } catch (Exception ex) {
                    return def;
                }
            }
        }

        private static double getDouble(String[] parts, java.util.Map<String, Integer> idx, String key, double def) {
            String s = getString(parts, idx, key, null);
            if (s == null)
                return def;
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                return def;
            }
        }

        // Legacy descriptor helpers removed: loader now uses explicit per-tier numeric
        // columns (header-aware).

        // Accessors for tier-specific values (0=chapters1-2,1=3-4,2=5)
        public double getTierHp(int tier) {
            return tierHp[Math.max(0, Math.min(2, tier))];
        }

        public double getTierSpeed(int tier) {
            return tierSpeed[Math.max(0, Math.min(2, tier))];
        }

        public double getTierHandling(int tier) {
            return tierHandling[Math.max(0, Math.min(2, tier))];
        }

        public double getTierAggression(int tier) {
            return tierAggression[Math.max(0, Math.min(2, tier))];
        }

        public int getTierTrail(int tier) {
            return tierTrail[Math.max(0, Math.min(2, tier))];
        }

        public char getTrailSymbol() {
            return trailSymbol;
        }
    }

    // Stores data. Key = "Name_Rank" (e.g., "Clu_Minion")
    private static Map<String, EnemyStats> database = new HashMap<>();
    private static boolean isLoaded = false;

    public static void loadData() {
        if (isLoaded)
            return;

        File file = new File("data/enemies.txt");
        if (!file.exists()) {
            System.err.println("CRITICAL: enemies.txt not found at " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Read header line and build index map (skip comments and blank lines)
            String headerLine = null;
            while ((headerLine = br.readLine()) != null) {
                if (headerLine.trim().isEmpty())
                    continue;
                if (headerLine.trim().startsWith("#"))
                    continue;
                break;
            }
            if (headerLine == null)
                return;
            String[] headers = headerLine.split(",");
            java.util.Map<String, Integer> idx = new java.util.HashMap<>();
            for (int i = 0; i < headers.length; i++)
                idx.put(headers[i].trim(), i);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                String[] parts = line.split(",");
                EnemyStats stats = new EnemyStats(idx, parts);
                String key = stats.name + "_" + stats.rank;
                database.put(key, stats);
            }
            isLoaded = true;
            System.out.println("Enemy Database loaded successfully. Entries: " + database.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EnemyStats getStats(String name, boolean isBoss) {
        if (!isLoaded)
            loadData();

        String rank = isBoss ? "Boss" : "Minion";
        String key = name + "_" + rank;

        return database.get(key);
    }
}