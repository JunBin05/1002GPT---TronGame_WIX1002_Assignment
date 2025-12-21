package UI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import arena.ArenaLoader;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Tron Game");

        // 1. REMOVE BORDERS (Optional: delete if you want the X button)
        window.setUndecorated(true);

        // 2. MAXIMIZE WINDOW
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // Resize GamePanel to fit window
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // 3. START CUTSCENE FLOW on the GamePanel (runs on EDT)
        gamePanel.setupGame();
        gamePanel.startGameThread(window);

        // Start a background watcher thread so we don't block the EDT.
        Thread watcher = new Thread(() -> {
            try {
                int waited = 0;
                while (!gamePanel.cutscene.isActive() && waited < 3000) {
                    Thread.sleep(50);
                    waited += 50;
                }
                while (gamePanel.cutscene.isActive()) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {
            }

            // After the cutscene finishes, stop the GamePanel thread and switch to ArenaLoader on the EDT.
            SwingUtilities.invokeLater(() -> {
                gamePanel.stopGameThread();
                ArenaLoader.mainFrame = window;
                ArenaLoader.currentChapter = 1;
                ArenaLoader.currentStage = 1;
                ArenaLoader.startLevel();
            });
        });
        watcher.setDaemon(true);
        watcher.start();
    }
}