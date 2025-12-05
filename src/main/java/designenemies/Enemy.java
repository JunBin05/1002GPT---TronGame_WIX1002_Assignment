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

    protected String difficulty; 
    protected String intelligence; 

    public Enemy(String name, String color, String difficulty, int xp, 
                 String speed, String handling, String intelligence, String description,
                 boolean isBoss) {
        super(name, color); 
        this.difficulty = difficulty;
        this.intelligence = intelligence;
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
    // Sees EVERYTHING.
    private Direction decideMoveSmart() {
        // 1. Straight
        int[] straight = getNextCoords(currentDirection);
        if (isBossSafe(straight[0], straight[1])) return currentDirection;
        
        // 2. Right
        Direction right = getTurn(currentDirection, true);
        int[] rCoords = getNextCoords(right);
        if (isBossSafe(rCoords[0], rCoords[1])) return right;

        // 3. Left
        Direction left = getTurn(currentDirection, false);
        int[] lCoords = getNextCoords(left);
        if (isBossSafe(lCoords[0], lCoords[1])) return left;

        return currentDirection; // Trapped
    }

    // --- STRATEGY 2: STUPID (Minion) ---
    // Sees Walls/Obstacles/Teammates, but is BLIND to Player Tail.
    private Direction decideMoveStupid() {
        int[] straight = getNextCoords(currentDirection);
        
        // Momentum: If straight looks "Minion Safe", take it mostly
        if (isMinionSafe(straight[0], straight[1]) && rand.nextDouble() > 0.1) {
            return currentDirection;
        }

        // Random Turn: Pick any direction that looks "Minion Safe"
        List<Direction> validMoves = new ArrayList<>();
        
        for (Direction d : Direction.values()) {
            if (d == getOpposite(currentDirection)) continue; 
            
            int[] next = getNextCoords(d);
            
            // Uses Minion Vision (Thinks 'T' is safe)
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

    // --- VISION 1: BOSS (PERFECT VISION) ---
    // Avoids everything dangerous.
    private boolean isBossSafe(int r, int c) {
        if (arenaGrid == null) return false; 
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) return false; 
        char cell = arenaGrid[r][c];
        
        // Safe ONLY if Empty (.) or Speed (S)
        // Avoids: Walls (#), Obstacles (O), Discs (D), Enemy Tails (K), Player Tails (T)
        return cell == '.' || cell == 'S';
    }

    // --- VISION 2: MINION (SELECTIVE VISION) ---
    // Avoids Walls and Teammates. Walks into Player Tails.
    private boolean isMinionSafe(int r, int c) {
        if (arenaGrid == null) return false; 
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) return false; 
        char cell = arenaGrid[r][c];
        
        // 1. DANGEROUS THINGS (Minion Avoids)
        // Wall (#), Obstacle (O), Disc (D), Other Enemy Tails (K)
        if (cell == '#' || cell == 'O' || cell == 'D' || cell == 'K') {
            return false;
        }
        
        // 2. "SAFE" THINGS (Minion Walks Here)
        // Empty (.), Speed (S)... AND PLAYER TAIL ('T')!
        // Because we return TRUE for 'T', the Minion will try to walk there and die.
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
    public String getName() {
        return this.name;
    }
    
    // --- BOSS MODIFIERS ---
    public boolean isBoss() {
        return this.isEnemyBoss;
    }
    
    public int getTrailDuration() {
        return this.isEnemyBoss ? 14 : 7; // Boss trails last 2x longer
    }
    
    public double getSpeedModifier() {
        return this.isEnemyBoss ? 1.5 : 1.0; // Boss moves 1.5x faster
    }
    
    public String getBossIndicator() {
        return this.isEnemyBoss ? " [BOSS]" : "";
    }
}