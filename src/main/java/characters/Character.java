package characters;

public abstract class Character {
    // --- Position and Identifier ---
    public int r;            
    public int c;            
    public String name;  
    
    // --- DISC SYSTEM: AMMO TRACKING ---
    public int currentDiscCount = 0;    
    
    public Direction currentDirection; 
    
    // Core Stats
    protected double lives;
    protected double maxLives;     
    protected double speed;      
    protected double handling;   
    protected int discsOwned;    
    protected int experiencePoints; // Kept to prevent load errors, but ignored
    protected int level = 1;     

    protected String color;      
    protected char symbol = 'P'; 
    public boolean isStunned = false;

    public Character(String name, String color) {
        this.name = name;
        this.color = color;
        this.currentDirection = Direction.NORTH; 
    }

    // --- INITIALIZATION ---
    public void loadInitialAttributes(CharacterData data) {
        this.speed = data.speed;
        this.handling = data.handling;
        this.lives = data.lives;
        this.maxLives = data.lives;
        this.discsOwned = data.discsOwned;
        this.experiencePoints = data.experiencePoints;
        
        // DISC CHANGE 1: Fill ammo to max when game starts
        this.currentDiscCount = this.discsOwned;
    }
    
    // --- DISC SYSTEM METHODS ---
    public boolean hasDisc() {
        return currentDiscCount > 0;
    }

    public void throwDisc() {
        if (currentDiscCount > 0) {
            currentDiscCount--;
            System.out.println("Disc Thrown! Remaining: " + currentDiscCount);
        }
    }

    public void pickupDisc() {
        if (currentDiscCount < discsOwned) {
            currentDiscCount++;
            System.out.println("Disc Retrieved! Ammo: " + currentDiscCount);
        }
    }

    // --- STANDARD GETTERS/SETTERS ---
    public int getRow() { return r; }
    public int getCol() { return c; }
    public double getLives() { return this.lives; }
    public double getMaxLives() { return this.maxLives; }
    public char getSymbol() { return this.symbol; }
    public int getLevel() { return this.level; } // Kept for display only

    public void changeLives(double amount) {
        this.lives += amount;
    }
    
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
    }

    public void revertPosition(char grid[][], int [][] trailTimer) {
        switch (this.currentDirection) {
            case NORTH -> r++; 
            case SOUTH -> r--; 
            case EAST -> c--; 
            case WEST -> c++; 
        }
        if (r >= 0 && r < 40 && c >= 0 && c < 40) {
            grid[this.r][this.c] = '.'; 
            trailTimer[this.r][this.c] = 0;
        }
        this.isStunned = true;
    }
    
    public void advancePosition(char[][] grid) {
        if (this.isStunned) {
            this.isStunned = false;
            return;
        }
        switch (this.currentDirection) {
            case NORTH -> r--; 
            case SOUTH -> r++; 
            case WEST -> c--; 
            case EAST -> c++; 
        }
    }
    
    public abstract void levelUp(); 
}