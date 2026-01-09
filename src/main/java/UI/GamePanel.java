package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.JFrame;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // Screen Settings
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int screenHeight = (int) screenSize.getHeight();

    // Game Loop
    Thread gameThread;
    int FPS = 60;

    // System
    public CutsceneManager cutscene = new CutsceneManager();

    public void startCutscene(String filename) {
        if (cutscene != null) {
            this.gameState = CUTSCENE_STATE;
            cutscene.startScene(filename);
        }
    }

    public void startCutscene(String filename, boolean allowChaining) {
        if (cutscene != null) {
            this.gameState = CUTSCENE_STATE;
            cutscene.startScene(filename, allowChaining);
        }
    }

    // States
    public int gameState;
    public final int TITLE_STATE = 0;
    public final int PLAY_STATE = 1;
    public final int CUTSCENE_STATE = 2;

    // Level Progression
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
        // Intro Scene
        gameState = CUTSCENE_STATE;
        cutscene.startScene("c1level1.txt");
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void startGameThread(JFrame parentFrame) {
        if (parentFrame != null) {
            parentFrame.addKeyListener(this);
        }
        startGameThread();
    }

    public void stopGameThread() {
        if (gameThread != null) {
            Thread t = gameThread;
            gameThread = null; // run() loop will exit
            try {
                t.interrupt();
            } catch (Exception ignored) {
            }
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
                if (remainingTime < 0)
                    remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        // Cutscene mode
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

        // Draw cutscene
        if (gameState == CUTSCENE_STATE) {
            cutscene.draw(g, screenWidth, screenHeight, this);
            return;
        }

        // Draw game
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ESCAPE)
            System.exit(0);

        // Cutscence control
        if (gameState == CUTSCENE_STATE) {
            if (key == KeyEvent.VK_SPACE) {
                cutscene.advance();
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}