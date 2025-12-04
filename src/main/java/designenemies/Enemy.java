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

    protected int x, y; // Current position in the arena (x is column, y is row)
    protected Direction currentDirection = Direction.NORTH; // NEW: Tracks the cycle's current heading
    
    protected char[][] arenaGrid; // NEW: Reference to the arena map (used for safety checks)

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

    // NEW: Setter for the arena grid
    public void setArenaGrid(char[][] grid) {
        this.arenaGrid = grid;
    }

    // NEW: Applies the chosen move, updating the position (x, y)
    public void applyMove(Direction dir) {
        this.currentDirection = dir; // Always update direction
        
        switch (dir) {
            case NORTH -> this.y--;
            case SOUTH -> this.y++;
            case WEST -> this.x--;
            case EAST -> this.x++;
        }
    }

    // Spawn at a random position in the arena
    // NOTE: This assumes (x) is column, (y) is row, which matches typical grid indexing (grid[y][x])
    public void spawnRandom(int width, int height) {
        this.x = (int) (Math.random() * width);
        this.y = (int) (Math.random() * height);
    }

    // Abstract method for AI movement - NOW returns the chosen Direction
    public abstract Direction decideMove();

    //Override toString() for readable output
    @Override
    public String toString() {
        return name + " (" + color + ", " + difficulty + ") at (" + x + "," + y + ")";
    }
}