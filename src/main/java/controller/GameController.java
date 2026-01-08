package controller;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.ArrayList;
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

    // Player invulnerability window after collision to prevent rapid repeat hits
    private static final long PLAYER_IFRAME_NS = 900_000_000L; // 900 ms
    private long playerInvulnerableUntilNs = 0L;

    // changed to CopyOnWriteArrayList to avoid concurrent modification when adding discs from key events
    private List<Disc> activeDiscs = new CopyOnWriteArrayList<>();
    private volatile boolean isRunning = true;

    private boolean onSpeedRamp = false;
    // Multiplier to amplify speed differences (higher => more noticeable)
    private static final int SPEED_MULTIPLIER = 60; // tuned to make 0.5 vs 0.2 noticeably different
    private final int BASE_DELAY = 100; // Base speed in milliseconds
    private final int FAST_DELAY = 20;
    private static final long DISC_COOLDOWN_NS = 5_000_000_000L;
    private static final double COOLDOWN_BASE_S = 5.0;
    private static final double COOLDOWN_STEP_S = 0.5; // reduce 0.5s every LEVEL_STEP levels
    private static final int COOLDOWN_LEVEL_STEP = 10;

    private int globalStepCounter = 1;
    private static final int TRAIL_DURATION = 7;
    private static final int DISC_THROW_DISTANCE = 5;

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

        // Set enemy disc capacity based on chapter (c1=1, c2=2, ...)
        int enemyDiscCap = 1;
        for (Character ch : cycles) {
            if (ch instanceof Enemy enemy) {
                enemy.setDiscCapacity(enemyDiscCap);
                enemy.currentDiscCount = enemyDiscCap;
            }
        }
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

            moveDiscs(grid, deadEnemies);
            moveDiscs(grid, deadEnemies);
            movePlayer(grid, trailTimer);

            // Snapshot the grid for this tick so all enemies read a consistent view (pre-commit)
            char[][] readGrid = copyGrid(grid);

            long nowNs = System.nanoTime();
            for (Character c : cycles) {
                if (c != playerCycle && c instanceof Enemy) {
                    Enemy enemy = (Enemy) c;

                    // Enemy disc throw: line-of-sight to player within throw distance and not on cooldown
                    tryEnemyThrow(enemy, grid);

                    processEnemyMove(enemy, readGrid, grid, trailTimer, nowNs, deadEnemies);
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

    // Discs stop and persist when hitting a player/enemy; enemy deaths are staged in deadEnemies
    private void moveDiscs(char[][] grid, List<Character> deadEnemies) {
        for (int i = activeDiscs.size() - 1; i >= 0; i--) {
            Disc disc = activeDiscs.get(i);

            // Restore the tile the disc was previously occupying
            if (disc.r >= 0 && disc.r < 40 && disc.c >= 0 && disc.c < 40) {
                if (grid[disc.r][disc.c] == 'D' || grid[disc.r][disc.c] == 'E') {
                    grid[disc.r][disc.c] = disc.getOriginalTile();
                }
            }

            // Check if disc has already traveled max distance BEFORE moving again
            boolean stopFlying = disc.distanceTraveled >= DISC_THROW_DISTANCE;
            boolean collided = false;

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

                // Bounds / walls
                if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) {
                    stopFlying = true;
                } else if (grid[nextR][nextC] == '#' || grid[nextR][nextC] == 'O') {
                    stopFlying = true;
                } else {
                    // Body collision check (player or enemy bodies are tracked in cycles, not grid)
                    Character hit = null;
                    for (Character c : cycles) {
                        if (c.getRow() == nextR && c.getCol() == nextC) { hit = c; break; }
                    }
                    if (hit != null) {
                        long now = System.nanoTime();
                        if (hit.isPlayer) {
                            if (disc.owner != null && !disc.owner.isPlayer && now >= playerInvulnerableUntilNs) {
                                playerCycle.changeLives(-1.0);
                                playerCycle.setStunned(true);
                                playerInvulnerableUntilNs = now + PLAYER_IFRAME_NS;
                            }
                        } else if (hit instanceof Enemy enemyHit) {
                            if (disc.owner != null && disc.owner.isPlayer) {
                                enemyHit.changeLives(-1.0);
                                if (enemyHit.getLives() <= 0) {
                                    deadEnemies.add(enemyHit);
                                    restoreBaseTile(grid, enemyHit.getRow(), enemyHit.getCol());
                                }
                            }
                        }

                        // Land the disc on the collision cell and freeze further movement
                        collided = true;
                        stopFlying = true;
                        disc.r = nextR;
                        disc.c = nextC;
                        disc.setOriginalTile(grid[nextR][nextC]);
                        disc.distanceTraveled = DISC_THROW_DISTANCE; // cap so it won't advance again
                    } else {
                        // No collision: move forward
                        disc.r = nextR;
                        disc.c = nextC;
                        disc.distanceTraveled++;
                        disc.setOriginalTile(grid[nextR][nextC]);
                    }
                }
            }

            // Keep disc visible as 'D' (player) or 'E' (enemy) while active
            char discChar = (disc.owner != null && !disc.owner.isPlayer) ? 'E' : 'D';
            grid[disc.r][disc.c] = discChar;
        }
    }

    private void processEnemyMove(Enemy enemy, char[][] readGrid, char[][] grid, int[][] trailTimer,
                                  long nowNs, List<Character> deadEnemies) {
        // Time-based scheduling per enemy
        final long defaultEnemyDelay = 250_000_000L; // 250 ms
        long configuredDelay = enemy.getMoveDelayNs() > 0 ? enemy.getMoveDelayNs() : defaultEnemyDelay;
        if (!isOutOfBounds(enemy.getRow(), enemy.getCol()) && readGrid[enemy.getRow()][enemy.getCol()] == 'S') {
            configuredDelay = FAST_DELAY; // speed ramp boost
        }

        if (enemy.getLastMoveNs() == 0L) {
            enemy.setLastMoveNs(nowNs);
            return; // initialize and skip this tick
        }
        if ((nowNs - enemy.getLastMoveNs()) < configuredDelay) return;

        Direction nextMove = enemy.decideMove();
        int nextR = enemy.getRow();
        int nextC = enemy.getCol();
        switch (nextMove) { case NORTH -> nextR--; case SOUTH -> nextR++; case EAST -> nextC++; case WEST -> nextC--; }

        boolean wallHit = false;
        boolean hitDisc = false;
        boolean blockedByOtherEnemy = false;
        Disc discAtTarget = null;

        if (isOutOfBounds(nextR, nextC)) {
            // Out-of-bounds => instant death (mirror player behavior)
            enemy.changeLives(-enemy.getLives());
            deadEnemies.add(enemy);
            if (!isOutOfBounds(enemy.getRow(), enemy.getCol())) restoreBaseTile(grid, enemy.getRow(), enemy.getCol());
            return;
        } else {
            // Occupied by another enemy
            for (Character other : cycles) {
                if (other == enemy) continue;
                if (other instanceof Enemy && other.getRow() == nextR && other.getCol() == nextC) {
                    blockedByOtherEnemy = true;
                    wallHit = true;
                    break;
                }
            }

            if (!blockedByOtherEnemy) {
                char tile = readGrid[nextR][nextC];
                if (tile == 'D' || tile == 'E') discAtTarget = findDiscAt(nextR, nextC);

                if ((tile == 'D' || tile == 'E') && discAtTarget != null && discAtTarget.owner != null && discAtTarget.owner.isPlayer) {
                    wallHit = true;
                    hitDisc = true;
                }

                // Enemy steps into player
                if (tile == playerCycle.getSymbol() && playerCycle.getRow() == nextR && playerCycle.getCol() == nextC) {
                    long now = System.nanoTime();
                    if (now >= playerInvulnerableUntilNs) {
                        playerCycle.changeLives(-0.5);
                        playerInvulnerableUntilNs = now + PLAYER_IFRAME_NS;
                        playerCycle.isStunned = true;
                    }
                    enemy.changeLives(-0.5);
                    if (enemy.getLives() <= 0) {
                        deadEnemies.add(enemy);
                        if (!isOutOfBounds(enemy.getRow(), enemy.getCol())) restoreBaseTile(grid, enemy.getRow(), enemy.getCol());
                    } else {
                        enemy.setOppositeDirection();
                    }
                    return; // handled collision
                }

                // Trails and walls
                if (tile != '.' && tile != 'S' && tile != 'D' && tile != 'E') {
                    char ownTrail = enemy.getTrailSymbol();
                    if (tile == ownTrail) {
                        // allow own trail
                    } else if (isEnemyTrail(tile)) {
                        blockedByOtherEnemy = true;
                        wallHit = true;
                    } else {
                        wallHit = true;
                    }
                }

                if (!wallHit && (tile == 'D' || tile == 'E') && discAtTarget != null && discAtTarget.owner != null && !discAtTarget.owner.isPlayer) {
                    pickupDiscForEnemy(enemy, discAtTarget, grid);
                }
            }
        }

        if (wallHit) {
            if (blockedByOtherEnemy) {
                enemy.setOppositeDirection();
            } else {
                double dmg = hitDisc ? -1.0 : -0.5;
                enemy.changeLives(dmg);
                if (enemy.getLives() <= 0) {
                    deadEnemies.add(enemy);
                    if (!isOutOfBounds(enemy.getRow(), enemy.getCol())) {
                        restoreBaseTile(grid, enemy.getRow(), enemy.getCol());
                        System.out.println(String.format("[GameController] Restored base tile after enemy death at (%d,%d): %s", enemy.getRow(), enemy.getCol(), enemy.getName()));
                    }
                } else {
                    enemy.setOppositeDirection();
                }
            }
            final long bounceNs = 80_000_000L; // 80 ms
            enemy.setLastMoveNs(nowNs - configuredDelay + bounceNs);
            return;
        }

        // Successful move
        enemy.currentDirection = nextMove;
        enemy.advancePosition(grid);
        enemy.setLastMoveNs(nowNs);
        if (!isOutOfBounds(enemy.getRow(), enemy.getCol()) && grid[enemy.getRow()][enemy.getCol()] == 'S') {
            ArenaLoader.appendGameplayLog("Enemy " + enemy.getName() + " hit speed ramp");
        }
        placeEnemyTrail(enemy, grid, trailTimer);
    }


    private Disc findDiscAt(int r, int c) {
        for (Disc d : activeDiscs) {
            if (d.r == r && d.c == c) return d;
        }
        return null;
    }

    private void pickupDiscForEnemy(Enemy picker, Disc disc, char[][] grid) {
        // Restore underlying tile
        if (grid[disc.r][disc.c] == 'D' || grid[disc.r][disc.c] == 'E') {
            grid[disc.r][disc.c] = disc.getOriginalTile();
        }
        activeDiscs.remove(disc);

        // Choose recipient: prefer self if space, else first teammate with space
        Enemy recipient = null;
        if (picker.currentDiscCount < picker.getDiscCapacity()) {
            recipient = picker;
        } else {
            for (Character ch : cycles) {
                if (ch instanceof Enemy teammate) {
                    if (teammate.currentDiscCount < teammate.getDiscCapacity()) {
                        recipient = teammate;
                        break;
                    }
                }
            }
        }

        if (recipient != null) {
            recipient.pickupDisc();
            ArenaLoader.appendGameplayLog("Enemy " + picker.getName() + " picked up a disc" +
                    (recipient != picker ? " for " + recipient.getName() : ""));
        }
    }

    private void movePlayer(char[][] grid, int[][] trailTimer) {
        // Try to apply any pending player direction (handling-dependent)
        try { this.playerCycle.tryApplyPendingDirection(grid); } catch (Exception ignored) {}

        int futureR = this.playerCycle.r; int futureC = this.playerCycle.c;
        switch (this.playerCycle.currentDirection) { case NORTH -> futureR--; case SOUTH -> futureR++; case EAST -> futureC++; case WEST -> futureC--; }
        char element = ' '; boolean collided = false; boolean hitEnemyDisc = false;
        if (futureR < 0 || futureR >= 40 || futureC < 0 || futureC >= 40) { this.playerCycle.changeLives(-this.playerCycle.getLives()); collided = true; }
        else {
            element = grid[futureR][futureC];
            if (element == 'D' || element == 'E') {
                Disc picked = findDiscAt(futureR, futureC);
                if (picked != null && picked.owner != null && !picked.owner.isPlayer) {
                    // Enemy disc damages player on contact, disc stays in place
                    this.playerCycle.changeLives(-1.0);
                    ArenaLoader.pulseLog(new Color(0, 180, 255));
                    this.playerCycle.setStunned(true);
                    hitEnemyDisc = true;
                    collided = true;
                    element = 'E';
                } else {
                    // Player disc or unknown disc: pick up
                    this.playerCycle.pickupDisc();
                    char base = arena.getBaseTile(futureR, futureC);
                    char restore = (base != '\0') ? base : '.';
                    grid[futureR][futureC] = restore;
                    element = restore;
                    if (picked != null) activeDiscs.remove(picked);
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
            if (!hitEnemyDisc && element != '.' && element != 'S' && element != this.playerCycle.getSymbol()) collided = true;
        }
        if (collided) {
            if (hitEnemyDisc) {
                // Already applied enemy disc damage; stop movement without extra penalty
                this.playerCycle.setOppositeDirection();
            } else {
                long now = System.nanoTime();
                    if (now >= playerInvulnerableUntilNs) {
                        this.playerCycle.changeLives(-0.5);
                        playerInvulnerableUntilNs = now + PLAYER_IFRAME_NS;
                        if (this.playerCycle.getLives() > 0.0) {
                            this.playerCycle.setOppositeDirection();
                            this.playerCycle.isStunned = true;
                        }
                    } else {
                        // Ignore damage during i-frames; still bounce direction to avoid sticking
                    this.playerCycle.setOppositeDirection();
                }
            }
        } else {
            // Always lay trail (hide ramps while occupied); decay logic will restore base tile (including 'S') later
            grid[this.playerCycle.r][this.playerCycle.c] = this.playerCycle.getSymbol();
            trailTimer[this.playerCycle.r][this.playerCycle.c] = globalStepCounter;
            onSpeedRamp = (element == 'S');
            if (onSpeedRamp && playerCycle.isPlayer) {
                ArenaLoader.appendGameplayLog("Entered speed ramp (SPD: " + String.format("%.2f", playerCycle.getSpeed()) + ")");
            }
            this.globalStepCounter++; this.playerCycle.advancePosition(grid);
        }
    }

    private boolean hasLineOfSight(Character enemy, Character target, char[][] grid) {
        if (enemy.getRow() == target.getRow()) {
            int row = enemy.getRow();
            int start = Math.min(enemy.getCol(), target.getCol()) + 1;
            int end = Math.max(enemy.getCol(), target.getCol()) - 1;
            int distance = Math.abs(enemy.getCol() - target.getCol());
            if (distance > DISC_THROW_DISTANCE) return false;
            for (int c = start; c <= end; c++) {
                char cell = grid[row][c];
                if (cell == '#' || cell == 'O') return false;
            }
            return true;
        }
        if (enemy.getCol() == target.getCol()) {
            int col = enemy.getCol();
            int start = Math.min(enemy.getRow(), target.getRow()) + 1;
            int end = Math.max(enemy.getRow(), target.getRow()) - 1;
            int distance = Math.abs(enemy.getRow() - target.getRow());
            if (distance > DISC_THROW_DISTANCE) return false;
            for (int r = start; r <= end; r++) {
                char cell = grid[r][col];
                if (cell == '#' || cell == 'O') return false;
            }
            return true;
        }
        return false;
    }

    private void tryEnemyThrow(Enemy enemy, char[][] grid) {
        if (!enemy.hasDisc()) return;
        long now = System.nanoTime();
        if (now < enemy.getNextDiscReadyNs()) return;
        if (!hasLineOfSight(enemy, playerCycle, grid)) return;
        attemptThrowDisc(enemy);
    }

    private long computePlayerCooldownNs(Character thrower) {
        if (thrower == null || !thrower.isPlayer) return DISC_COOLDOWN_NS;
        int level = thrower.getLevel();
        int steps = Math.max(0, level / COOLDOWN_LEVEL_STEP);
        double cooldownSeconds = COOLDOWN_BASE_S - (steps * COOLDOWN_STEP_S);
        return (long) (cooldownSeconds * 1_000_000_000L);
    }

    private void attemptThrowDisc(Character thrower) {
        long now = System.nanoTime();
        if (now < thrower.getNextDiscReadyNs()) {
            if (thrower.isPlayer) ArenaLoader.setDiscCooldownEnd(thrower.getNextDiscReadyNs());
            return;
        }
        if (!thrower.hasDisc()) return;

        long cooldownNs = thrower.isPlayer ? computePlayerCooldownNs(thrower) : DISC_COOLDOWN_NS;
        thrower.setNextDiscReadyNs(now + cooldownNs);
        if (thrower.isPlayer) ArenaLoader.setDiscCooldownEnd(thrower.getNextDiscReadyNs());

        thrower.throwDisc();
        Disc newDisc = new Disc(thrower, thrower.r, thrower.c, thrower.currentDirection, DISC_THROW_DISTANCE);
        try {
            char[][] gridRef = arena.getGrid();
            if (thrower.r >= 0 && thrower.r < 40 && thrower.c >= 0 && thrower.c < 40) {
                newDisc.setOriginalTile(gridRef[thrower.r][thrower.c]);
                char discTile = (thrower instanceof Enemy) ? 'E' : 'D';
                gridRef[thrower.r][thrower.c] = discTile;
            }
        } catch (Exception ex) {
            // ignore
        }
        activeDiscs.add(newDisc);
        if (thrower instanceof Enemy enemyThrower) {
            ArenaLoader.appendGameplayLog("Enemy " + enemyThrower.getName() + " threw a disc");
        }
        SwingUtilities.invokeLater(() -> { ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, arenaPanel, hudPanel); });
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
                        if (currentElement == enemy.getTrailSymbol()) {
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
        attemptThrowDisc(this.playerCycle);
    }

    // --- Small helpers to keep the main loop readable ---
    private boolean isOutOfBounds(int r, int c) {
        return (r < 0 || r >= 40 || c < 0 || c >= 40);
    }

    private boolean isEnemyTrail(char tile) {
        return tile == 'M' || tile == 'C' || tile == 'Y' || tile == 'G' || tile == 'R';
    }

    private void restoreBaseTile(char[][] grid, int r, int c) {
        char base = arena.getBaseTile(r, c);
        grid[r][c] = (base != '.' && base != '\0') ? base : '.';
    }

    private void placeEnemyTrail(Enemy enemy, char[][] grid, int[][] trailTimer) {
        int r = enemy.getRow();
        int c = enemy.getCol();
        if (isOutOfBounds(r, c)) return;
        char trailChar = enemy.getTrailSymbol();
        grid[r][c] = trailChar;
        trailTimer[r][c] = globalStepCounter;
    }

    private char[][] copyGrid(char[][] src) {
        char[][] copy = new char[src.length][src[0].length];
        for (int r = 0; r < src.length; r++) {
            System.arraycopy(src[r], 0, copy[r], 0, src[r].length);
        }
        return copy;
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