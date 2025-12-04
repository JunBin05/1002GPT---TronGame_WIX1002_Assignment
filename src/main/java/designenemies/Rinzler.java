package designenemies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rinzler enemy class.
 * Subclass of Enemy representing Rinzler type with very high speed and clever intelligence level.
 * Implements a tactical AI that chooses the path with the most open space.
 */
public class Rinzler extends Enemy {

     /**
     * Constructor that initializes a Rinzler object using data from enemies.txt
     * @param data - String array containing enemy attributes.
     */
    public Rinzler(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }
    
    // --- Helper Methods (Same as Koura.java) ---
    // (Note: For Rinzler, we'll implement the safety check directly within the scoring logic)

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
    
    /**
     * Scores a direction by counting how many safe steps it can take in a straight line.
     */
    private int scoreDirection(Direction dir) {
        int score = 0;
        int r = this.y;
        int c = this.x;
        
        // Loop up to max arena size to count steps
        for (int i = 0; i < arenaGrid.length; i++) {
            int[] coords = getNextCoords(dir, r, c);
            r = coords[0];
            c = coords[1];
            
            if (isSafe(r, c)) {
                score++;
            } else {
                break; // Stop counting when hitting an obstacle
            }
        }
        return score;
    }

    /**
     * Implement AI movement logic for Rinzler (Clever/Adaptive).
     * Chooses the direction that provides the most open space.
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
        
        // 1. Score all non-reverse directions
        for (Direction dir : Direction.values()) {
            if (dir == reverseDir) continue;
            
            int score = scoreDirection(dir);
            scores.put(dir, score);
            
            if (score > maxScore) {
                maxScore = score;
                bestDirections.clear();
                bestDirections.add(dir);
            } else if (score == maxScore) {
                bestDirections.add(dir);
            }
        }
        
        // 2. Select the best direction
        if (maxScore > 0) {
            // If multiple directions are equally good, choose one randomly
            Collections.shuffle(bestDirections);
            return bestDirections.get(0);
        } 
        
        // 3. If all non-reverse directions result in immediate crash (score 0), 
        // try to randomly choose any direction just before crashing.
        if (isSafe(getNextCoords(currentDirection, y, x)[0], getNextCoords(currentDirection, y, x)[1])) {
            return currentDirection;
        }

        // Last resort: trapped
        return currentDirection;
    }
}