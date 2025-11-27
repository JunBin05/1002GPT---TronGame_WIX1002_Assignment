package designenemies;

/**
 * Abstract superclass for all AI-controlled enemies in the Tron game.
 * Defines common attributes and behaviors shared by all enemy types.
 */

public abstract class Enemy {

    //Enemy attributes
    protected String name; // Enemy identifier
    protected String color;  // Jetwall color
    protected String difficulty; // Difficulty level
    protected int xp; // Experience points given to player
    protected String speed; // Movement speed (textual)
    protected String handling; // Turning efficiency
    protected String intelligence; // AI intelligence
    protected String description; // Enemy description

    protected int x, y; // Current position in the arena

    // Constructor
    public Enemy(
            String name, String color, String difficulty, int xp,
            String speed, String handling, String intelligence, String description
    ) {
        this.name = name;
        this.color = color;
        this.difficulty = difficulty;
        this.xp = xp;
        this.speed = speed;
        this.handling = handling;
        this.intelligence = intelligence;
        this.description = description;
    }

    // Spawn at a random position in the arena
    public void spawnRandom(int width, int height) {
        this.x = (int) (Math.random() * width);
        this.y = (int) (Math.random() * height);
    }

    // Abstract method for AI movement
    // Each subclass must implement its own movement logic
    public abstract void decideMove();

    //Override toString() for readable output
    @Override
    public String toString() {
        return name + " (" + color + ", " + difficulty + ") at (" + x + "," + y + ")";
    }
}


