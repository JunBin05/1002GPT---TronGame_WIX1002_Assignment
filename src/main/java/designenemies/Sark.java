package designenemies;

import characters.Direction; // FIXED: Import the correct enum

public class Sark extends Enemy { // FIXED: Removed 'abstract'

    public Sark(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }
    
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
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) return false; 
        char targetCell = arenaGrid[r][c];
        return targetCell == '.' || targetCell == 'S';
    }
    
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

    @Override
    public Direction decideMove() {
        if (isSafe(getNextCoords(currentDirection)[0], getNextCoords(currentDirection)[1])) {
            return currentDirection;
        }
        Direction rightDir = getRightTurn(currentDirection);
        if (isSafe(getNextCoords(rightDir)[0], getNextCoords(rightDir)[1])) {
            return rightDir;
        }
        Direction leftDir = getLeftTurn(currentDirection);
        if (isSafe(getNextCoords(leftDir)[0], getNextCoords(leftDir)[1])) {
            return leftDir;
        }
        return currentDirection;
    }
}