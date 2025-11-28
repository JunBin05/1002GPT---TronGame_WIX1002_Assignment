package arena;

public class ArenaTwo extends Arena {

    @Override
    protected void designArena() {

        // 1. Outer border walls (with holes)
        for (int i = 0; i < ROWS; i++) {
            grid[i][0] = '#';
            grid[i][COLS - 1] = '#';
        }
        for (int i = 0; i < COLS; i++) {
            grid[0][i] = '#';
            grid[ROWS - 1][i] = '#';
        }

        // Add holes in the border (fixed positions, not random)
        grid[0][20] = '.';     // top hole
        grid[20][0] = '.';     // left hole
        grid[39][20] = '.';    // bottom hole
        grid[20][39] = '.';    // right hole


        // --------------------------------------------------
        // 2. Slither.io - style snake curves inside (OBSTACLES ONLY)
        // --------------------------------------------------

        // left wavy snake
        for (int r = 5; r <= 34; r++) {
            if (r % 6 == 0) grid[r][8] = 'O';
            if (r % 6 == 3) grid[r][10] = 'O';
        }

        // right wavy snake
        for (int r = 5; r <= 34; r++) {
            if (r % 6 == 0) grid[r][30] = 'O';
            if (r % 6 == 3) grid[r][28] = 'O';
        }

        // middle ring (slither-like loop)
        grid[15][18] = 'O';
        grid[15][19] = 'O';
        grid[16][20] = 'O';
        grid[18][20] = 'O';
        grid[19][19] = 'O';
        grid[19][18] = 'O';
        grid[18][17] = 'O';
        grid[16][17] = 'O';


        // --------------------------------------------------
        // 3. Slither-style “food pellets” (speed ramps)
        // --------------------------------------------------

        // cluster 1
        grid[10][10] = 'S';
        grid[11][10] = 'S';
        grid[10][11] = 'S';

        // cluster 2
        grid[28][10] = 'S';
        grid[29][10] = 'S';
        grid[28][11] = 'S';

        // cluster 3
        grid[10][28] = 'S';
        grid[11][28] = 'S';
        grid[10][29] = 'S';

        // cluster 4
        grid[28][28] = 'S';
        grid[29][28] = 'S';
        grid[28][29] = 'S';

        // long line of pellets across center
        for (int c = 14; c <= 26; c += 3) {
            grid[20][c] = 'S';
        }
    }
}
