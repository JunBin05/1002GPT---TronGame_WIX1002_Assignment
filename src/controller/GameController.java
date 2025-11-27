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
    
    private static final int GAME_SPEED_MS = 80; // Controls game speed (80ms = 12.5 FPS)

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
        while (true) {
            
            // 1. UPDATE STATE (Movement)
            // advancePosition() handles the stun flag internally
            this.playerCycle.advancePosition(); 

            // --- COLLISION LOGIC ---
            int nextR = this.playerCycle.r;
            int nextC = this.playerCycle.c;
            char element = ' '; 
            
            // Ensure we check within the bounds before accessing the grid
            if (nextR >= 0 && nextC >= 0 && nextR < 40 && nextC < 40) {
                element = arena.getGrid()[nextR][nextC];
            }

            boolean collided = false;
            
            // A. Check for Boundary/Wall/Obstacle Collision (# or O)
            if (element == '#' || element == 'O') {
                this.playerCycle.changeLives(-0.5); 
                System.out.println(this.playerCycle.name + " hit obstacle. Lives: " + this.playerCycle.getLives());
                collided = true;
            } 
            
            // B. Check for Falling Off (Fatal Boundary Condition)
            if (nextR < 0 || nextR >= 40 || nextC < 0 || nextC >= 40) {
                this.playerCycle.changeLives(-this.playerCycle.getLives()); 
                System.out.println(this.playerCycle.name + " fell off the Grid! Lives: 0.0");
            }
            
            // C. REBOUND MECHANIC (Fixes teleporting/stuck state)
            if (collided && this.playerCycle.getLives() > 0.0) {
                this.playerCycle.revertPosition(); // Rolls back position and sets stun flag
                this.playerCycle.setOppositeDirection();
                
                // Force an immediate redraw to show the cycle rebounded and stunned
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, 
                                            this.arenaPanel, this.hudPanel);
                });
            }
            
            // D. Game Over Check
            if (this.playerCycle.getLives() <= 0.0) {
                System.out.println("GAME OVER!");
                
                // --- NEW: Trigger the dialog in a thread-safe way ---
                SwingUtilities.invokeLater(() -> {
                    // Pass the main game frame to the static dialog method
                    ArenaLoader.showGameOverDialog(this.gameFrame);
                });
                // ----------------------------------------------------
                
                break; // Exit the game loop thread
            }
            
            // 3. REDRAW FRAME (Only redraw if no collision or after the stun frame passes)
            if (!this.playerCycle.isStunned || !collided) {
                SwingUtilities.invokeLater(() -> {
                    ArenaLoader.redrawArena(this.gameFrame, this.arena, this.cycles, this.icons, 
                                            this.arenaPanel, this.hudPanel);
                });
            }

            // 4. CONTROL SPEED
            try {
                Thread.sleep(GAME_SPEED_MS); 
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