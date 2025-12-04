<<<<<<< HEAD
package characters;
import java.awt.Point;
import java.util.LinkedList;
=======
package characters; 
>>>>>>> main

public abstract class Character {
    // --- Position and Identifier (MUST BE PUBLIC for ArenaLoader access) ---
    public int r;            
    public int c;            
    public String name;      
    
<<<<<<< HEAD
    private static final int MAX_TRAIL_LENGTH = 7;
=======
    // --- NEW: Direction Tracking Field ---
>>>>>>> main
    public Direction currentDirection; // Tracks the cycle's current heading
    // -------------------------------------
    
    // Core Stats (Protected)
    protected double lives;
    protected double maxLives;     
    protected double speed;      
    protected double handling;   
    protected int discsOwned;    
    protected int experiencePoints; 
    protected int level = 1;     

    protected String color;      
    protected char symbol = 'P'; 

    public Character(String name, String color) {
        this.name = name;
        this.color = color;
        // Initialize the direction to a default state (e.g., North/Up)
        this.currentDirection = Direction.NORTH; 
    }

    public boolean isStunned = false;

    public void loadInitialAttributes(CharacterData data) {
        this.speed = data.speed;
        this.handling = data.handling;
        this.lives = data.lives;
        this.maxLives=data.lives;
        this.discsOwned = data.discsOwned;
        this.experiencePoints = data.experiencePoints;
    }

    // FIX: Getters needed by ArenaLoader
    public int getRow() { return r; }
    public int getCol() { return c; }

    public double getLives() {
        return this.lives;
    }

    public double getMaxLives() { 
        return this.maxLives; 
    }

<<<<<<< HEAD
    public char getSymbol() {
        return this.symbol;
    }

=======
>>>>>>> main
    public void changeLives(double amount) {
        this.lives += amount;
    }
    
    // --- UPDATED: Method to Handle Direction Input (Turning) ---
    /**
     * Updates the cycle's currentDirection based on WASD input.
     * This method handles turning only.
     */
    public void setDirection(char directionInput) {
        switch (directionInput) {
            case 'W' -> this.currentDirection = Direction.NORTH; 
            case 'S' -> this.currentDirection = Direction.SOUTH; 
            case 'A' -> this.currentDirection = Direction.WEST; 
            case 'D' -> this.currentDirection = Direction.EAST; 
        }
    }

    public void setOppositeDirection() {
        this.currentDirection = switch (this.currentDirection) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
        System.out.println(this.name + " reversed direction due to collision. New direction: " + this.currentDirection);
    }

<<<<<<< HEAD
    public void revertPosition(char grid[][], int [][] trailTimer) {
    // Moves the cycle's position one unit backward based on its current direction.
    // NOTE: This does NOT change the currentDirection, which is needed for the turn.
        switch (this.currentDirection) {
            case NORTH:
                r++; // Go South (since last move was North)
                break;
            case SOUTH:
                r--; // Go North (since last move was South)
                break;
            case EAST:
                c--;  // Go West (since last move was East)
                break;
            case WEST:
                c++;  // Go East (since last move was West)
                break;
        };
        
        if (r >= 0 && r < 40 && c >= 0 && c < 40) {
                grid[this.r][this.c] = '.'; 
                trailTimer[this.r][this.c] = 0;
        }

        this.isStunned = true;
    }
=======
    public void revertPosition() {
    // Moves the cycle's position one unit backward based on its current direction.
    // NOTE: This does NOT change the currentDirection, which is needed for the turn.
    switch (this.currentDirection) {
        case NORTH -> r++; // Go South (since last move was North)
        case SOUTH -> r--; // Go North (since last move was South)
        case EAST -> c--;  // Go West (since last move was East)
        case WEST -> c++;  // Go East (since last move was West)
    };

    this.isStunned = true;
}
>>>>>>> main
    
    // --- NEW: Method to Advance Position (Moving Straight) ---
    /**
     * Moves the cycle one grid unit in the currentDirection.
     * This method handles the continuous movement required by Light Cycles.
     */
<<<<<<< HEAD
    public void advancePosition(char[][] grid) {
=======
    public void advancePosition() {
>>>>>>> main
        if (this.isStunned) {
            // Skip movement this frame, but clear the stun for the next frame
            this.isStunned = false;
            return;
        }
<<<<<<< HEAD

=======
>>>>>>> main
        switch (this.currentDirection) {
            case NORTH -> r--; 
            case SOUTH -> r++; 
            case WEST -> c--; 
            case EAST -> c++; 
        }
    }
    
    // The previous 'move(char direction)' method is now replaced by setDirection() and advancePosition().
    public abstract void levelUp(); 
}