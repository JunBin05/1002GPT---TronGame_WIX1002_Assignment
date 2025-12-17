package arena;

public class ArenaThree extends Arena {

    @Override
    protected void designArena() {

        // 1. Outer border walls
        for (int i = 0; i < ROWS; i++) {
            grid[i][0] = '#';
            grid[i][COLS - 1] = '#';
            grid[0][i] = '#';
            grid[ROWS - 1][i] = '#';
        }

        // 2. Pikachu pixel art in center
        int startR = 10;
        int startC = 10;

        // Outline box (head)
        for (int r = 0; r < 20; r++) {
            grid[startR + r][startC] = '#';
            grid[startR + r][startC + 19] = '#';
        }
        for (int c = 0; c < 20; c++) {
            grid[startR][startC + c] = '#';
            grid[startR + 19][startC + c] = '#';
        }
        grid[15][10]='.';
        grid[15][29]='.';

        // Ears
        grid[startR - 2][startC + 3] = '#';
        grid[startR - 1][startC + 3] = '#';
        grid[startR - 2][startC + 16] = '#';
        grid[startR - 1][startC + 16] = '#';

        // Eyes (speed ramps)
        grid[startR + 6][startC + 6] = 'S';
        grid[startR + 6][startC + 13] = 'S';

        // Cheeks (obstacles)
        grid[startR + 10][startC + 4]  = 'O';
        grid[startR + 10][startC + 15] = 'O';

        // Nose
        grid[startR + 8][startC + 9] = 'O';

        // Mouth
        grid[startR + 12][startC + 8]  = '#';
        grid[startR + 12][startC + 10] = '#';

        // ---------------------------------------------------------
        // 3. REDUCED, FIXED SPEED RAMPS (S)
        // ---------------------------------------------------------

        // Top left & right
        grid[6][8]  = 'S';
        grid[6][30] = 'S';

        // Bottom left & right
        grid[33][8]  = 'S';
        grid[33][30] = 'S';

        // Middle-left & middle-right
        grid[20][5]  = 'S';
        grid[20][34] = 'S';

        // Top middle & bottom middle
        grid[6][20]  = 'S';
        grid[33][20] = 'S';

        // ---------------------------------------------------------
        // 4. FUN, FIXED OBSTACLES (O)
        // ---------------------------------------------------------

        // Four around the corners of Pikachu
        grid[8][8]   = 'O';
        grid[8][30]  = 'O';
        grid[31][8]  = 'O';
        grid[31][30] = 'O';

        // Four farther out for balance
        grid[15][5]  = 'O';
        grid[25][5]  = 'O';
        grid[15][34] = 'O';
        grid[25][34] = 'O';
    }
}
