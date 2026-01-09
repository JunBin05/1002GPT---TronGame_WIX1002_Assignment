package arena;

import java.util.Random;

public abstract class Arena {
    final int ROWS = 40;
    final int COLS = 40;

    protected char[][] grid = new char[ROWS][COLS];
    // baseGrid stores the immutable underlying tiles (walls, speed ramps) defined
    // at design time
    protected char[][] baseGrid = new char[ROWS][COLS];
    protected Random rand = new Random();
    protected int[][] trailTimer = new int[ROWS][COLS];

    public Arena() {
        generateEmptyGrid();
        designArena();

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                baseGrid[r][c] = grid[r][c];
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

    /**
     * Returns the base (design-time) tile at the given location. This is used
     * to restore speed ramps and other static tiles when dynamic overlays are
     * removed (e.g., trail decay, enemy death).
     */
    public char getBaseTile(int r, int c) {
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS)
            return '.';
        return baseGrid[r][c];
    }
}
