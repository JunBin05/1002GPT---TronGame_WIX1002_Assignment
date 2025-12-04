package designenemies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Koura enemy class.
 * Subclass of Enemy representing Koura type with low speed and low intelligence level.
 * Moves randomly with minimal regard for safety.
 */
public class Koura extends Enemy {

    /**
     * Constructor that initializes a Koura object using data from enemies.txt
     * @param data - String array containing enemy attributes.
     */
    public Koura(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }

    /**
     * Helper method to determine the coordinates of a potential move.
     * Maps (Direction) to (y, x) change.
     */
    private int[] getNextCoords(Direction dir) {
        int nextR = this.y; // 'y' is the row index
        int nextC = this.x; // 'x' is the column index
        
        switch (dir) {
            case NORTH -> nextR--;
            case SOUTH -> nextR++;
            case WEST -> nextC--;
            case EAST -> nextC++;
        }
        return new int[]{nextR, nextC};
    }

    /**
     * Checks if a potential move leads to a collision with a wall or obstacle.
     * It considers only '.' (Empty) and 'S' (Speed Ramp) as safe.
     */
    private boolean isSafe(int r, int c) {
        if (arenaGrid == null) return false; 
        
        // Check bounds (40x40 grid)
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) {
            return false; // Out of bounds is not safe (hits border wall)
        }
        
        char targetCell = arenaGrid[r][c];
        
        // Treat anything that isn't empty or a speed ramp as unsafe (walls, obstacles, trails).
        if (targetCell != '.' && targetCell != 'S') {
            return false; 
        }

        return true;
    }

    /**
     * Implement AI movement logic for Koura (Random Movement).
     * Chooses a random safe direction that is not a 180-degree turn.
     */
    @Override
    public Direction decideMove() {
        // 1. Define the direction opposite to the current one
        Direction reverseDir = switch (currentDirection) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case WEST -> Direction.EAST;
            case EAST -> Direction.WEST;
        };

        // 2. Find all non-reverse safe moves
        List<Direction> safeMoves = new ArrayList<>();
        
        for (Direction dir : Direction.values()) {
            if (dir == reverseDir) continue; // Skip 180-degree turn
            
            int[] coords = getNextCoords(dir);
            int nextR = coords[0];
            int nextC = coords[1];
            
            if (isSafe(nextR, nextC)) {
                safeMoves.add(dir);
            }
        }
        
        // 3. Make a random choice
        if (!safeMoves.isEmpty()) {
            Collections.shuffle(safeMoves);
            return safeMoves.get(0);
        } 
        
        // 4. Last resort: if trapped, maintain current direction (will crash next step)
        return currentDirection;
    }
}