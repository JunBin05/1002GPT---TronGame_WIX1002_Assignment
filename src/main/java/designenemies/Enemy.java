package designenemies;

import characters.Character; // ADDED: Import for the superclass
import characters.Direction; // USED: To match Direction enum usage in Character

/**
 * Abstract superclass for all AI-controlled enemies in the Tron game.
 * Defines common attributes and behaviors shared by all enemy types.
 */
public abstract class Enemy extends characters.Character {

    //Enemy attributes
    protected String name; // Enemy identifier
    protected String color;  // Jetwall color
    protected String difficulty; // Difficulty level
    protected int xp; // Experience points given to player
    protected String speed; // Movement speed (textual)
    protected String handling; // Turning efficiency
    protected String intelligence; // AI intelligence
    protected String description; // Enemy description

    protected int x, y; // Current position in the arena (x is column, y is row)
    // NOTE: r and c are inherited from Character, but Enemy uses x and y internally.
    protected Direction currentDirection = Direction.NORTH; 
    
    protected char[][] arenaGrid; // Reference to the arena map

    // Constructor
    public Enemy(
            String name, String color, String difficulty, int xp,
            String speed, String handling, String intelligence, String description
    ) {
        // FIX: Explicitly call the only available Character constructor
        super(name, color); 

        // Inherited fields from Character are set here:
        this.r = 0; // Initialize inherited fields
        this.c = 0;
        this.currentDirection = Direction.NORTH;
        
        // Enemy specific fields:
        this.name = name;
        this.color = color;
        this.difficulty = difficulty;
        this.xp = xp;
        this.speed = speed;
        this.handling = handling;
        this.intelligence = intelligence;
        this.description = description;
    }

    // NEW: Setter for the arena grid
    public void setArenaGrid(char[][] grid) {
        this.arenaGrid = grid;
    }

    // NEW: Applies the chosen move, updating the position (x, y)
    public void applyMove(Direction dir) {
        // Must update inherited position fields (r, c)
        this.currentDirection = dir; 
        
        switch (dir) {
            case NORTH -> { this.y--; this.r--; }
            case SOUTH -> { this.y++; this.r++; }
            case WEST -> { this.x--; this.c--; }
            case EAST -> { this.x++; this.c++; }
        }
    }

    // Spawn at a random position in the arena
    public void spawnRandom(int width, int height) {
        this.x = (int) (Math.random() * width);
        this.y = (int) (Math.random() * height);
        this.r = this.y; // Ensure inherited fields match
        this.c = this.x; // Ensure inherited fields match
    }

    // Abstract method for AI movement
    public abstract Direction decideMove();

    // --- IMPLEMENTATIONS OF ABSTRACT METHODS FROM characters.Character ---

    /**
     * Implements the abstract method from Character. Enemies do not level up.
     */
    @Override
    public void levelUp() {
        // No-op (no operation)
    }

    /**
     * Implements the setDirection method from Character. 
     * Enemies use decideMove() for AI, so this is a no-op.
     */
    @Override
    public void setDirection(char directionInput) {
        // No-op (AI controls direction)
    }

    //Override toString() for readable output
    @Override
    public String toString() {
        return name + " (" + color + ", " + difficulty + ") at (" + x + "," + y + ")";
    }
}