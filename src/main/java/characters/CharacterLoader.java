// src/characters/CharacterLoader.java

package characters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// NOTE: No need to import CharacterData because it's in the same package (characters)

/**
 * Handles the mandatory File I/O for character initialization.
 * This class adheres to the Single Responsibility Principle (SRP) by focusing only on file reading.
 */
public class CharacterLoader {

    // --- Replace this with your ABSOLUTE PATH or correct relative path ---
    private static final String FILE_PATH = "data\\characters.txt"; 

    /**
     * Reads character stats from characters.txt and returns a CharacterData object.
     */
    public static CharacterData loadCharacterData(String characterName) {
        // ... (The file reading logic remains the same)
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                // ... (File parsing and error handling) ...
                
                String[] parts = line.split(",\\s*");
                
                if (parts.length == 6 && parts[0].equalsIgnoreCase(characterName)) {
                    // ... (Parsing logic) ...
                    try {
                        String name = parts[0];
                        double speed = Double.parseDouble(parts[1]);
                        double handling = Double.parseDouble(parts[2]);
                        int lives = Integer.parseInt(parts[3]);
                        int discsOwned = Integer.parseInt(parts[4]);
                        int xp = Integer.parseInt(parts[5]);
                        
                        return new CharacterData(name, speed, handling, lives, discsOwned, xp);
                    } catch (NumberFormatException e) {
                        System.err.println("FATAL ERROR: Could not parse numerical data in line: " + line);
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("FATAL ERROR: File I/O Failed. Check path: " + FILE_PATH);
            return null;
        }
        
        System.err.println("FATAL: Character data not found in file for: " + characterName);
        return null;
    }

    // Simple debug runner to verify the loader reads from the data file
    public static void main(String[] args) {
        System.out.println("[CharacterLoader] Debug run: attempting to load Tron and Kevin from " + FILE_PATH);
        CharacterData t = loadCharacterData("Tron");
        if (t != null) System.out.println(String.format("Loaded Tron -> speed=%.3f handling=%.3f lives=%d discs=%d xp=%d", t.speed, t.handling, t.lives, t.discsOwned, t.experiencePoints)); else System.out.println("Failed to load Tron data.");
        CharacterData k = loadCharacterData("Kevin");
        if (k != null) System.out.println(String.format("Loaded Kevin -> speed=%.3f handling=%.3f lives=%d discs=%d xp=%d", k.speed, k.handling, k.lives, k.discsOwned, k.experiencePoints)); else System.out.println("Failed to load Kevin data.");
    }
}