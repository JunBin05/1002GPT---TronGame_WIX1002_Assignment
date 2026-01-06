package arena;

import UI.GamePanel;
import javax.swing.*;
import java.awt.Dialog;
import java.io.File;

public class CutsceneUtil {
    /**
     * Checks if a cutscene file exists for the given chapter, stage, and suffix ("a" or "b").
     * Skips files with "random" in the name.
     */
    public static boolean cutsceneExists(int chapter, int stage, String suffix) {
        String[] candidates = {
            String.format("cutscene/c%dlevel%d%s.txt", chapter, stage, suffix),
            String.format("cutscene/c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random")) continue;
            File file = new File(path);
            if (file.exists() && file.length() > 0) return true;
        }
        return false;
    }

    /**
     * Shows the cutscene using the GamePanel's cutscene system, if the file exists and is not random.
     */
    public static void showCutsceneIfExists(int chapter, int stage, String suffix, JFrame parent, GamePanel panel) {
        showCutsceneIfExists(chapter, stage, suffix, parent, panel, true);
    }

    public static void showCutsceneIfExists(int chapter, int stage, String suffix, JFrame parent, GamePanel panel, boolean allowChain) {
        String[] candidates = {
            String.format("c%dlevel%d%s.txt", chapter, stage, suffix),
            String.format("c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random")) continue;
            File file = new File("cutscene/" + path);
            if (file.exists() && file.length() > 0) {
                // Play the cutscene in a fullscreen, modal dialog so all cutscenes are consistent
                JDialog dlg = new JDialog(parent, "Cutscene", Dialog.ModalityType.APPLICATION_MODAL);
                dlg.setUndecorated(true);
                // Match parent bounds to cover full window
                if (parent != null) {
                    dlg.setBounds(parent.getBounds());
                } else {
                    dlg.setSize(800, 600);
                    dlg.setLocationRelativeTo(null);
                }

                UI.GamePanel cutscenePanel = new UI.GamePanel();
                cutscenePanel.setPreferredSize(dlg.getSize());
                dlg.getContentPane().add(cutscenePanel);
                dlg.pack();
                dlg.setLocationRelativeTo(parent);

                // Ensure key events are received by attaching listener and starting the thread
                cutscenePanel.setFocusable(true);
                SwingUtilities.invokeLater(() -> cutscenePanel.requestFocusInWindow());

                // Start the requested scene first so gameState is CUTSCENE before the thread ticks
                cutscenePanel.startCutscene(path, allowChain);

                cutscenePanel.startGameThread(parent);
                // Force repaints during cutscene in case the game thread is delayed
                final javax.swing.Timer repaintTimer = new javax.swing.Timer(50, e -> cutscenePanel.repaint());
                repaintTimer.start();

                // Wait in background for cutscene to finish then dispose dialog on EDT
                Thread waiter = new Thread(() -> {
                    try {
                        while (cutscenePanel.cutscene.isActive() || cutscenePanel.cutscene.isFadingOut()) {
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException ignored) {}
                    SwingUtilities.invokeLater(() -> {
                        try { cutscenePanel.cutscene.forceStop(); } catch (Exception ignored) {}
                        try { cutscenePanel.stopGameThread(); } catch (Exception ignored) {}
                        dlg.dispose();
                    });
                });
                waiter.setDaemon(true);
                waiter.start();

                dlg.setVisible(true);
                return;
            }
        }
    }
}
