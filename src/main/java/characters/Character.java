package characters;

import XPSystem.TronRules; 

public abstract class Character {
    public int r;            
    public int c;            
    public String name;  
    public String imageBaseName; 
    public int currentDiscCount = 0;    
    public Direction currentDirection; 
    
    protected double lives;
    protected double maxLives;     
    protected int level = 1;     
    protected long currentXp = 0; 
    protected int discCapacity = 1; 

    protected long pendingXp = 0; 

    protected String color;      
    protected char symbol = 'P'; 
    public boolean isStunned = false;

    // Attributes
    protected int experiencePoints; 
    protected double speed = 1.0;      
    protected double handling = 1.0;   
    public int discsOwned;   

    public Character(String name, String color) {
        this.name = name;
        this.color = color;
        this.currentDirection = Direction.NORTH; 
    }

    // --- MISSING METHOD ADDED HERE ---
    public void setStartPosition(int r, int c) {
        this.r = r;
        this.c = c;
    }
    // ---------------------------------

    public void loadInitialAttributes(CharacterData data) {
        this.speed = data.speed;
        this.handling = data.handling;
        this.lives = data.lives;
        this.maxLives = data.lives;
        
        // Initial Capacity Check
        this.discCapacity = 1 + (this.level / 15);
        this.currentDiscCount = this.discCapacity; 
        
        this.experiencePoints = data.experiencePoints; 
    }
    
    // --- FORCE REFILL ---
    // Called by GameController Constructor to ensure Stage 2 starts fresh
    public void prepareForNextStage() {
        // Recalculate capacity just in case
        this.discCapacity = 1 + (this.level / 15);
        
        this.currentDiscCount = this.discCapacity; // Refill Ammo
        this.lives = this.maxLives;                // Heal to Full
        this.isStunned = false;
        
        System.out.println(name + " Ready for Stage. Discs: " + currentDiscCount + "/" + discCapacity + " HP: " + lives);
    }

    public void addXP(long amount) {
        this.pendingXp += amount;
    }

    public String commitPendingXP() {
        if (pendingXp == 0) return "No XP Gained.";

        long oldXp = currentXp;
        int oldLevel = level;
        double oldSpeed = speed;

        this.currentXp += pendingXp;
        long gained = pendingXp;
        this.pendingXp = 0; 

        boolean didLevelUp = false;
        while (level < TronRules.MAX_LEVEL && currentXp >= TronRules.getTotalXpForLevel(level + 1)) {
            levelUp(); 
            didLevelUp = true;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width: 300px; background-color: #222; color: #00FFCC; font-family: Sans-serif; padding: 10px;'>");
        sb.append("<h2 style='text-align: center; border-bottom: 2px solid #00FFCC;'>SYSTEM UPDATE</h2>");
        sb.append("<table style='width: 100%; color: white;'>");
        
        sb.append(String.format("<tr><td>XP Gained:</td><td style='text-align: right; color: #00FF00;'>+%d</td></tr>", gained));
        sb.append(String.format("<tr><td>Total XP:</td><td style='text-align: right;'>%d</td></tr>", currentXp));
        
        if (didLevelUp) {
            sb.append("<tr><td colspan='2'><hr></td></tr>");
            sb.append(String.format("<tr><td><b>LEVEL:</b></td><td style='text-align: right; color: yellow;'><b>%d &rarr; %d</b></td></tr>", oldLevel, level));
            sb.append(String.format("<tr><td>Speed:</td><td style='text-align: right;'>%.2f &rarr; <span style='color: #00FFCC;'>%.2f</span></td></tr>", oldSpeed, speed));
            
            // Show Capacity Increase
            sb.append(String.format("<tr><td>Disc Cap:</td><td style='text-align: right;'>%d</td></tr>", discCapacity));
            sb.append(String.format("<tr><td>Max Lives:</td><td style='text-align: right;'>%.1f</td></tr>", maxLives));
        } else {
             sb.append("<tr><td colspan='2'><br><i style='color: #888;'>Progress to Level " + (level+1) + "...</i></td></tr>");
        }
        
        sb.append("</table></body></html>");
        return sb.toString();
    }

    public void levelUp() {
        level++;
        
        // --- NEW RULE: 1 Disc + 1 extra every 15 levels ---
        this.discCapacity = 1 + (this.level / 15);
        
        // Refill immediately on level up
        this.currentDiscCount = this.discCapacity; 
        
        System.out.println(">>> LEVEL UP! " + name + " is now Level " + level + ". Disc Cap: " + discCapacity);
    }

    public boolean hasDisc() { return currentDiscCount > 0; }
    
    public void throwDisc() {
        if (currentDiscCount > 0) {
            currentDiscCount--;
        }
    }

    public void pickupDisc() {
        if (currentDiscCount < discCapacity) {
            currentDiscCount++;
        }
    }

    // Getters
    public int getRow() { return r; }
    public int getCol() { return c; }
    public double getLives() { return this.lives; }
    public double getMaxLives() { return this.maxLives; }
    public char getSymbol() { return this.symbol; }
    public int getLevel() { return this.level; } 
    public long getXp() { return this.currentXp; }
    public double getSpeed() { return this.speed; }
    public double getHandling() { return this.handling; }
    public int getDiscCapacity() { return this.discCapacity; }

    public void setStunned(boolean stunned) { this.isStunned = stunned; }

    public void changeLives(double amount) {
        this.lives += amount;
        if (this.lives > this.maxLives) this.lives = this.maxLives;
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
        switch (this.currentDirection) { case NORTH -> r++; case SOUTH -> r--; case EAST -> c--; case WEST -> c++; }
        if (r >= 0 && r < 40 && c >= 0 && c < 40) {
            grid[this.r][this.c] = '.'; 
            trailTimer[this.r][this.c] = 0;
        }
        this.isStunned = true;
    }
    
    public void advancePosition(char[][] grid) {
        if (this.isStunned) { this.isStunned = false; return; }
        switch (this.currentDirection) { case NORTH -> r--; case SOUTH -> r++; case WEST -> c--; case EAST -> c++; }
    }
}