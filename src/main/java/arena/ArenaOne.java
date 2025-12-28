package arena;

public class ArenaOne extends Arena {

    @Override
    protected void designArena() {
        // Outer walls
        for (int i = 0; i < ROWS; i++) {
            grid[i][0] = '#';
            grid[i][COLS - 1] = '#';
            grid[0][i] = '#';
            grid[ROWS - 1][i] = '#';
        }

        // Speed ramps (S)
        for (int i = 5; i < 35; i += 6) {
            grid[10][i] = 'S';
            grid[30][i] = 'S';
        }

        // Obstacles (O) arranged like circuit nodes
        for (int i = 8; i <= 32; i += 8) {
            for (int j = 8; j <= 32; j += 8) {
                grid[i][j] = 'O';
            }
        }
    }
}
