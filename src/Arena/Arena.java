package arena;

import java.util.Random;

public abstract class Arena {
    final int ROWS = 40;
    final int COLS = 40;

    protected char[][] grid = new char[ROWS][COLS];
    protected Random rand = new Random();   // <-- FIX: now always initialized
    protected int[][] trailTimer = new int[ROWS][COLS];

    public Arena() {
        generateEmptyGrid();
        designArena(); // safe now
        initializeTrailTimer();
    }

    private void initializeTrailTimer() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                trailTimer[r][c] = 0; // Initialize all timers to 0
            }
        }
    }

    private void generateEmptyGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = '.';
            }
        }
    }

    protected abstract void designArena();

    public char[][] getGrid() {
        return grid;
    }

    public int[][] getTrailTimer() {
        return trailTimer;
    }
}
