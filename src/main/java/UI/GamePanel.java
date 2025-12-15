package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // SCREEN SETTINGS
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int screenHeight = (int) screenSize.getHeight();
    
    // GAME LOOP
    Thread gameThread;
    int FPS = 60;

    // SYSTEM
    CutsceneManager cutscene = new CutsceneManager();
    
    // STATES
    public int gameState;
    public final int TITLE_STATE = 0;
    public final int PLAY_STATE = 1;
    public final int CUTSCENE_STATE = 2;
    
    // LEVEL PROGRESSION
    public int currentLevel = 1;
    public boolean levelCompleteTriggered = false; // Helper for "Win" condition

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);
    }

    public void setupGame() {
        // START WITH INTRO CUTSCENE
        gameState = CUTSCENE_STATE;
        cutscene.startScene("c1level1.txt"); // <--- MAKE SURE THIS FILENAME IS CORRECT
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        // --- 1. CUTSCENE MODE ---
        if (gameState == CUTSCENE_STATE) {
            if (!cutscene.isActive) {
                gameState = PLAY_STATE;
                System.out.println("Cutscene finished. Starting Level " + currentLevel);
            }
            return;
        }

        // --- 2. PLAY MODE ---
        if (gameState == PLAY_STATE) {
            
            // player.update(); <--- Your movement code goes here
            
            // CHECK WIN CONDITION (Traffic Controller)
            if (isLevelFinished()) {
                levelCompleteTriggered = false; 

                // CASE: Level 1 Finished -> Go to Level 2 (with cutscene?)
                if (currentLevel == 1) {
                    gameState = CUTSCENE_STATE; 
                    cutscene.startScene("c1level2a.txt"); // Change to your real filename
                    currentLevel = 2; 
                }
                
                // CASE: Level 2 Finished -> Go to Level 3
                else if (currentLevel == 2) {
                    // Example: No cutscene here, just straight to next level
                    currentLevel = 3; 
                    // player.resetPosition(); // IMPORTANT: Reset player!
                }
                
                // CASE: Level 3 Finished -> Ending
                else if (currentLevel == 3) {
                    gameState = CUTSCENE_STATE;
                    cutscene.startScene("C5_Ending.txt");
                }
            }
        }
    }

    public boolean isLevelFinished() {
        return levelCompleteTriggered;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // DRAW CUTSCENE (Pass 'this' so GIFs animate!)
        if (gameState == CUTSCENE_STATE) {
            cutscene.draw(g, screenWidth, screenHeight, this);
            return;
        }

        // DRAW GAME
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("LEVEL " + currentLevel, 100, 100);
        g.drawString("Press ENTER to Simulate Win", 100, 150);
        g.drawString("Press ESC to Quit", 100, 200);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ESCAPE) System.exit(0);

        // CUTSCENE CONTROLS
        if (gameState == CUTSCENE_STATE) {
            if (key == KeyEvent.VK_SPACE) {
                cutscene.advance();
                repaint();
            }
        }

        // GAMEPLAY CONTROLS
        if (gameState == PLAY_STATE) {
            if (key == KeyEvent.VK_W) System.out.println("Up");
            
            // CHEAT KEY TO WIN LEVEL
            if (key == KeyEvent.VK_ENTER) {
                levelCompleteTriggered = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}