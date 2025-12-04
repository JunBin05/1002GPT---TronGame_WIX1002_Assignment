package designenemies;

import java.io.*;
import java.util.*;

/**
 * EnemyLoader is responsible for reading enemy data from a file
 * and creating corresponding Enemy objects.
 */

public class EnemyLoader {

     /**
     * Loads enemies from a text file.
     * Each line should contain 8 comma-separated fields:
     * name,color,difficulty,XP,speed,handling,intelligence,description
     *
     * @param filename The name of the enemies file (e.g., "enemies.txt")
     * @return List of Enemy objects loaded from the file
     */
    public static List<Enemy> loadEnemies(String filename) {
        List<Enemy> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            String line;
            while ((line = br.readLine()) != null) {

                // Split the line into 8 fields; last field may contain commas
                String[] d = line.split(",", 8); // 8 fields

                String name = d[0];

                Enemy enemy;

                // Create the appropriate subclass based on the name
                switch (name) {
                    case "Clu" -> enemy = new Clu(d);
                    case "Rinzler" -> enemy = new Rinzler(d);
                    case "Sark" -> enemy = new Sark(d);
                    case "Koura" -> enemy = new Koura(d);
                    default -> {
                        System.out.println("Unknown enemy type: " + name);
                        continue; // skip this line
                    }
                }

                // Add the enemy to the list
                list.add(enemy);
            }

        } catch (Exception e) {
            // For production, consider printing a friendly message instead of stack trace
            e.printStackTrace();
        }

        return list;
    }
}
