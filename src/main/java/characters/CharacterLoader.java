package characters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//Handles the mandatory File I/O for character initialization

public class CharacterLoader {

    private static final String FILE_PATH = "data\\characters.txt";

    /**
     * Populate the target character with values from characters.txt.
     * Expected CSV: name,speed,handling,lives,discsOwned,xp,symbol,color,imageBase
     */
    public static void loadInto(Character target, String characterName) {
        if (target == null || characterName == null)
            return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank())
                    continue;
                String[] p = line.split(",\\s*");
                if (p.length >= 6 && p[0].equalsIgnoreCase(characterName)) {
                    try {
                        target.name = p[0];
                        target.setSpeed(Double.parseDouble(p[1]));
                        target.setHandling(Double.parseDouble(p[2]));
                        target.setMaxLives(Double.parseDouble(p[3]));
                        target.setLives(target.getMaxLives());
                        target.setDiscsOwned(Integer.parseInt(p[4]));
                        target.setXp(Long.parseLong(p[5]));
                        if (p.length > 6 && !p[6].isEmpty())
                            target.symbol = p[6].charAt(0);
                        if (p.length > 7 && !p[7].isEmpty())
                            target.color = p[7];
                        if (p.length > 8 && !p[8].isEmpty())
                            target.imageBaseName = p[8];
                        target.recalcDiscCapacity();
                        target.currentDiscCount = target.getDiscCapacity();
                    } catch (NumberFormatException ignored) {
                    }
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("FATAL ERROR: File I/O Failed. Check path: " + FILE_PATH);
        }
    }
}