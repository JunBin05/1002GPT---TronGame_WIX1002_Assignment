package designenemies;

/**
 * Sark enemy class.
 * Subclass of Enemy representing Sark type with medium speed and moderate intelligence level.
 * Implements a predictable, wall-following (or always-turn-right) AI pattern.
 */
public class Sark extends Enemy {

    /**
     * Constructor that initializes a Sark object using data from enemies.txt
     * @param data - String array containing enemy attributes.
     */
    public Sark(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }
    
    // --- Helper Methods (Same as Koura.java) ---

    private int[] getNextCoords(Direction dir) {
        int nextR = this.y; 
        int nextC = this.x; 
        
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
    
    // --- New Helper for Sark's Turn Logic ---
    private Direction getRightTurn(Direction currentDir) {
        return switch (currentDir) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
        };
    }

    private Direction getLeftTurn(Direction currentDir) {
        return switch (currentDir) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
        };
    }

    /**
     * Implement AI movement logic for Sark (Predictable Pattern).
     * Prioritizes moving straight, then turning right, then turning left.
     */
    @Override
    public Direction decideMove() {
        // 1. Check straight ahead
        if (isSafe(getNextCoords(currentDirection)[0], getNextCoords(currentDirection)[1])) {
            return currentDirection;
        }

        // 2. Check right turn
        Direction rightDir = getRightTurn(currentDirection);
        if (isSafe(getNextCoords(rightDir)[0], getNextCoords(rightDir)[1])) {
            return rightDir;
        }
        
        // 3. Check left turn
        Direction leftDir = getLeftTurn(currentDirection);
        if (isSafe(getNextCoords(leftDir)[0], getNextCoords(leftDir)[1])) {
            return leftDir;
        }
        
        // 4. Last resort: if trapped, maintain current direction (will crash next step)
        return currentDirection;
    }
}