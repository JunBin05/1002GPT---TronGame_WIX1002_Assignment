package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // 1. GET SCREEN SIZE AUTOMATICALLY
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int screenHeight = (int) screenSize.getHeight();
    
    Thread gameThread;
    int FPS = 60;

    CutsceneManager cutscene = new CutsceneManager();

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);
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
        if (cutscene.isActive) return;
        // player.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (cutscene.isActive) {
            cutscene.draw(g, screenWidth, screenHeight);
            return;
        }

        // DRAW GAME
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("FULLSCREEN MODE ACTIVE", 100, 100);
        g.drawString("Resolution: " + screenWidth + " x " + screenHeight, 100, 150);
        
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Press 1, 2, or 3 to test Cutscenes", 100, 200);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // QUIT BUTTON (Since we are fullscreen, we need a way to close!)
        if (key == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }

        if (cutscene.isActive) {
            if (key == KeyEvent.VK_SPACE) {
                cutscene.advance();
                repaint();
            }
            return;
        }

        // UPDATE THESE NAMES TO MATCH YOUR FILES EXACTLY!
        if (key == KeyEvent.VK_1) {
            cutscene.startScene("c1level1.txt"); 
            repaint();
        }
        if (key == KeyEvent.VK_2) {
            cutscene.startScene("c1level2a.txt");
            repaint();
        }
        if (key == KeyEvent.VK_3) {
            cutscene.startScene("c5level1.txt");
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}