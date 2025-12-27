package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.JFrame;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // SCREEN SETTINGS
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int screenHeight = (int) screenSize.getHeight();
    
    // GAME LOOP
    Thread gameThread;
    int FPS = 60;

    // SYSTEM
    public CutsceneManager cutscene = new CutsceneManager();

    // Convenience API for other packages to start cutscenes without accessing the field
    public void startCutscene(String filename) {
        if (cutscene != null) {
            // Ensure the panel is in cutscene state so draw() will render the scene
            this.gameState = CUTSCENE_STATE;
            cutscene.startScene(filename);
        }
    }

    // Overload that allows the caller to specify whether NEXT_FILE chaining is allowed.
    public void startCutscene(String filename, boolean allowChaining) {
        if (cutscene != null) {
            this.gameState = CUTSCENE_STATE;
            cutscene.startScene(filename, allowChaining);
        }
    }
    
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

    // Overload that accepts a parent frame (e.g., MainFrame) so callers can pass
    // the frame and the panel can attach a KeyListener to it as a fallback.
    public void startGameThread(JFrame parentFrame) {
        if (parentFrame != null) {
            parentFrame.addKeyListener(this);
        }
        startGameThread();
    }

    /**
     * Stops the game thread cleanly. This causes the run loop to exit.
     */
    public void stopGameThread() {
        if (gameThread != null) {
            Thread t = gameThread;
            gameThread = null; // run() loop will exit
            try { t.interrupt(); } catch (Exception ignored) {}
        }
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
        // Overlay text removed per request: no lines shown during PLAY_STATE
        // If needed later, re-enable specific messages via config or a debug flag.
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
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}