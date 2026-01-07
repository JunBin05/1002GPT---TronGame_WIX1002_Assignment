package characters;

import XPSystem.TronRules; 

public abstract class Character {
    public int r;            
    public int c;            
    public String name;  
    public String imageBaseName; 
    public int currentDiscCount = 0;    
    // Per-entity disc cooldown end timestamp (ns) so player and enemies share logic
    protected long nextDiscReadyNs = 0L;
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
    public boolean isBoss = false;
    public boolean isPlayer = false;
    public boolean tookDamage = false;

    // Attributes
    protected int experiencePoints; 
    protected double speed = 1.0;      
    protected double handling = 1.0;   
    public int discsOwned;  

    // Pending turn requested by player input. Applied probabilistically based on handling.
    protected char pendingDirection = '\0';

    public void requestDirection(char dir) { this.pendingDirection = java.lang.Character.toUpperCase(dir); }

    /**
     * Attempt to apply the pending direction based on handling and grid availability.
     * Returns true if applied (direction changed), false otherwise.
     */
    public boolean tryApplyPendingDirection(char[][] grid) {
        if (this.pendingDirection == '\0') return false;
        int nextR = this.r; int nextC = this.c;
        switch (java.lang.Character.toUpperCase(this.pendingDirection)) {
            case 'W' -> nextR--;
            case 'S' -> nextR++;
            case 'A' -> nextC--;
            case 'D' -> nextC++;
            default -> { return false; }
        }
        // out of bounds or blocked?
        if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) return false;
        char tile = grid[nextR][nextC];
        if (tile == '#' || tile == 'O' || tile == 'D' || tile == 'M') return false;

