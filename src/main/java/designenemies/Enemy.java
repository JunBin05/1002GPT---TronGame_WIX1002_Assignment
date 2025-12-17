package designenemies;

import characters.Character; 
import characters.Direction; 
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Enemy extends Character {

    protected boolean isEnemyBoss; 
    protected char[][] arenaGrid; 
    protected Random rand = new Random();

    // Stats loaded from enemies.txt
    protected String difficulty; 
    protected String intelligence; 
    protected String speedDesc;
    protected String handlingDesc;
    protected String description;
    
    // Note: This field is loaded from the file, but your GameController 
    // now ignores it and uses TronRules instead. That is perfectly fine.
    protected long xpReward; 

    // --- CONSTRUCTOR ---
    public Enemy(String name, boolean isBoss) {
        super(name, "Gray");

        // 1. Look up stats from the Database/Text File
        EnemyLoader.EnemyStats stats = EnemyLoader.getStats(name, isBoss);

        // 2. Safety Check: If file read fails, prevent crash
        if (stats == null) {
            System.err.println("Warning: Stats missing for " + name + ". Using defaults.");
            // Manual fallback
            stats = new EnemyLoader.EnemyStats(new String[]{
                name, isBoss?"Boss":"Minion", "Gray", "Normal", "50", "Normal", "Normal", "Basic", "Fallback"
            });
        }

        // 3. Overwrite the placeholder color with the real one from the file
        this.color = stats.color;

        // 4. Set Local Fields from the Text File
        this.difficulty = stats.difficulty;
        this.intelligence = stats.intelligenceDesc;
        this.speedDesc = stats.speedDesc;
        this.handlingDesc = stats.handlingDesc;
        this.description = stats.description;
        this.xpReward = stats.xp; // Stored, even if unused by controller

        // Initialize per-instance numeric attributes from stats base values
        if (stats != null) {
            this.speed = stats.baseSpeed;
            this.handling = stats.baseHandling;
            this.aggression = stats.baseAggression;
        }
        
        // 5. Boss Logic (Lives & Behavior flag)
        this.isEnemyBoss = isBoss; 
        if (isBoss) {
            this.lives = 3;
            this.maxLives = 3;
            // Default boss trail duration (can be tuned later by LevelManager)
            this.setTrailDuration(14);
        } else {
            this.lives = 1;
            this.maxLives = 1;
            // Default minion trail duration
            this.setTrailDuration(7);
        }
    }

    public void setArenaGrid(char[][] grid) {
        this.arenaGrid = grid;
    }

    public void spawnRandom(int rows, int cols) {
        this.r = (int) (Math.random() * rows);
        this.c = (int) (Math.random() * cols);
        if (arenaGrid != null) {
            while (arenaGrid[r][c] != '.') {
                this.r = (int) (Math.random() * rows);
                this.c = (int) (Math.random() * cols);
            }
        }
    }

    // Behavior tuning
    private int moveInterval = isEnemyBoss ? 2 : 3; // number of ticks between moves (lower => faster)

    // New numeric attributes (per-instance)
    private double speed = 0.4;      // affects moveInterval
    private double handling = 0.7;   // 0..1, higher = more likely to keep straight at corners
    private double aggression = 0.2; // 0..1, higher = bosses are more likely to use smart moves

    // --- DECISION LOGIC ---
    public Direction decideMove() {
        if (this.isEnemyBoss) {
            // Boss uses aggression as the probability to execute the smart strategy
            if (rand.nextDouble() < this.aggression) return decideMoveSmart();
            return decideMoveStupid();
        } else {
            return decideMoveStupid();
        }
    }

    // Move interval getter/setter (used by controller to determine move frequency)
    public int getMoveInterval() { return this.moveInterval; }
    public void setMoveInterval(int interval) { this.moveInterval = Math.max(1, interval); }

    public double getSpeed() { return this.speed; }
    public void setSpeed(double s) { this.speed = Math.max(0.0, s); }

    public double getHandling() { return this.handling; }
    public void setHandling(double h) { this.handling = Math.max(0.0, Math.min(1.0, h)); }

    public double getAggression() { return this.aggression; }
    public void setAggression(double a) { this.aggression = Math.max(0.0, Math.min(1.0, a)); }

    // --- STRATEGY 1: SMART (Boss) ---
    private Direction decideMoveSmart() {
        int[] straight = getNextCoords(currentDirection);
        if (isBossSafe(straight[0], straight[1])) return currentDirection;
        
        Direction right = getTurn(currentDirection, true);
        int[] rCoords = getNextCoords(right);
        if (isBossSafe(rCoords[0], rCoords[1])) return right;

        Direction left = getTurn(currentDirection, false);
        int[] lCoords = getNextCoords(left);
        if (isBossSafe(lCoords[0], lCoords[1])) return left;

        return currentDirection; 
    }

    // --- STRATEGY 2: STUPID (Minion) ---
    private Direction decideMoveStupid() {
        int[] straight = getNextCoords(currentDirection);
        
        // Momentum: probability to keep moving straight equals 'handling' (higher handling -> more precise)
        if (isMinionSafe(straight[0], straight[1]) && rand.nextDouble() < this.handling) {
            return currentDirection;
        }

        // Random Turn
        List<Direction> validMoves = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d == getOpposite(currentDirection)) continue; 
            int[] next = getNextCoords(d);
            if (isMinionSafe(next[0], next[1])) {
                validMoves.add(d);
            }
        }
        
        if (!validMoves.isEmpty()) {
            return validMoves.get(rand.nextInt(validMoves.size()));
        }
        
        return currentDirection;
    }

    // --- HELPERS ---
    private int[] getNextCoords(Direction dir) {
        int nextR = this.r; int nextC = this.c; 
        switch (dir) { case NORTH -> nextR--; case SOUTH -> nextR++; case WEST -> nextC--; case EAST -> nextC++; }
        return new int[]{nextR, nextC};
    }

    private boolean isBossSafe(int r, int c) {
        if (arenaGrid == null) return false; 
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) return false; 
        char cell = arenaGrid[r][c];
        // Boss avoids everything dangerous
        return cell == '.' || cell == 'S';
    }

    private boolean isMinionSafe(int r, int c) {
        if (arenaGrid == null) return false; 
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) return false; 
        char cell = arenaGrid[r][c];
        // Minion walks into Player Tails ('T') but avoids walls
        if (cell == '#' || cell == 'O' || cell == 'D' || cell == 'M') {
            return false;
        }
        return true; 
    }
    
    private Direction getTurn(Direction d, boolean right) {
        if (right) return switch(d) { case NORTH->Direction.EAST; case EAST->Direction.SOUTH; case SOUTH->Direction.WEST; case WEST->Direction.NORTH; };
        return switch(d) { case NORTH->Direction.WEST; case WEST->Direction.SOUTH; case SOUTH->Direction.EAST; case EAST->Direction.NORTH; };
    }
    
    private Direction getOpposite(Direction d) {
        return switch(d) { case NORTH->Direction.SOUTH; case SOUTH->Direction.NORTH; case EAST->Direction.WEST; case WEST->Direction.EAST; };
    }

    @Override public void levelUp() {}
    @Override public void setDirection(char directionInput) {}
    
    // --- GETTERS ---
    public String getName() { return this.name; }
    public boolean isBoss() { return this.isEnemyBoss; }
    
    // Kept for compatibility, even if Controller uses Rules instead
    public long getXp() { return this.xpReward; }

    // Respect the per-instance trail duration set via setTrailDuration()
    @Override
    public int getTrailDuration() { return super.getTrailDuration(); }
    public double getSpeedModifier() { return this.isEnemyBoss ? 1.5 : 1.0; }
    public String getBossIndicator() { return this.isEnemyBoss ? " [BOSS]" : ""; }

    // NEW: Helper to get the color string for ArenaLoader
    public String getColorString() { return this.color; }
}