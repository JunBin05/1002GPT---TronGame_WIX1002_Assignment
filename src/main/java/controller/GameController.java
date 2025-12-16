package controller;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList; // <- added

import arena.Arena;
import arena.ArenaLoader;
import arena.Disc;
import characters.Character;
import characters.Direction;
import designenemies.Enemy;
import XPSystem.TronRules;

public class GameController implements KeyListener, Runnable {

    private final JFrame gameFrame;
    private final Arena arena;
    private final List<Character> cycles;
    private final Map<String, ImageIcon> icons;
    private final JPanel arenaPanel;
    private final JPanel hudPanel;
    private final Character playerCycle;

    // changed to CopyOnWriteArrayList to avoid concurrent modification when adding discs from key events
    private List<Disc> activeDiscs = new CopyOnWriteArrayList<>();
    private volatile boolean isRunning = true;

    private boolean onSpeedRamp = false;
    private final int BASE_DELAY = 100; // Base speed in milliseconds
    private final int FAST_DELAY = 20;

    private int globalStepCounter = 1;
    private static final int TRAIL_DURATION = 7;
    private static final int BOSS_TRAIL_DURATION = 14; // Boss trails last 2x longer
    private static final int DISC_THROW_DISTANCE = 6;

    public GameController(JFrame frame, Arena arena, List<Character> cycles, Map<String, ImageIcon> icons,
                          JPanel arenaPanel, JPanel hudPanel) {
        this.gameFrame = frame;
        this.arena = arena;
        this.cycles = cycles;
        this.icons = icons;
        this.arenaPanel = arenaPanel;
        this.hudPanel = hudPanel;
        this.playerCycle = cycles.get(0);        this.gameFrame.addKeyListener(this);
        this.gameFrame.setFocusable(true);
    }

