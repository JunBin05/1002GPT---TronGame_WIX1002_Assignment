package designenemies;

import characters.Character;
import characters.Direction;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Enemy extends Character {

    protected boolean isEnemyBoss;
    protected char[][] arenaGrid;
    protected Random rand = new Random();

    private char trailSymbol = 'M';

    // Stats loaded from enemies.txt (legacy descriptor fields removed)
    // numeric defaults (populated from EnemyLoader) are used instead

    // CONSTRUCTOR
    public Enemy(String name, boolean isBoss) {
        super();

        // Look up stats from the Database/Text File
        EnemyLoader.EnemyStats stats = EnemyLoader.getStats(name, isBoss);

        // Safety Check: If file read fails, prevent crash
        if (stats == null) {
            System.err.println("Warning: Stats missing for " + name + ". Using defaults.");
            // Manual fallback
            stats = new EnemyLoader.EnemyStats();
            stats.name = name;
            stats.rank = isBoss ? "Boss" : "Minion";
            stats.color = "Gray";
        }

        // Overwrite the placeholder color with the real one from the file
        this.name = (stats != null && stats.name != null && !stats.name.isBlank()) ? stats.name : name;
        this.color = stats.color;
        this.trailSymbol = stats.getTrailSymbol();

        // Initialize per-instance numeric attributes from stats: use defaults here;
        // LevelManager applies per-tier values when spawning.

        // Boss Logic (Lives & Behavior flag)
        this.isEnemyBoss = isBoss;

        this.isBoss = isBoss; // Updates the Parent (Character.java)
        // Initialize role-based defaults he re because field initializers run before
        // the constructor.
        // This prevents the earlier bug where `isEnemyBoss` was false during field
        // initialization.

        if (isBoss) {
            this.lives = 3;
            this.maxLives = 3;
            // Default boss trail duration (can be tuned later by LevelManager)
            this.setTrailDuration(14);
        } else {
            this.lives = 1;
            this.maxLives = 1;
            // Default minion trail duration
            this.setTrailDuration(7);
        }

        // Safe fallbacks: initialize numeric attributes from stats (tier 0) so we don't
        // hardcode
        // concrete values inline in the class. LevelManager will typically override
        // these with
        // the appropriate tier values when spawning the enemy.
        int defaultTier = 0;
        if (stats != null) {
            this.speed = stats.getTierSpeed(defaultTier);
            this.handling = stats.getTierHandling(defaultTier);
            this.aggression = stats.getTierAggression(defaultTier);

        }
    }

    public void setArenaGrid(char[][] grid) {
        this.arenaGrid = grid;
    }

    public void spawnRandom(int rows, int cols) {
        this.r = (int) (Math.random() * rows);
        this.c = (int) (Math.random() * cols);
        if (arenaGrid != null) {
            while (arenaGrid[r][c] != '.') {
                this.r = (int) (Math.random() * rows);
                this.c = (int) (Math.random() * cols);
            }
        }
    }

    // Values are sourced from `EnemyLoader` at spawn time (per-tier). We leave them
    // unset here
    // to avoid hardcoding and let `LevelManager` apply per-tier tuning; constructor
    // sets safe fallbacks.
    private double speed;
    private double handling;
    private double aggression;

    // DECISION LOGIC
    public Direction decideMove() {
        if (this.isEnemyBoss) {
            // Boss uses aggression as the probability to execute the smart strategy
            if (rand.nextDouble() < this.aggression)
                return decideMoveSmart();
            return decideMoveStupid();
        } else {
            return decideMoveStupid();
        }
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double s) {
        this.speed = Math.max(0.0, s);
    }

    public double getHandling() {
        return this.handling;
    }

    public void setHandling(double h) {
        this.handling = Math.max(0.0, Math.min(1.0, h));
    }

    public double getAggression() {
        return this.aggression;
    }

    public void setAggression(double a) {
        this.aggression = Math.max(0.0, Math.min(1.0, a));
    }

    public char getTrailSymbol() {
        return this.trailSymbol;
    }

    public void setTrailSymbol(char symbol) {
        this.trailSymbol = symbol;
    }

    // moveDelayMs: desired delay between moves in milliseconds
    private long moveDelayNs = 0L; // stored internally as nanoseconds
    private long lastMoveNs = 0L; // last move timestamp

    public void setMoveDelayMs(long ms) {
        this.moveDelayNs = Math.max(0L, ms) * 1_000_000L;
        this.lastMoveNs = System.nanoTime();
    }

    public long getMoveDelayMs() {
        return this.moveDelayNs / 1_000_000L;
    }

    public long getMoveDelayNs() {
        return this.moveDelayNs;
    }

    public long getLastMoveNs() {
        return this.lastMoveNs;
    }

    public void setLastMoveNs(long ns) {
        this.lastMoveNs = ns;
    }

    // STRATEGY 1: SMART (Boss)
    private Direction decideMoveSmart() {
        int[] straight = getNextCoords(currentDirection);
        if (isBossSafe(straight[0], straight[1]))
            return currentDirection;

        Direction right = getTurn(currentDirection, true);
        int[] rCoords = getNextCoords(right);
        if (isBossSafe(rCoords[0], rCoords[1]))
            return right;

        Direction left = getTurn(currentDirection, false);
        int[] lCoords = getNextCoords(left);
        if (isBossSafe(lCoords[0], lCoords[1]))
            return left;

        // Last resort: turn back if it's the only safe exit
        Direction back = getOpposite(currentDirection);
        int[] backCoords = getNextCoords(back);
        if (isBossSafe(backCoords[0], backCoords[1]))
            return back;

        return currentDirection;
    }

    // STRATEGY 2: STUPID (Minion)
    private Direction decideMoveStupid() {
        int[] straight = getNextCoords(currentDirection);

        // Momentum: probability to keep moving straight equals 'handling' (higher
        // handling => more precise)
        if (isMinionSafe(straight[0], straight[1]) && rand.nextDouble() < this.handling) {
            return currentDirection;
        }

        // Random Turn
        List<Direction> validMoves = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d == getOpposite(currentDirection))
                continue;
            int[] next = getNextCoords(d);
            if (isMinionSafe(next[0], next[1])) {
                validMoves.add(d);
            }
        }

        if (!validMoves.isEmpty()) {
            return validMoves.get(rand.nextInt(validMoves.size()));
        }

        // Dead-end: allow reverse if it's at least not a wall/obstacle
        Direction back = getOpposite(currentDirection);
        int[] backCoords = getNextCoords(back);
        if (isMinionSafe(backCoords[0], backCoords[1]))
            return back;

        return currentDirection;
    }

    // HELPERS
    private int[] getNextCoords(Direction dir) {
        int nextR = this.r;
        int nextC = this.c;
        switch (dir) {
            case NORTH -> nextR--;
            case SOUTH -> nextR++;
            case WEST -> nextC--;
            case EAST -> nextC++;
        }
        return new int[] { nextR, nextC };
    }

    private boolean isBossSafe(int r, int c) {
        if (arenaGrid == null)
            return false;
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length)
            return false;
        char cell = arenaGrid[r][c];
        // Boss avoids everything dangerous
        return cell == '.' || cell == 'S';
    }

    private boolean isMinionSafe(int r, int c) {
        if (arenaGrid == null)
            return false;
        if (r < 0 || r >= arenaGrid.length || c < 0 || c >= arenaGrid[0].length)
            return false;
        char cell = arenaGrid[r][c];
        // Minion walks into Player Tails ('T') but avoids walls
        if (cell == '#' || cell == 'O' || cell == 'M') {
            return false;
        }
        return true;
    }

    private Direction getTurn(Direction d, boolean right) {
        if (right)
            return switch (d) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
            };
        return switch (d) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
        };
    }

    private Direction getOpposite(Direction d) {
        return switch (d) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    @Override
    public void levelUp() {
    }

    @Override
    public void setDirection(char directionInput) {
    }

    // GETTERS
    public String getName() {
        return this.name;
    }

    public boolean isBoss() {
        return this.isEnemyBoss;
    }

    // Enemy-specific XP (per-enemy xp field removed; XP is awarded according to
    // TronRules)

    @Override
    public int getTrailDuration() {
        return super.getTrailDuration();
    }

    public double getSpeedModifier() {
        return this.isEnemyBoss ? 1.5 : 1.0;
    }

    public String getBossIndicator() {
        return this.isEnemyBoss ? " [BOSS]" : "";
    }

    public String getColorString() {
        return this.color;
    }
}