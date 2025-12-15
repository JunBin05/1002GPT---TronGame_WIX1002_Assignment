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
        this.intelligence = stats.intelligence;
        this.speedDesc = stats.speed;
        this.handlingDesc = stats.handling;
        this.description = stats.description;
        this.xpReward = stats.xp; // Stored, even if unused by controller
        
        // 5. Boss Logic (Lives & Behavior flag)
        this.isEnemyBoss = isBoss; 
        if (isBoss) {
            this.lives = 3;
            this.maxLives = 3;
        } else {
            this.lives = 1;
            this.maxLives = 1;
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

    // --- DECISION LOGIC ---
    public Direction decideMove() {
        if (this.isEnemyBoss) {
            return decideMoveSmart();
        } else {
            return decideMoveStupid();
        }
    }

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
        
        // Momentum
        if (isMinionSafe(straight[0], straight[1]) && rand.nextDouble() > 0.1) {
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
        if (cell == '#' || cell == 'O' || cell == 'D' || cell == 'K') {
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

    public int getTrailDuration() { return this.isEnemyBoss ? 14 : 7; }
    public double getSpeedModifier() { return this.isEnemyBoss ? 1.5 : 1.0; }
    public String getBossIndicator() { return this.isEnemyBoss ? " [BOSS]" : ""; }

    // NEW: Helper to get the color string for ArenaLoader
    public String getColorString() { return this.color; }
}