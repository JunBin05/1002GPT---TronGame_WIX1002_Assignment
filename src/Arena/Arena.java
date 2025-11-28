package arena;

import java.util.Random;

public abstract class Arena {
    final int ROWS = 40;
    final int COLS = 40;

    protected char[][] grid = new char[ROWS][COLS];
    protected Random rand = new Random();   // <-- FIX: now always initialized

    public Arena() {
        generateEmptyGrid();
        designArena(); // safe now
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
}
