package arena;

public class RandomArena extends Arena {

    @Override
    protected void designArena() {

        // 1. Create outer walls
        for (int i = 0; i < ROWS; i++) {
            grid[i][0] = '#';
            grid[i][COLS - 1] = '#';
            grid[0][i] = '#';
            grid[ROWS - 1][i] = '#';
        }

        // 2. Add 1–3 RANDOM HOLES in the wall (trap gaps)
        int holes = rand.nextInt(3) + 1; // 1 to 3 holes

        for (int h = 0; h < holes; h++) {
            int side = rand.nextInt(4); // 0=top,1=bottom,2=left,3=right
            int pos = rand.nextInt(38) + 1; // avoid corners

            switch (side) {
                case 0 -> grid[0][pos] = '.';       // top wall hole
                case 1 -> grid[ROWS - 1][pos] = '.'; // bottom
                case 2 -> grid[pos][0] = '.';       // left
                case 3 -> grid[pos][COLS - 1] = '.'; // right
            }
        }

        // 3. Add random obstacles
        for (int i = 0; i < 35; i++) {
            int r = rand.nextInt(ROWS - 2) + 1; // inside only
            int c = rand.nextInt(COLS - 2) + 1;
            grid[r][c] = 'O';
        }

        // 4. Add speed ramps
        for (int i = 0; i < 12; i++) {
            int r = rand.nextInt(ROWS - 2) + 1;
            int c = rand.nextInt(COLS - 2) + 1;
            grid[r][c] = 'S';
        }
    }
}
