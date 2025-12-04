package controller;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Map;

// Import all necessary classes from other packages
import arena.Arena; 
import arena.ArenaLoader; 
import characters.Character; 
import characters.Tron; 
import characters.Direction;
// import characters.Kevin; // Kevin is likely not used directly if loaded via generic list, but keeping it is fine.
import designenemies.Enemy; // REQUIRED: Added to identify and move enemies

public class GameController implements KeyListener, Runnable {
    
    private final JFrame gameFrame;
    private final Arena arena;
    private final List<Character> cycles;
    private final Map<String, ImageIcon> icons;
    private final JPanel arenaPanel; 
    private final JPanel hudPanel;   
    private final Tron playerCycle; 
    
    // --- FIELDS ---
    private boolean onSpeedRamp = false; // Flag to remember if we are on a ramp
    private final int NORMAL_DELAY = 80; // Normal speed
    private final int FAST_DELAY = 10;   // Turbo speed
    
    private int globalStepCounter = 1; 
    private static final int TRAIL_DURATION = 7; 

    // --- CONSTRUCTOR ---
    public GameController(JFrame frame, Arena arena, List<Character> cycles, Map<String, ImageIcon> icons,
                          JPanel arenaPanel, JPanel hudPanel) {
        this.gameFrame = frame;
        this.arena = arena;
        this.cycles = cycles;
        this.icons = icons;
        this.arenaPanel = arenaPanel; 
        this.hudPanel = hudPanel;     
        this.playerCycle = (Tron) cycles.get(0); // Assumes player is always index 0
        this.gameFrame.addKeyListener(this); 
        this.gameFrame.setFocusable(true);   
    }

    // --- GAME LOOP ---
    @Override
    public void run() {
        char[][] grid = arena.getGrid();
        int[][] trailTimer = arena.getTrailTimer();
        
        while (true) {
            
            // 1. MOVE PLAYER
            movePlayer(grid, trailTimer);

            // 2. MOVE ENEMIES
            // Iterate through all characters. If it's an enemy, let AI decide and move.
            for (Character c : cycles) {
                if (c != playerCycle && c instanceof Enemy) {
                    Enemy enemy = (Enemy) c;
                    
                    // A. AI decides where to go
                    Direction nextMove = enemy.decideMove();
                    
                    // B. Update Enemy Position internally (x, y, r, c)
                    enemy.applyMove(nextMove);
                    
                    // C. Update the Grid (Enemy leaves a trail 'K')
                    int eRow = enemy.getRow();
                    int eCol = enemy.getCol();
                    
                    // Boundary check to ensure we don't crash the array
                    if (eRow >= 0 && eRow < 40 && eCol >= 0 && eCol < 40) {
                         // Only place a trail if the cell is currently empty.
                         // This prevents overwriting walls or other critical elements if the AI glitches.
                         if (grid[eRow][eCol] == '.') {
                             grid[eRow][eCol] = 'K'; // 'K' for Killer/Enemy Trail
                             trailTimer[eRow][eCol] = globalStepCounter;
                         }
                    }
                }
            }
            
            // 3. REDRAW
            SwingUtilities.invokeLater(() -> {
                ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, 
                                        this.arenaPanel, this.hudPanel);
            });

            // 4. GAME OVER CHECK
            if (this.playerCycle.getLives() <= 0.0) {
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.showGameOverDialog(this.gameFrame);
                });
                break; // Exit the loop
            }
            
            // 5. DECAY LOGIC (Remove old trails)
            handleTrailDecay(grid, trailTimer);
            
            // 6. CONTROL SPEED
            try {
                int currentSpeed = onSpeedRamp ? FAST_DELAY : NORMAL_DELAY;
                Thread.sleep(currentSpeed); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // --- HELPER METHODS ---

    /**
     * Handles the movement, collision detection, and grid updates for the human player.
     */
    private void movePlayer(char[][] grid, int[][] trailTimer) {
        // A. Determine Next Position
        int futureR = this.playerCycle.r;
        int futureC = this.playerCycle.c;

        switch (this.playerCycle.currentDirection) {
            case NORTH -> futureR--; 
            case SOUTH -> futureR++; 
            case EAST -> futureC++; 
            case WEST -> futureC--; 
        }

        // B. Check Collision
        char element = ' '; 
        boolean collided = false;
        
        // Boundary Check
        if (futureR < 0 || futureR >= 40 || futureC < 0 || futureC >= 40) {
            this.playerCycle.changeLives(-this.playerCycle.getLives()); // Instant death on wall hit
            collided = true;
        } 
        
        // Obstacle/Tile Check
        if (!collided) {
            element = grid[futureR][futureC]; 

            // WHITELIST: You are SAFE on Empty (.), Your Tail (T), and Speed Ramps (S).
            // You DIE on Walls (#), Obstacles (O), or Enemy Trails (K).
            if (element != '.' && element != 'T' && element != 'S') {
                collided = true;
            }
        }
        
        // C. Execute Move
        if (collided) {
            // Collision Logic
            this.playerCycle.changeLives(-0.5); 
            System.out.println(this.playerCycle.name + " hit barrier. Lives: " + this.playerCycle.getLives());
            
            if (this.playerCycle.getLives() > 0.0) {
                this.playerCycle.revertPosition(grid, trailTimer); 
                this.playerCycle.setOppositeDirection();
            }
            // Note: Redraw is handled in the main loop
            
        } else {
            // Safe Move Logic
            
            // 1. Restore the grid behind us (leaving the current cell)
            if (onSpeedRamp) {
                grid[this.playerCycle.r][this.playerCycle.c] = 'S'; // Leave ramp intact
            } else {
                grid[this.playerCycle.r][this.playerCycle.c] = this.playerCycle.getSymbol(); // 'T'
                trailTimer[this.playerCycle.r][this.playerCycle.c] = globalStepCounter; // Start decay timer
            }

            // 2. Check if the NEW tile is a ramp
            if (element == 'S') {
                onSpeedRamp = true; 
            } else {
                onSpeedRamp = false; 
            }

            // 3. Update Player Coordinates
            this.globalStepCounter++;
            this.playerCycle.advancePosition(grid); 
        }
    }

    /**
     * Iterates over the grid and removes trails ('T' or 'K') that have expired.
     */
    private void handleTrailDecay(char[][] grid, int[][] trailTimer) {
        for (int r = 0; r < 40; r++) {
            for (int c = 0; c < 40; c++) {
                char currentElement = grid[r][c];
                
                if (currentElement == 'T' || currentElement == 'K') {
                    int placementStep = trailTimer[r][c];
                    // If enough steps have passed since placement, clear the cell
                    if (placementStep > 0 && (globalStepCounter - placementStep) >= TRAIL_DURATION) {
                        grid[r][c] = '.'; 
                        trailTimer[r][c] = 0; 
                    }
                }
            }
        }
    }
    
    // --- KeyListener Interface Methods ---
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        char key = java.lang.Character.toUpperCase(e.getKeyChar());
        if (key == 'W' || key == 'S' || key == 'A' || key == 'D') {
            this.playerCycle.setDirection(key);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
}