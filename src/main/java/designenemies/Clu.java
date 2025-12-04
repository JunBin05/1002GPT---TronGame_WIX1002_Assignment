package designenemies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Clu enemy class.
 * Subclass of Enemy representing Clu type with very high speed and brilliant intelligence level.
 * Implements a strategic AI based on path assessment with added non-deterministic elements.
 */
public class Clu extends Enemy {
    
    private final Random rand = new Random();
    private static final double UNPREDICTABILITY_CHANCE = 0.3; // 30% chance to deviate slightly

    /**
     * Constructor that initializes a Clu object using data from enemies.txt
     * @param data - String array containing enemy attributes.
     */
    public Clu(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }
    
    // --- Helper Methods (Copied from Rinzler.java) ---
    
    private int[] getNextCoords(Direction dir, int r, int c) {
        int nextR = r; 
        int nextC = c; 
        
        switch (dir) {
            case NORTH -> nextR--;
            case SOUTH -> nextR++;
            case WEST -> nextC--;
            case EAST -> nextC++;
        }
        return new int[]{nextR, nextC};
    }

    private boolean isSafe(int r, int c) {
        if (arenaGrid == null) return false; 
        
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) {
            return false; 
        }
        
        char targetCell = arenaGrid[r][c];
        
        if (targetCell != '.' && targetCell != 'S') {
            return false; 
        }
        return true;
    }
    
    private int scoreDirection(Direction dir) {
        int score = 0;
        int r = this.y;
        int c = this.x;
        
        for (int i = 0; i < arenaGrid.length; i++) {
            int[] coords = getNextCoords(dir, r, c);
            r = coords[0];
            c = coords[1];
            
            if (isSafe(r, c)) {
                score++;
            } else {
                break; 
            }
        }
        return score;
    }

    // --- Clu's Core Decision Logic ---

    /**
     * Implement AI movement logic for Clu (Brilliant/Strategic).
     * Chooses the path with the most open space, but introduces a chance of
     * non-deterministic (random) movement for unpredictability.
     */
    @Override
    public Direction decideMove() {
        Direction reverseDir = switch (currentDirection) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case WEST -> Direction.EAST;
            case EAST -> Direction.WEST;
        };
        
        Map<Direction, Integer> scores = new HashMap<>();
        int maxScore = -1;
        List<Direction> bestDirections = new ArrayList<>();
        List<Direction> safeDirections = new ArrayList<>();
        
        // 1. Score all non-reverse directions
        for (Direction dir : Direction.values()) {
            if (dir == reverseDir) continue;
            
            int score = scoreDirection(dir);
            scores.put(dir, score);
            
            if (score > 0) {
                safeDirections.add(dir);
            }
            
            if (score > maxScore) {
                maxScore = score;
                bestDirections.clear();
                bestDirections.add(dir);
            } else if (score == maxScore) {
                bestDirections.add(dir);
            }
        }
        
        // 2. Introduce Unpredictability (Non-deterministic element)
        if (maxScore > 0 && safeDirections.size() > 1 && rand.nextDouble() < UNPREDICTABILITY_CHANCE) {
            // 30% chance to choose a random SAFE direction (not necessarily the best one)
            Collections.shuffle(safeDirections);
            return safeDirections.get(0);
        }
        
        // 3. Strategic Choice (Default behavior: choose the path with max open space)
        if (maxScore > 0) {
            // Choose randomly among the equally best directions
            Collections.shuffle(bestDirections);
            return bestDirections.get(0);
        } 
        
        // 4. Last resort: trapped
        return currentDirection;
    }
}