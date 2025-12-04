package designenemies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import characters.Direction; // FIXED: Import the correct enum

public class Koura extends Enemy { // FIXED: Removed 'abstract'

    public Koura(String[] data) {
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

    @Override
    public Direction decideMove() {
        Direction reverseDir = switch (currentDirection) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case WEST -> Direction.EAST;
            case EAST -> Direction.WEST;
        };

        List<Direction> safeMoves = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (dir == reverseDir) continue;
            int[] coords = getNextCoords(dir);
            if (isSafe(coords[0], coords[1])) {
                safeMoves.add(dir);
            }
        }
        
        if (!safeMoves.isEmpty()) {
            Collections.shuffle(safeMoves);
            return safeMoves.get(0);
        } 
        return currentDirection;
    }
}