    public Character getPlayer() { return this.playerCycle; }
    public void stopGame() {
        isRunning = false;
        try {
            if (this.gameFrame != null) {
                this.gameFrame.removeKeyListener(this);
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    @Override
    public void run() {
        char[][] grid = arena.getGrid();
        int[][] trailTimer = arena.getTrailTimer();

        List<Character> deadEnemies = new ArrayList<>();
        int enemyMoveTick = 0;

        while (isRunning) {

            moveDiscs(grid);
            moveDiscs(grid);
            movePlayer(grid, trailTimer);

            enemyMoveTick++;
            for (Character c : cycles) {
                if (c != playerCycle && c instanceof Enemy) {
                    Enemy enemy = (Enemy) c;
                    
                    // Boss enemies move faster (every 2 ticks instead of 3)
                    int moveInterval = enemy.isBoss() ? 2 : 3;
                    if (enemyMoveTick % moveInterval != 0) continue;

                    Direction nextMove = enemy.decideMove();
                    enemy.currentDirection = nextMove;

                    int nextR = enemy.getRow();
                    int nextC = enemy.getCol();
                    switch (nextMove) {
                        case NORTH -> nextR--; case SOUTH -> nextR++;
                        case EAST -> nextC++; case WEST -> nextC--;
                    }

                        boolean wallHit = false;

                        if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) { wallHit = true; }
                        else {
                            char tile = grid[nextR][nextC];
                            // Treat Tron and Kevin tails as normal wall hits (no instant kill)
                            if (tile != '.' && tile != 'S') wallHit = true;
                        }

                        if (wallHit) {
                            enemy.changeLives(-0.5);
                            if (enemy.getLives() <= 0) {
                                awardKillXp(enemy); // STORES XP
                                deadEnemies.add(enemy);
                                if (enemy.getRow()>=0 && enemy.getRow()<40 && enemy.getCol()>=0 && enemy.getCol()<40) {
                                    grid[enemy.getRow()][enemy.getCol()] = '.';
                                }
                            } else {
                                enemy.revertPosition(grid, trailTimer);
                                enemy.setOppositeDirection();
                            }
                        } else {
                            enemy.advancePosition(grid);
                            int eRow = enemy.getRow();
                            int eCol = enemy.getCol();
                            if (eRow >= 0 && eRow < 40 && eCol >= 0 && eCol < 40) {
                                if (grid[eRow][eCol] == '.') {
                                char trailChar = 'M'; // Default minion trail
                                    String name = enemy.getName();

                                if (name.contains("Clu"))        trailChar = 'C'; // Gold
                                else if (name.contains("Sark"))  trailChar = 'Y'; // Yellow
                                else if (name.contains("Koura")) trailChar = 'G'; // Green
                                else if (name.contains("Rinzler")) trailChar = 'R'; // Red
                                
                                grid[eRow][eCol] = trailChar;
                                trailTimer[eRow][eCol] = globalStepCounter;
                                }
                            }
                        }
                    }
                }

            if (!deadEnemies.isEmpty()) {
                cycles.removeAll(deadEnemies);
                deadEnemies.clear();
            }

            SwingUtilities.invokeLater(() -> {
                ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons,
                                        this.arenaPanel, this.hudPanel);
            });

            if (this.playerCycle.getLives() <= 0.0) {
                SwingUtilities.invokeLater(() -> ArenaLoader.showGameOverDialog(this.gameFrame));
                break;
            }

            // --- STAGE CLEAR LOGIC ---
            if (cycles.size() == 1 && cycles.get(0) == playerCycle) {
                isRunning = false;

                // 1. Commit XP and Level Up NOW (at end of stage)
                String summaryHtml = playerCycle.commitPendingXP();

                // 2. Show the Nice Popup
                SwingUtilities.invokeLater(() -> {
                    // Use a JLabel to render the HTML properly
                    JLabel messageLabel = new JLabel(summaryHtml);
                    JOptionPane.showMessageDialog(gameFrame, messageLabel, "Stage Complete", JOptionPane.PLAIN_MESSAGE);

                    // 3. Load next level
                    ArenaLoader.showLevelCompleteDialog();
                });
            }

            handleTrailDecay(grid, trailTimer);

            try {
                // --- NEW SMOOTHER SPEED FORMULA ---
                double speedVal = playerCycle.getSpeed();
                int speedAdjustment = (int)((speedVal - 1.0) * 30); // 30ms reduction per 1.0 speed
                int dynamicDelay = BASE_DELAY - speedAdjustment;

                if (dynamicDelay < 30) dynamicDelay = 30; // Hard cap so it's never instant

                int currentSpeed = onSpeedRamp ? FAST_DELAY : dynamicDelay;
                Thread.sleep(currentSpeed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void awardKillXp(Enemy enemy) {
        TronRules.EnemyType type = TronRules.EnemyType.MINION;
        if (enemy.name.contains("Sark")) type = TronRules.EnemyType.SARK;
        else if (enemy.name.contains("Clu")) type = TronRules.EnemyType.CLU;
        else if (enemy.name.contains("Rinzler")) type = TronRules.EnemyType.RINZLER;
        else if (enemy.name.contains("Koura")) type = TronRules.EnemyType.KOURA;

        long reward = TronRules.calculateEnemyXp(playerCycle.getLevel(), type);

        // --- CHANGED: Call storeXP instead of addXP ---
        playerCycle.addXP(reward);
    }

    private void moveDiscs(char[][] grid) {
    for (int i = activeDiscs.size() - 1; i >= 0; i--) {
        Disc disc = activeDiscs.get(i);

        // Restore the tile the disc was previously occupying
        if (disc.r >= 0 && disc.r < 40 && disc.c >= 0 && disc.c < 40) {
            if (grid[disc.r][disc.c] == 'D') {
                grid[disc.r][disc.c] = disc.getOriginalTile();
            }
        }

        // Check if disc has already traveled max distance BEFORE moving again
        boolean stopFlying = false;
        if (disc.distanceTraveled >= DISC_THROW_DISTANCE) {
            stopFlying = true;
        }

        if (!stopFlying) {
            int nextR = disc.r;
            int nextC = disc.c;

            // Movement
            switch (disc.dir) {
                case NORTH -> nextR--;
                case SOUTH -> nextR++;
                case EAST  -> nextC++;
                case WEST  -> nextC--;
            }

            // Check for boundaries and obstacles
            if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) {
                stopFlying = true;
            } else if (grid[nextR][nextC] == '#' || grid[nextR][nextC] == 'O') {
                stopFlying = true;
            } else {
                // Safe to move: update position and distance
                disc.r = nextR;
                disc.c = nextC;
                disc.distanceTraveled++;

                // Remember what tile is at this cell
                char tileHere = grid[nextR][nextC];
                disc.setOriginalTile(tileHere);
            }
        }

        // Always show disc as 'D' while it's active (whether flying or stopped)
        grid[disc.r][disc.c] = 'D';
    }
}


    private void movePlayer(char[][] grid, int[][] trailTimer) {
        int futureR = this.playerCycle.r; int futureC = this.playerCycle.c;
        switch (this.playerCycle.currentDirection) { case NORTH -> futureR--; case SOUTH -> futureR++; case EAST -> futureC++; case WEST -> futureC--; }
        char element = ' '; boolean collided = false;
        if (futureR < 0 || futureR >= 40 || futureC < 0 || futureC >= 40) { this.playerCycle.changeLives(-this.playerCycle.getLives()); collided = true; }
        else {
            element = grid[futureR][futureC];
            if (element == 'D') {
                // Pickup: restore the tile underneath the disc instead of blindly writing '.'
                Disc picked = null;
                for (Disc d : activeDiscs) {
                    if (d.r == futureR && d.c == futureC) { picked = d; break; }
                }
                this.playerCycle.pickupDisc();
                if (picked != null) {
                    grid[futureR][futureC] = picked.getOriginalTile();
                    activeDiscs.remove(picked);
                    element = grid[futureR][futureC];
                } else {
                    grid[futureR][futureC] = '.';
                    element = '.';
                }
            }
            // Allow stepping onto player's own trail/symbol (e.g., 'T' for Tron, 'K' for Kevin)
            if (element != '.' && element != 'S' && element != this.playerCycle.getSymbol()) collided = true;
        }
        if (collided) {
            this.playerCycle.changeLives(-0.5);
            if (this.playerCycle.getLives() > 0.0) { this.playerCycle.revertPosition(grid, trailTimer); this.playerCycle.setOppositeDirection(); }
        } else {
            if (onSpeedRamp) grid[this.playerCycle.r][this.playerCycle.c] = 'S';
            else { grid[this.playerCycle.r][this.playerCycle.c] = this.playerCycle.getSymbol(); trailTimer[this.playerCycle.r][this.playerCycle.c] = globalStepCounter; }
            if (element == 'S') onSpeedRamp = true; else onSpeedRamp = false;
            this.globalStepCounter++; this.playerCycle.advancePosition(grid);
        }
    }

    private void handleTrailDecay(char[][] grid, int[][] trailTimer) {
        for (int r = 0; r < 40; r++) { for (int c = 0; c < 40; c++) {
            char currentElement = grid[r][c];
            if (currentElement == 'T' || currentElement == 'K') {
                int placementStep = trailTimer[r][c];
                if (placementStep > 0 && (globalStepCounter - placementStep) >= TRAIL_DURATION) { grid[r][c] = '.'; trailTimer[r][c] = 0; }
            } else if (currentElement == 'M' || currentElement == 'C' || 
                     currentElement == 'Y' || currentElement == 'G' || 
                     currentElement == 'R') {
                // Enemy trails - check if it's a boss (longer duration)
                // For now use BOSS_TRAIL_DURATION for all enemies - bosses will be explicitly marked
                int placementStep = trailTimer[r][c];
                if (placementStep > 0) {
                    // Check if any active cycle is a boss at this position - use longer duration
                    int trailDuration = TRAIL_DURATION;
                    for (Character cycle : cycles) {
                        if (cycle instanceof Enemy) {
                            Enemy enemy = (Enemy) cycle;
                            if (enemy.isBoss()) {
                                trailDuration = BOSS_TRAIL_DURATION;
                                break;
                            }
                        }
                    }
                    if ((globalStepCounter - placementStep) >= trailDuration) { grid[r][c] = '.'; trailTimer[r][c] = 0; }
                }
            }
        }}
    }

    private void throwDiscAction() {
        if (!this.playerCycle.hasDisc()) {
            return;
        }
        this.playerCycle.throwDisc();
        Disc newDisc = new Disc(this.playerCycle.r, this.playerCycle.c, this.playerCycle.currentDirection, DISC_THROW_DISTANCE);
        // Remember what tile is under the disc at spawn
        try {
            char[][] grid = arena.getGrid();
            if (this.playerCycle.r >= 0 && this.playerCycle.r < 40 && this.playerCycle.c >= 0 && this.playerCycle.c < 40) {
                newDisc.setOriginalTile(grid[this.playerCycle.r][this.playerCycle.c]);
                grid[this.playerCycle.r][this.playerCycle.c] = 'D';
            }
        } catch (Exception ex) {
            // ignore
        }
        activeDiscs.add(newDisc);
        SwingUtilities.invokeLater(() -> { ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, arenaPanel, hudPanel); });
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        char key = java.lang.Character.toUpperCase(e.getKeyChar());
        if (key == 'W' || key == 'S' || key == 'A' || key == 'D') this.playerCycle.setDirection(key);
        // Support Arrow keys as well for better UX
        if (e.getKeyCode() == KeyEvent.VK_LEFT) this.playerCycle.setDirection('A');
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) this.playerCycle.setDirection('D');
        if (e.getKeyCode() == KeyEvent.VK_UP) this.playerCycle.setDirection('W');
        if (e.getKeyCode() == KeyEvent.VK_DOWN) this.playerCycle.setDirection('S');
        if (e.getKeyCode() == KeyEvent.VK_SPACE) throwDiscAction();
    }
    @Override public void keyReleased(KeyEvent e) {}
}