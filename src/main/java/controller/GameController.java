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
import characters.Kevin; 

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
    private final int FAST_DELAY = 20;   // Turbo speed
    
    // FIX: Start at 1 so the first block decays correctly
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
        this.playerCycle = (Tron) cycles.get(0); 
        this.gameFrame.addKeyListener(this); 
        this.gameFrame.setFocusable(true);   
    }

    // --- GAME LOOP ---
    @Override
    public void run() {
        char[][] grid = arena.getGrid();
        int[][] trailTimer = arena.getTrailTimer();
        
        while (true) {
            
            // 1. DETERMINE NEXT POSITION
            int futureR = this.playerCycle.r;
            int futureC = this.playerCycle.c;

            switch (this.playerCycle.currentDirection) {
                case NORTH -> futureR--; 
                case SOUTH -> futureR++; 
                case EAST -> futureC++; 
                case WEST -> futureC--; 
            }

            // 2. CHECK COLLISION
            char element = ' '; 
            boolean collided = false;
            
            // A. Boundary Check
            if (futureR < 0 || futureR >= 40 || futureC < 0 || futureC >= 40) {
                this.playerCycle.changeLives(-this.playerCycle.getLives()); 
                collided = true;
            } 
            
            // B. Obstacle Check
            if (!collided && futureR >= 0 && futureC >= 0 && futureR < 40 && futureC < 40) {
                element = grid[futureR][futureC]; 

                // WHITELIST: You are SAFE on Empty (.), Your Tail (T), and Speed Ramps (S).
                // You DIE on Walls (#), Obstacles (O), or Enemy Trails (K).
                if (element != '.' && element != 'T' && element != 'S') {
                    collided = true;
                }
            }
            
            // 3. EXECUTE MOVE
            if (collided) {
                // --- COLLISION LOGIC ---
                this.playerCycle.changeLives(-0.5); 
                System.out.println(this.playerCycle.name + " hit barrier. Lives: " + this.playerCycle.getLives());
                
                if (this.playerCycle.getLives() > 0.0) {
                    this.playerCycle.revertPosition(grid, trailTimer); 
                    this.playerCycle.setOppositeDirection();
                }
                
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, 
                                            this.arenaPanel, this.hudPanel);
                });
                
            } else {
                // --- SAFE MOVE LOGIC ---

                // A. Restore the grid behind us
                // If we are leaving a Speed Ramp, put the 'S' back.
                // If we are leaving normal ground, put a Trail 'T'.
                if (onSpeedRamp) {
                    grid[this.playerCycle.r][this.playerCycle.c] = 'S';
                } else {
                    grid[this.playerCycle.r][this.playerCycle.c] = this.playerCycle.getSymbol(); // 'T'
                    trailTimer[this.playerCycle.r][this.playerCycle.c] = globalStepCounter; // Start decay timer
                }

                // B. Check if the NEW tile is a ramp
                if (element == 'S') {
                    onSpeedRamp = true; // Turn on Turbo Mode
                } else {
                    onSpeedRamp = false; // Back to Normal
                }

                // C. Move the Character
                this.globalStepCounter++;
                this.playerCycle.advancePosition(grid); 
                
                // D. Redraw
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, 
                                            this.arenaPanel, this.hudPanel);
                });
            }

            // 4. GAME OVER CHECK
            if (this.playerCycle.getLives() <= 0.0) {
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.showGameOverDialog(this.gameFrame);
                });
                break; 
            }
            
            // 5. JETWALL DECAY LOGIC
            for (int r = 0; r < 40; r++) {
                for (int c = 0; c < 40; c++) {
                    char currentElement = grid[r][c];
                    
                    if (currentElement == 'T' || currentElement == 'K') {
                        int placementStep = trailTimer[r][c];
                        if (placementStep > 0 && (globalStepCounter - placementStep) >= TRAIL_DURATION) {
                            grid[r][c] = '.'; 
                            trailTimer[r][c] = 0; 
                        }
                    }
                }
            }
            
            // 6. CONTROL SPEED
            try {
                // Ternary Operator: If onSpeedRamp is true, use FAST_DELAY (20), else use NORMAL (80)
                int currentSpeed = onSpeedRamp ? FAST_DELAY : NORMAL_DELAY;
                Thread.sleep(currentSpeed); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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