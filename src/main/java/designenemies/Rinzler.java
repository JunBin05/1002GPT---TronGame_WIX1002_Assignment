package designenemies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import characters.Direction; // FIXED: Import the correct enum

public class Rinzler extends Enemy { // FIXED: Removed 'abstract'

    public Rinzler(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }
    
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
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length) return false; 
        char targetCell = arenaGrid[r][c];
        return targetCell == '.' || targetCell == 'S';
    }
    
    private int scoreDirection(Direction dir) {
        int score = 0;
        int r = this.y;
        int c = this.x;
        
        for (int i = 0; i < arenaGrid.length; i++) {
            int[] coords = getNextCoords(dir, r, c);
            r = coords[0];
            c = coords[1];
            if (isSafe(r, c)) score++;
            else break; 
        }
        return score;
    }

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
        
        if (maxScore > 0) {
            Collections.shuffle(bestDirections);
            return bestDirections.get(0);
        } 
        
        if (isSafe(getNextCoords(currentDirection, y, x)[0], getNextCoords(currentDirection, y, x)[1])) {
            return currentDirection;
        }
        return currentDirection;
    }
}