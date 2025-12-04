package controller;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Map;

// Import all necessary classes from other packages
import arena.Arena; 
import arena.ArenaLoader; // To call the static redraw method
import characters.Character; 
import characters.Tron; 
import characters.Direction;
import characters.Kevin; 

// Implements KeyListener for input and Runnable for the game loop thread
public class GameController implements KeyListener, Runnable {
    
    private final JFrame gameFrame;
    private final Arena arena;
    private final List<Character> cycles;
    private final Map<String, ImageIcon> icons;
    private final JPanel arenaPanel; // Field for the grid panel
    private final JPanel hudPanel;   // Field for the HUD panel
    private final Tron playerCycle; 
    
    // --- JETWALL DECAY AND SPEED CONTROL FIELDS ---
    // Track if we are currently on a speed ramp
    private boolean onSpeedRamp = false;
    private int gameDelay = 80; // Controls game speed (12.5 FPS)
    private int globalStepCounter = 1; 
    private static final int TRAIL_DURATION = 7; // Jetwall fades after 7 steps
    // ----------------------------------------------

    // --- CORRECTED CONSTRUCTOR ---
    public GameController(JFrame frame, Arena arena, List<Character> cycles, Map<String, ImageIcon> icons,
                          JPanel arenaPanel, JPanel hudPanel) {
        this.gameFrame = frame;
        this.arena = arena;
        this.cycles = cycles;
        this.icons = icons;
        
        // Assign the new panels (Fixes the undefined constructor error)
        this.arenaPanel = arenaPanel; 
        this.hudPanel = hudPanel;     

        // Assume the first character in the list is the player (Tron)
        this.playerCycle = (Tron) cycles.get(0); 

        // Attach listener to the frame and ensure it can receive input focus
        this.gameFrame.addKeyListener(this); 
        this.gameFrame.setFocusable(true);   
    }

    // --- Game Loop Implementation (Heart of the Real-Time System) ---
    @Override
    public void run() {
        char[][] grid = arena.getGrid();
        int[][] trailTimer = arena.getTrailTimer();
        
        while (true) {
            
            // 1. DETERMINE NEXT POSITION (Hypothetical move)
            int futureR = this.playerCycle.r;
            int futureC = this.playerCycle.c;

            switch (this.playerCycle.currentDirection) {
                case NORTH -> futureR--; 
                case SOUTH -> futureR++; 
                case EAST -> futureC++; 
                case WEST -> futureC--; 
            }

            // --- 2. CHECK COLLISION at the hypothetical position ---
            char element = ' '; 
            boolean collided = false;
            
            // A. Check for Falling Off (Fatal Boundary Condition)
            if (futureR < 0 || futureR >= 40 || futureC < 0 || futureC >= 40) {
                this.playerCycle.changeLives(-this.playerCycle.getLives()); 
                collided = true;
            } 
            
            // B. Check within bounds before reading grid element
            if (!collided && futureR >= 0 && futureC >= 0 && futureR < 40 && futureC < 40) {
                element = grid[futureR][futureC]; 

                // 1. SPEED RAMP CHECK (New Code)
                if (element == 'S') {
                    // Decrease delay by 15ms to speed up (don't go below 20ms)
                    gameDelay = Math.max(20, gameDelay - 15); 
                    System.out.println("SPEED BOOST! Delay is now: " + gameDelay);
                }
                
                // Check for Wall, Obstacle, OR ANY JETWALL TRAIL (#, O, T, or K)
                if (element != '.' && element != 'T' && element != 'S') {
                    collided = true;
                }
            }
            
            // --- 3. EXECUTE MOVE OR REBOUND ---
            if (collided) {
                // Apply Damage and trigger rebound logic if surviving
                this.playerCycle.changeLives(-0.5); 
                System.out.println(this.playerCycle.name + " hit barrier. Lives: " + this.playerCycle.getLives());
                
                if (this.playerCycle.getLives() > 0.0) {
                    this.playerCycle.revertPosition(grid, trailTimer); // Rolls back and clears trail/sets stun
                    this.playerCycle.setOppositeDirection();
                }
                
                // Force immediate redraw to show damage/rebound
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, 
                                            this.arenaPanel, this.hudPanel);
                });
                
            } else {
                // C. EXECUTE SAFE MOVE (Only reached if NO COLLISION)

                // 1. Mark the CURRENT position (r, c) as the jetwall trail
                grid[this.playerCycle.r][this.playerCycle.c] = this.playerCycle.getSymbol(); 
                
                // 2. Mark the placement time for decay calculation
                trailTimer[this.playerCycle.r][this.playerCycle.c] = globalStepCounter; 

                // 3. Increment step counter
                this.globalStepCounter++;
                
                // 4. Move the cycle's position to the safe, new location
                // advancePosition now just moves r/c and handles the stun flag
                this.playerCycle.advancePosition(grid); 
                
                // Redraw the safe move
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
            
            // --- 5. JETWALL DECAY LOGIC (ERASURE) ---
            for (int r = 0; r < 40; r++) {
                for (int c = 0; c < 40; c++) {
                    char currentElement = grid[r][c];
                    
                    if (currentElement == 'T' || currentElement == 'K') {
                        int placementStep = trailTimer[r][c];
                        
                        // Check if the trail has expired (age is 7 or more steps)
                        if (placementStep > 0 && (globalStepCounter - placementStep) >= TRAIL_DURATION) {
                            grid[r][c] = '.'; // Erase the trail
                            trailTimer[r][c] = 0; // Reset the timer
                        }
                    }
                }
            }
            
            // 6. CONTROL SPEED
            try {
                Thread.sleep(gameDelay); 
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
        // Only set direction on WASD keys
        if (key == 'W' || key == 'S' || key == 'A' || key == 'D') {
            this.playerCycle.setDirection(key);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
}