        // Decide if the player's handling allows the turn to be executed now
        double roll = Math.random();
        if (roll < this.handling) {
            setDirection(this.pendingDirection);
            this.pendingDirection = '\0';
            return true;
        }
        // not applied this tick, keep pending for next tick
        return false;
    }

    public Character() {
        this.name = "";
        this.color = "";
        this.imageBaseName = "";
        this.currentDirection = Direction.NORTH;
    }

    // --- MISSING METHOD ADDED HERE ---
    public void setStartPosition(int r, int c) {
        this.r = r;
        this.c = c;
    }
    // ---------------------------------

    // --- FORCE REFILL ---
    // Called by GameController Constructor to ensure Stage 2 starts fresh
    public void prepareForNextStage() {
        // Recalculate capacity just in case
        this.discCapacity = this.discsOwned + (this.level / 15);
        
        this.currentDiscCount = this.discCapacity; // Refill Ammo
        this.lives = this.maxLives;                // Heal to Full
        this.isStunned = false;
        this.tookDamage = false;
        
        System.out.println(name + " Ready for Stage. Discs: " + currentDiscCount + "/" + discCapacity + " HP: " + lives);
        if (this.isPlayer) {
            arena.ArenaLoader.appendGameplayLog("Ready for Stage. Discs: " + currentDiscCount + "/" + discCapacity + " HP: " + lives);
        }
    }

    public void addXP(long amount) {
        this.pendingXp += amount;
    }

    /**
     * Set the current XP for this character and synchronize level accordingly.
     * This is used when loading saved player data from the database.
     */
    public void setXp(long xp) {
        if (xp < 0) xp = 0;
        this.currentXp = xp;
        // Recompute level to match XP (start from current level, levelUp will apply buffs)
        while (level < TronRules.MAX_LEVEL && currentXp >= TronRules.getTotalXpForLevel(level + 1)) {
            levelUp();
        }
    }

    public String commitPendingXP(String username) {
        if (pendingXp == 0) return "No XP Gained.";

        int oldLevel = level;
        double oldSpeed = speed;

        // Determine saved max level for display purposes (do not change player's state)
        int savedMaxLevel = 0;
        if (username != null && !username.trim().isEmpty()) {
            try {
                UI.DatabaseManager db = new UI.DatabaseManager();
                if (this.name.equals("Tron")) savedMaxLevel = db.getTronLevel(username);
                else if (this.name.equals("Kevin")) savedMaxLevel = db.getKevinLevel(username);
            } catch (Exception e) {
                // ignore and proceed with savedMaxLevel = 0
            }
        }

        int displayOldLevel = Math.max(savedMaxLevel, oldLevel);

        // Now apply XP and level up the in-memory character
        this.currentXp += pendingXp;
        long gained = pendingXp;
        this.pendingXp = 0; 

        boolean didLevelUp = false;
        while (level < TronRules.MAX_LEVEL && currentXp >= TronRules.getTotalXpForLevel(level + 1)) {
            levelUp(); 
            didLevelUp = true;
        }

        int newLevel = level;
        int displayNewLevel = Math.max(savedMaxLevel, newLevel);

        // Build display stats for OLD and NEW using in-memory values.
        // We intentionally avoid simulating base attributes from CharacterData here to keep the UI
        // method lightweight and reduce failure surface; saved max-level (from DB) is still respected
        // via `displayOldLevel`/`displayNewLevel` computed earlier.
        double displayOldSpeed = oldSpeed;
        double displayNewSpeed = this.speed;
        double displayOldHandling = this.handling;
        double displayNewHandling = this.handling;
        int displayNewDiscCap = this.discCapacity;
        double displayNewMaxLives = this.maxLives;


        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width: 300px; background-color: #222; color: #00FFCC; font-family: Sans-serif; padding: 10px;'>");
        sb.append("<h2 style='text-align: center; border-bottom: 2px solid #00FFCC;'>SYSTEM UPDATE</h2>");
        sb.append("<table style='width: 100%; color: white;'>");

        sb.append(String.format("<tr><td>XP Gained:</td><td style='text-align: right; color: #00FF00;'>+%d</td></tr>", gained));
        sb.append(String.format("<tr><td>Total XP:</td><td style='text-align: right;'>%d</td></tr>", currentXp));

        if (didLevelUp || displayOldLevel != displayNewLevel) {
            sb.append("<tr><td colspan='2'><hr></td></tr>");
            sb.append(String.format("<tr><td><b>LEVEL:</b></td><td style='text-align: right; color: yellow;'><b>%d &rarr; %d</b></td></tr>", displayOldLevel, displayNewLevel));
            sb.append(String.format("<tr><td>Speed:</td><td style='text-align: right;'>%.2f &rarr; <span style='color: #00FFCC;'>%.2f</span></td></tr>", displayOldSpeed, displayNewSpeed));
            sb.append(String.format("<tr><td>Handling:</td><td style='text-align: right;'>%.2f &rarr; <span style='color: #00FFCC;'>%.2f</span></td></tr>", displayOldHandling, displayNewHandling));

            // Show Capacity Increase (show new capacity)
            sb.append(String.format("<tr><td>Disc Cap:</td><td style='text-align: right;'>%d</td></tr>", displayNewDiscCap));
            sb.append(String.format("<tr><td>Max Lives:</td><td style='text-align: right;'>%.1f</td></tr>", displayNewMaxLives));
        } else {
             sb.append("<tr><td colspan='2'><br><i style='color: #888;'>Progress to Level " + (displayNewLevel+1) + "...</i></td></tr>");
        }

        sb.append("</table></body></html>");
        return sb.toString();
    }

    public void levelUp() {
        level++;
        
        // --- NEW RULE: 1 Disc + 1 extra every 15 levels ---
        this.discCapacity = this.discsOwned + (this.level / 15);
        
        // Refill immediately on level up
        this.currentDiscCount = this.discCapacity; 

        if (level % 10 == 0) {
            this.maxLives += 1.0; 
            this.lives = this.maxLives; // Heal to the new max immediately
            System.out.println(">> MAX LIVES INCREASED! New Max: " + this.maxLives);
        }
        
        // Removed noisy console print to reduce log spam on frequent level-ups
        // Persist XP immediately when leveling up for logged-in users so UI remains consistent
        try {
            if (arena.ArenaLoader.mainFrame instanceof UI.MainFrame) {
                String user = ((UI.MainFrame) arena.ArenaLoader.mainFrame).getCurrentUsername();
                if (user != null && !user.trim().isEmpty()) {
                    new Thread(() -> {
                        try {
                            UI.DatabaseManager db = new UI.DatabaseManager();
                            if (this.name.equals("Tron")) db.setTronXp(user, this.currentXp);
                            else if (this.name.equals("Kevin")) db.setKevinXp(user, this.currentXp);
                        } catch (Exception e) {
                            // ignore persistence failures (non-critical)
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            // ignore any errors retrieving mainFrame
        }
    }

    public boolean hasDisc() { return currentDiscCount > 0; }
    
    public void throwDisc() {
        if (currentDiscCount > 0) {
            currentDiscCount--;
            if (this.isPlayer) {
                arena.ArenaLoader.appendGameplayLog("Used a disc (Discs: " + currentDiscCount + "/" + discCapacity + ")");
            }
        }
    }

    public void pickupDisc() {
        if (currentDiscCount < discCapacity) {
            currentDiscCount++;
            if (this.isPlayer) {
                arena.ArenaLoader.appendGameplayLog("Picked up a disc (Discs: " + currentDiscCount + "/" + discCapacity + ")");
            }
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
    public void setSpeed(double speed) { this.speed = speed; }
    public void setHandling(double handling) { this.handling = handling; }
    public void setDiscCapacity(int cap) {
        this.discCapacity = Math.max(0, cap);
        if (this.currentDiscCount > this.discCapacity) this.currentDiscCount = this.discCapacity;
    }
    public long getNextDiscReadyNs() { return this.nextDiscReadyNs; }
    public void setNextDiscReadyNs(long ns) { this.nextDiscReadyNs = ns; }

    public void setStunned(boolean stunned) { this.isStunned = stunned; }

    public void changeLives(double amount) {
        double oldLives = this.lives; 
        
        this.lives += amount;
        if (this.lives > this.maxLives) this.lives = this.maxLives;

        // Append gameplay log for damage/heal
        if (amount < 0) {
            this.tookDamage = true;
            arena.ArenaLoader.appendGameplayLog(this.name + " took " + String.format("%.1f", -amount) + " damage (HP: " + String.format("%.1f", this.lives) + "/" + String.format("%.1f", this.maxLives) + ")");
        } 

        // --- DEATH DETECTION ---
        if (this.lives <= 0 && oldLives > 0) {
            
            // CASE A: PLAYER DIED 
            if (this.isPlayer) {
                // [Icon 3] Learning the Hard Way
                arena.ArenaLoader.unlockAchievement(3, "LEARNING THE HARD WAY", "Experience your first dead.");
                arena.ArenaLoader.appendGameplayLog("Player died! Nooooo...");

                // Calculate where the player was facing when they died
                int nextR = this.r;
                int nextC = this.c;
                switch (this.currentDirection) { 
                    case NORTH -> nextR--; 
                    case SOUTH -> nextR++; 
                    case WEST -> nextC--; 
                    case EAST -> nextC++; 
                }

                // If that destination is OUT OF BOUNDS, they fell into the void!
                if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) {
                     arena.ArenaLoader.unlockAchievement(5, "INTO THE VOID", "Fall Outside the map.");
                     System.out.println(">> ACHIEVEMENT: Fell into the void!");
                     arena.ArenaLoader.appendGameplayLog("Fell into the void!");
                }
                // =========================================================
            } 
            
            // CASE B: ENEMY DIED (Clu, Sark, etc.)
            else {
                arena.ArenaLoader.unlockAchievement(1, "FIRST BLOOD", "Defeat your very first enemy.");
                arena.ArenaLoader.appendGameplayLog(this.name + " defeated.");

                if (this.isBoss) {
                     arena.ArenaLoader.unlockAchievement(4, "BOSS SLAYER", "Defeat a boss for the first time.");
                }
            }
        }
    }
    

    // Public setters so callers (e.g., LevelManager) can tune health values per-stage
    public void setMaxLives(double max) {
        this.maxLives = max;
        if (this.lives > this.maxLives) this.lives = this.maxLives;
    }

    public void setLives(double lives) {
        this.lives = lives;
        if (this.lives > this.maxLives) this.lives = this.maxLives;
    }

    // Trail visibility length (number of steps before a trail cell decays)
    protected int trailDuration = 7;
    public int getTrailDuration() { return this.trailDuration; }
    public void setTrailDuration(int duration) { this.trailDuration = Math.max(1, duration); }
    
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

    public void advancePosition(char[][] grid) {
        if (this.isStunned) { this.isStunned = false; return; }
        switch (this.currentDirection) { case NORTH -> r--; case SOUTH -> r++; case WEST -> c--; case EAST -> c++; }
    }
}