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
    // Multiplier to amplify speed differences (higher => more noticeable)
    private static final int SPEED_MULTIPLIER = 60; // tuned to make 0.5 vs 0.2 noticeably different
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
        this.playerCycle = cycles.get(0);  
        this.playerCycle.isPlayer = true;      
        this.gameFrame.addKeyListener(this);
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

        while (isRunning) {

            moveDiscs(grid);
            moveDiscs(grid);
            movePlayer(grid, trailTimer);
            long nowNs = System.nanoTime();
            for (Character c : cycles) {
                if (c != playerCycle && c instanceof Enemy) {
                    Enemy enemy = (Enemy) c;

                    // Time-based scheduling: use configured per-enemy delay if provided, otherwise use a
                    // default delay so enemies remain independent from the game loop speed (e.g., player FPS).
                    boolean shouldMove = false;
                    final long defaultEnemyDelay = 250_000_000L; // 250 ms in nanoseconds
                    long configuredDelay = enemy.getMoveDelayNs() > 0 ? enemy.getMoveDelayNs() : defaultEnemyDelay;
                    // Guard: if lastMoveNs is zero, initialize it so enemies don't all burst on first tick
                    if (enemy.getLastMoveNs() == 0L) {
                        enemy.setLastMoveNs(nowNs);
                    } else if ((nowNs - enemy.getLastMoveNs()) >= configuredDelay) {
                        shouldMove = true;
                        enemy.setLastMoveNs(nowNs);
                    }

                    if (!shouldMove) continue;

                    Direction nextMove = enemy.decideMove();

                    int nextR = enemy.getRow();
                    int nextC = enemy.getCol();
                    switch (nextMove) {
                        case NORTH -> nextR--; case SOUTH -> nextR++;
                        case EAST -> nextC++; case WEST -> nextC--;
                    }

                        boolean wallHit = false;

                        boolean blockedByOtherEnemy = false;
                        if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) { 
                            wallHit = true; 
                        }
                        else {
                            // If any other enemy is currently at the target cell, mark as blocked but do NOT damage
                            for (Character other : cycles) {
                                if (other == enemy) continue;
                                if (other instanceof Enemy) {
                                    if (other.getRow() == nextR && other.getCol() == nextC) {
                                        blockedByOtherEnemy = true;
                                        wallHit = true;
                                        break;
                                    }
                                }
                            }

                            if (!blockedByOtherEnemy) {
                                char tile = grid[nextR][nextC];

                                // Special-case: enemy attempting to move into the player's cell
                                if (tile == playerCycle.getSymbol() && playerCycle.getRow() == nextR && playerCycle.getCol() == nextC) {
                                    System.out.println(String.format("[GameController] Enemy %s attempted to move onto player at (%d,%d)", enemy.getName(), nextR, nextC));

                                    // Minimal behavior change: enemy takes damage when attempting to move into the player
                                    enemy.changeLives(-0.5);
                                    if (enemy.getLives() <= 0) {
                                        // Enemy died: award XP and mark for removal
                                        deadEnemies.add(enemy);

                                        // Restore the design-time/base tile at enemy's position (if any)
                                        if (enemy.getRow()>=0 && enemy.getRow()<40 && enemy.getCol()>=0 && enemy.getCol()<40) {
                                            int er = enemy.getRow(); int ec = enemy.getCol();
                                            char base = arena.getBaseTile(er, ec);
                                            if (base != '.' && base != '\0') {
                                                grid[er][ec] = base;
                                                System.out.println(String.format("[GameController] Restored base tile '%c' at (%d,%d) after enemy death: %s", base, er, ec, enemy.getName()));
                                            } else {
                                                grid[er][ec] = '.';
                                            }
                                        }
                                    } else {
                                        // Enemy survived: turn it around
                                        enemy.setOppositeDirection();
                                    }

                                    // Skip the rest of the movement processing for this attempt
                                    continue;
                                }

                                // Treat Tron and Kevin tails as normal wall hits (no instant kill)
                                // If this is an enemy trail char (M/C/Y/G/R) consider it occupied so
                                // enemies will bounce instead of causing damage to each other.
                                if (tile != '.' && tile != 'S') {
                                    if (tile == 'M' || tile == 'C' || tile == 'Y' || tile == 'G' || tile == 'R') {
                                        blockedByOtherEnemy = true;
                                        wallHit = true;
                                    } else {
                                        wallHit = true;
                                    }
                                }
                            }
                        }

                        if (wallHit) {
                            if (blockedByOtherEnemy) {
                                // Other enemy occupies the cell: avoid damaging either side, just turn around
                                enemy.setOppositeDirection();
                                // Do not call revertPosition here because the enemy has not moved yet
                            } else {
                                enemy.changeLives(-0.5);
                                if (enemy.getLives() <= 0) {
                                    deadEnemies.add(enemy);
                                    if (enemy.getRow()>=0 && enemy.getRow()<40 && enemy.getCol()>=0 && enemy.getCol()<40) {
                                        int er = enemy.getRow(); int ec = enemy.getCol();
                                        // Restore the design-time/base tile if present, otherwise clear to '.'
                                        char base = arena.getBaseTile(er, ec);
                                        if (base != '.' && base != '\0') {
                                            grid[er][ec] = base;
                                            System.out.println(String.format("[GameController] Restored base tile '%c' at (%d,%d) after enemy death: %s", base, er, ec, enemy.getName()));
                                        } else {
                                            grid[er][ec] = '.';
                                        }
                                    }
                                } else {
                                    // The enemy did not move into the wall, so just turn it around
                                    enemy.setOppositeDirection();
                                }
                            }
                        } else {
                            // Movement allowed: commit the new direction and advance
                            enemy.currentDirection = nextMove;
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

                // 1. Award Stage Clear XP (if applicable), then commit XP and Level Up NOW (at end of stage)
                long stageXp = TronRules.calculateStageClearXp(ArenaLoader.currentChapter, ArenaLoader.currentStage);

                if (stageXp > 0) {
                    // Apply replay diminishing returns (Option A) so players far above a stage
                    // do not get excessive level jumps when replaying earlier content.
                    double replayMult = XPSystem.TronRules.calculateReplayMultiplier(playerCycle.getLevel(), ArenaLoader.currentChapter, ArenaLoader.currentStage);
                    long originalXp = stageXp;
                    if (replayMult < 0.9999) {
                        stageXp = Math.max(1, (int) Math.round(stageXp * replayMult));
                        System.out.println("[GameController] Stage XP replay multiplier applied: " + originalXp + " -> " + stageXp + " (mult=" + String.format("%.3f", replayMult) + ")");
                    }

                    playerCycle.addXP(stageXp);
                    System.out.println("[GameController] Stage Clear XP awarded: " + stageXp + " (C" + ArenaLoader.currentChapter + " S" + ArenaLoader.currentStage + ")");
                }

                String username = null;
                if (gameFrame instanceof UI.MainFrame) username = ((UI.MainFrame) gameFrame).getCurrentUsername();
                String summaryHtml = playerCycle.commitPendingXP(username);

                // Persist the player's XP (non-blocking)
                if (username != null && !username.trim().isEmpty()) {
                    final String userToSave = username;
                    new Thread(() -> {
                        try {
                            UI.DatabaseManager db = new UI.DatabaseManager();
                            if ("Tron".equalsIgnoreCase(playerCycle.name)) db.setTronXp(userToSave, playerCycle.getXp());
                            else if ("Kevin".equalsIgnoreCase(playerCycle.name)) db.setKevinXp(userToSave, playerCycle.getXp());
                        } catch (Exception e) { e.printStackTrace(); }
                    }).start();
                }

                // 2. Show the Nice Popup
                System.out.println("[GameController] Stage cleared. Preparing to show level complete dialog.");
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
                int speedAdjustment = (int)((speedVal - 1.0) * SPEED_MULTIPLIER); // larger multiplier makes differences more perceptible
                int dynamicDelay = BASE_DELAY - speedAdjustment;

                if (dynamicDelay < 30) dynamicDelay = 30; // Hard cap so it's never instant

                int currentSpeed = onSpeedRamp ? FAST_DELAY : dynamicDelay;
                Thread.sleep(currentSpeed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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
        // Try to apply any pending player direction (handling-dependent)
        try { this.playerCycle.tryApplyPendingDirection(grid); } catch (Exception ignored) {}

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
                    // Restore base tile when a disc disappears without a tracked original
                    char base = arena.getBaseTile(futureR, futureC);
                    grid[futureR][futureC] = (base != '\0') ? base : '.';
                    element = grid[futureR][futureC];
                }
            }

            // Special case: when the destination is a speed ramp 'S' it may still be occupied
            // by an enemy (enemies do not overwrite 'S' with their trail). Detect occupancy.
            if (element == 'S') {
                for (Character other : cycles) {
                    if (other == playerCycle) continue;
                    if (other.getRow() == futureR && other.getCol() == futureC) {
                        collided = true;
                        break;
                    }
                }
            }

            // Allow stepping onto player's own trail/symbol (e.g., 'T' for Tron, 'K' for Kevin)
            if (element != '.' && element != 'S' && element != this.playerCycle.getSymbol()) collided = true;
        }
        if (collided) {
            this.playerCycle.changeLives(-0.5);
            if (this.playerCycle.getLives() > 0.0) {
                // Provide the base tile of the current cell when reverting so it can be restored if needed
                char currentBase = arena.getBaseTile(this.playerCycle.r, this.playerCycle.c);
                this.playerCycle.revertPosition(grid, trailTimer, currentBase);
                // After revert, ensure the reverted cell also respects base tiles (e.g., speed ramps)
                char postBase = arena.getBaseTile(this.playerCycle.r, this.playerCycle.c);
                if (postBase != '\0' && postBase != '.') {
                    grid[this.playerCycle.r][this.playerCycle.c] = postBase;
                    if (postBase == 'S') onSpeedRamp = true;
                }
                this.playerCycle.setOppositeDirection();
            }
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
                if (placementStep > 0) {
                    int playerTrailDuration = TRAIL_DURATION;
                    if (cycles != null && !cycles.isEmpty()) playerTrailDuration = cycles.get(0).getTrailDuration();
                    if ((globalStepCounter - placementStep) >= playerTrailDuration) {
                        // Restore design-time tile if present, otherwise clear
                        char base = arena.getBaseTile(r, c);
                        grid[r][c] = (base != '\0') ? base : '.';
                        trailTimer[r][c] = 0;
                    }
                }
            } else if (currentElement == 'M' || currentElement == 'C' || 
                     currentElement == 'Y' || currentElement == 'G' || 
                     currentElement == 'R') {
                int placementStep = trailTimer[r][c];
                if (placementStep > 0) {
                    int trailDuration = TRAIL_DURATION; // default
                    // Try to find an enemy instance matching this trail char and use its trailDuration
                    for (Character cycle : cycles) {
                        if (!(cycle instanceof Enemy)) continue;
                        Enemy enemy = (Enemy) cycle;
                        String name = enemy.getName();
                        if ((currentElement == 'C' && name.contains("Clu")) ||
                            (currentElement == 'Y' && name.contains("Sark")) ||
                            (currentElement == 'G' && name.contains("Koura")) ||
                            (currentElement == 'R' && name.contains("Rinzler")) ||
                            (currentElement == 'M' && !enemy.isBoss())) {
                            trailDuration = enemy.getTrailDuration();
                            break;
                        }
                        // fallback: if any boss exists, prefer boss trail duration
                        if (enemy.isBoss()) trailDuration = Math.max(trailDuration, enemy.getTrailDuration());
                    }
                    if ((globalStepCounter - placementStep) >= trailDuration) {
                        char base = arena.getBaseTile(r, c);
                        grid[r][c] = (base != '\0') ? base : '.';
                        trailTimer[r][c] = 0;
                    }
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
        if (key == 'W' || key == 'S' || key == 'A' || key == 'D') this.playerCycle.requestDirection(key);
        // Support Arrow keys as well for better UX
        if (e.getKeyCode() == KeyEvent.VK_LEFT) this.playerCycle.requestDirection('A');
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) this.playerCycle.requestDirection('D');
        if (e.getKeyCode() == KeyEvent.VK_UP) this.playerCycle.requestDirection('W');
        if (e.getKeyCode() == KeyEvent.VK_DOWN) this.playerCycle.requestDirection('S');
        if (e.getKeyCode() == KeyEvent.VK_SPACE) throwDiscAction();
    }
    @Override public void keyReleased(KeyEvent e) {}
}