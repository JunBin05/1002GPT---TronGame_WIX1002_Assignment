// src/characters/CharacterData.java

package characters;

/**
 * Helper class to hold character attributes read from the external file.
 * This class is public to be used by Character.java and ArenaLoader.java.
 */
public class CharacterData { // <-- NOW PUBLIC
    public String name;
    public double speed;
    public double handling;
    public int lives;
    public int discsOwned;
    public int experiencePoints;

    public CharacterData(String name, double speed, double handling, int lives, int discsOwned, int xp) {
        this.name = name;
        this.speed = speed;
        this.handling = handling;
        this.lives = lives;
        this.discsOwned = discsOwned;
        this.experiencePoints = xp;
    }
}