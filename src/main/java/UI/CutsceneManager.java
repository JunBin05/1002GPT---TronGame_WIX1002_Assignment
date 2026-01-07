package UI;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class CutsceneManager {

    class SceneLine {
        String imagePath;
        String name;
        String text;

        public SceneLine(String image, String name, String text) {
            this.imagePath = image;
            this.name = name;
            this.text = text;
        }
    }

    private ArrayList<SceneLine> lines = new ArrayList<>();
    private int currentIndex = 0;
    public boolean isActive = false;
    private Image currentImage; 
    // Fade-out state for smooth transition when a scene finishes
    private volatile boolean fadingOut = false;
    private volatile float fadeAlpha = 1.0f;
    // When true, a trailing NEXT_FILE line will cause the manager to immediately start the next file.
    // When false, NEXT_FILE is ignored and the scene will end normally.
    private boolean allowChaining = true;
    private Component repaintTarget = null;
    
    // AUDIO handled by AudioManager

    // IMAGES
    private Image pKevin, pKevinReal, pTron, pClu, pQuorra, pQuorraEvil, pSark, pSam;

    // Auto-advance helper: for short (single-line) cutscenes, automatically advance after a short delay
    private volatile Thread autoAdvanceThread = null;

    public CutsceneManager() {
        // LOAD PORTRAITS
        pKevin      = new ImageIcon("cutscene/images/portrait_kevin.png").getImage();
        pKevinReal  = new ImageIcon("cutscene/images/portrait_kevin_real.png").getImage();
        pSam        = new ImageIcon("cutscene/images/portrait_sam.png").getImage();
        pTron       = new ImageIcon("cutscene/images/portrait_tron.png").getImage();
        pClu        = new ImageIcon("cutscene/images/portrait_clu.png").getImage();
        pQuorra     = new ImageIcon("cutscene/images/portrait_quorra.png").getImage();
        pQuorraEvil = new ImageIcon("cutscene/images/portrait_quorra_evil.png").getImage();
        pSark       = new ImageIcon("cutscene/images/portrait_sark.png").getImage();
    }

    public void startScene(String filename) {
        startScene(filename, true);
    }

    public void startScene(String filename, boolean allowChaining) {
        lines.clear();
        currentIndex = 0;
        isActive = true;
        this.allowChaining = allowChaining;

        try {
            File file = new File("cutscene/" + filename);
            if (!file.exists()) {
                System.out.println("ERROR: File not found: " + file.getAbsolutePath());
                isActive = false;
                return;
            }

            // UTF-8 READER
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        lines.add(new SceneLine(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                    }
                }
            }
            br.close();
            br.close();
            System.out.println("[CUTSCENE] Started scene '" + filename + "' with " + lines.size() + " lines.");
            
            processCurrentLine();

            // If this scene only has a single line, auto-advance after a short delay so
            // post-stage cutscenes don't hang the flow if the user doesn't press SPACE.
            if (lines.size() == 1) {
                // cancel any existing auto-advance thread
                if (autoAdvanceThread != null && autoAdvanceThread.isAlive()) {
                    try { autoAdvanceThread.interrupt(); } catch (Exception ignored) {}
                }
                autoAdvanceThread = new Thread(() -> {
                    try {
                        Thread.sleep(1200); // 1.2s default delay
                        // Guard: only advance if still active (user may have already advanced)
                        if (isActive) {
                            System.out.println("[CUTSCENE] Auto-advancing single-line scene: " + filename);
                            advance();
                        }
                    } catch (InterruptedException ignored) {}
                }, "Cutscene-AutoAdvance");
                autoAdvanceThread.setDaemon(true);
                autoAdvanceThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            isActive = false;
        }
    }

    private void processCurrentLine() {
        if (currentIndex >= lines.size()) return;

        // Guard against race where currentIndex may be == lines.size() during fade
        int idx = currentIndex;
        if (idx >= lines.size()) idx = Math.max(0, lines.size() - 1);
        SceneLine current = lines.get(idx);

        // AUDIO COMMANDS
        if (current.imagePath.equalsIgnoreCase("MUSIC")) {
            playMusic(current.name); 
            advance(); 
            return;
        }
        if (current.imagePath.equalsIgnoreCase("STOP_MUSIC")) {
            // Ignored: no explicit stop, music continues
            advance();
            return;
        }

        loadCurrentImage();
    }

    private void loadCurrentImage() {
        if (currentIndex < lines.size()) {
            String path = lines.get(currentIndex).imagePath;
            if (path.equalsIgnoreCase("SAME")) return;
            
            String fullPath = "cutscene/images/" + path;
            if (new File(fullPath).exists()) {
                currentImage = new ImageIcon(fullPath).getImage();
            }
        }
    }

    public void advance() {
        // Cancel any pending auto-advance to avoid double-advancing
        if (autoAdvanceThread != null && autoAdvanceThread.isAlive()) {
            try { autoAdvanceThread.interrupt(); } catch (Exception ignored) {}
            autoAdvanceThread = null;
        }

        currentIndex++;
        if (currentIndex >= lines.size()) {
            SceneLine lastLine = lines.get(lines.size() - 1);
            if (lastLine.imagePath.equalsIgnoreCase("NEXT_FILE")) {
                if (allowChaining) {
                    startScene(lastLine.name);
                } else {
                    System.out.println("[CUTSCENE] Scene finished. Starting fade-out...");
                    startFadeOut();
                }
            } else {
                System.out.println("[CUTSCENE] Scene finished. Starting fade-out...");
                startFadeOut();
            }
        } else {
            processCurrentLine();
        }
    }

    private void startFadeOut() {
        if (fadingOut) return;
        fadingOut = true;
        fadeAlpha = 1.0f;
        Thread fadeThread = new Thread(() -> {
            try {
                int steps = 10;
                int delay = 40; // ~400ms total
                for (int i = 0; i < steps; i++) {
                    fadeAlpha = 1.0f - ((float) (i + 1) / steps);
                    if (repaintTarget != null) SwingUtilities.invokeLater(() -> repaintTarget.repaint());
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ignored) {}
            // finalize
            fadingOut = false;
            isActive = false;
            currentImage = null;
            if (repaintTarget != null) SwingUtilities.invokeLater(() -> repaintTarget.repaint());
            System.out.println("[CUTSCENE] Fade-out complete, scene stopped.");
        });
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    /**
     * Forcefully stop the cutscene immediately (no fade)
     */
    public void forceStop() {
        // Cancel any pending auto-advance thread
        if (autoAdvanceThread != null && autoAdvanceThread.isAlive()) {
            try { autoAdvanceThread.interrupt(); } catch (Exception ignored) {}
            autoAdvanceThread = null;
        }
        fadingOut = false;
        fadeAlpha = 0f;
        isActive = false;
        currentImage = null;
        if (repaintTarget != null) SwingUtilities.invokeLater(() -> repaintTarget.repaint());
    }

    // Backwards-compatible method for checking active state
    public boolean isActive() {
        return isActive;
    }

    /**
     * Utility: check if a cutscene file exists for chapter/stage/suffix. Skips any path containing "random".
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
     * Dialog-based fullscreen playback that mirrors the old CutsceneUtil behavior.
     * Creates an undecorated, modal dialog sized to the parent frame so the sidebar and chrome are hidden.
     */
    public static boolean showCutsceneIfExists(int chapter, int stage, String suffix, JFrame parent, UI.GamePanel unusedPanel, boolean allowChain) {
        String[] candidates = {
            String.format("c%dlevel%d%s.txt", chapter, stage, suffix),
            String.format("c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random")) continue;
            File file = new File("cutscene/" + path);
            if (file.exists() && file.length() > 0) {
                // Build a borderless modal dialog that matches the parent size
                JDialog dlg = new JDialog(parent, "Cutscene", Dialog.ModalityType.APPLICATION_MODAL);
                dlg.setUndecorated(true);
                Rectangle bounds;
                if (parent != null) {
                    bounds = parent.getBounds();
                } else {
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    bounds = new Rectangle(0, 0, screen.width, screen.height);
                }
                dlg.setBounds(bounds);

                // Use a dedicated GamePanel for the cutscene so we don't disturb the live game container
                UI.GamePanel cutscenePanel = new UI.GamePanel();
                cutscenePanel.setPreferredSize(new Dimension(bounds.width, bounds.height));
                dlg.setContentPane(cutscenePanel);
                dlg.pack();
                dlg.setLocation(bounds.x, bounds.y);

                // Ensure key events are received
                cutscenePanel.setFocusable(true);
                SwingUtilities.invokeLater(() -> cutscenePanel.requestFocusInWindow());
                cutscenePanel.startGameThread(parent);

                // Start the requested scene
                cutscenePanel.startCutscene(path, allowChain);

                // Wait in background for cutscene to finish, then close the dialog on the EDT
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
                }, "Cutscene-Waiter");
                waiter.setDaemon(true);
                waiter.start();

                dlg.setVisible(true); // modal; blocks until cutscene finishes
                return true;
            }
        }
        return false;
    }

    /**
     * Play a specific cutscene file (relative to cutscene/). Useful for explicit endings.
     */
    public static boolean playCutsceneFile(String relativePath, JFrame parent, boolean allowChain) {
        File file = new File("cutscene/" + relativePath);
        if (!file.exists() || file.length() <= 0) return false;

        // Build a borderless modal dialog that matches the parent size
        JDialog dlg = new JDialog(parent, "Cutscene", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        Rectangle bounds;
        if (parent != null) {
            bounds = parent.getBounds();
        } else {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            bounds = new Rectangle(0, 0, screen.width, screen.height);
        }
        dlg.setBounds(bounds);

        // Use a dedicated GamePanel for the cutscene so we don't disturb the live game container
        UI.GamePanel cutscenePanel = new UI.GamePanel();
        cutscenePanel.setPreferredSize(new Dimension(bounds.width, bounds.height));
        dlg.setContentPane(cutscenePanel);
        dlg.pack();
        dlg.setLocation(bounds.x, bounds.y);

        // Ensure key events are received
        cutscenePanel.setFocusable(true);
        SwingUtilities.invokeLater(() -> cutscenePanel.requestFocusInWindow());
        cutscenePanel.startGameThread(parent);

        // Start the requested scene
        cutscenePanel.startCutscene(relativePath, allowChain);

        // Wait in background for cutscene to finish, then close the dialog on the EDT
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
        }, "Cutscene-Waiter");
        waiter.setDaemon(true);
        waiter.start();

        dlg.setVisible(true); // modal; blocks until cutscene finishes
        return true;
    }

    public void playMusic(String filename) {
        String path = "cutscene/sounds/" + filename;
        File soundFile = new File(path);
        if (!soundFile.exists()) {
            System.out.println("Sound not found: " + soundFile.getAbsolutePath());
            return;
        }
        // Delegate playback to AudioManager so it can handle switching tracks globally
        UI.AudioManager.playMusic(path);
    }

    /**
     * Returns whether the cutscene is currently fading out.
     * Exposed as a method to avoid making the internal field public.
     */
    public boolean isFadingOut() {
        return fadingOut;
    }

    public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
        // If no lines, nothing to show
        if (lines.isEmpty()) return;

        // remember repaint target so fade thread can trigger repaints
        if (observer instanceof Component) repaintTarget = (Component) observer;

        Graphics2D g2 = (Graphics2D) g.create();
        if (fadingOut) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
        }
        g2.drawImage(currentImage, 0, 0, screenWidth, screenHeight, observer);
        g2.dispose();

        // Defensive: currentIndex may be incremented to lines.size() while fade-out is running
        int idx = currentIndex;
        if (idx < 0) idx = 0;
        if (idx >= lines.size()) idx = Math.max(0, lines.size() - 1);
        SceneLine current = lines.get(idx);

        // HIDE COMMANDS
        if (current.imagePath.equalsIgnoreCase("MUSIC") || 
            current.imagePath.equalsIgnoreCase("NEXT_FILE") ||
            current.imagePath.equalsIgnoreCase("STOP_MUSIC")) {
            return;
        }

        // PORTRAITS
        if (current.name != null) {
            Image spriteToDraw = null;
            boolean isVillain = false; 
            String n = current.name.toUpperCase();
            
            if (n.contains("SAM")) spriteToDraw = pSam;
            else if (n.contains("REAL") || n.contains("OLD")) spriteToDraw = pKevinReal; 
            else if (n.contains("KEVIN") || n.contains("FLYNN")) spriteToDraw = pKevin;
            else if (n.contains("TRON")) spriteToDraw = pTron;
            else if (n.contains("EVIL QUORRA") || n.contains("CORRUPTED")) { spriteToDraw = pQuorraEvil; isVillain = true; }
            else if (n.contains("QUORRA")) spriteToDraw = pQuorra;
            else if (n.contains("CLU")) { spriteToDraw = pClu; isVillain = true; }
            else if (n.contains("SARK")) { spriteToDraw = pSark; isVillain = true; }

            if (spriteToDraw != null) {
                int spriteH = 600; 
                int spriteW = 450; 
                int x = isVillain ? screenWidth - spriteW + 50 : -50;
                int y = screenHeight - spriteH + 150; 
                g.drawImage(spriteToDraw, x, y, spriteW, spriteH, observer);
            }
        }

        // TEXT BOX (With Word Wrap)
        if (current.name != null && current.text != null) {
            int boxHeight = 150;
            int boxY = screenHeight - boxHeight;

            g.setColor(new Color(0, 0, 0, 220));
            g.fillRect(0, boxY, screenWidth, boxHeight);
            
            g.setColor(new Color(255, 215, 0));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(current.name, 40, boxY + 35);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.PLAIN, 18));
            drawWrappedText(g, current.text, 40, boxY + 70, screenWidth - 80);
            
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 10));
            g.drawString("SPACE", screenWidth - 50, screenHeight - 15);
        }
    }

    private void drawWrappedText(Graphics g, String text, int x, int y, int lineWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        String currentLine = "";
        int lineHeight = fm.getHeight();

        for (String word : words) {
            int width = fm.stringWidth(currentLine + word + " ");
            if (width < lineWidth) {
                currentLine += word + " ";
            } else {
                g.drawString(currentLine, x, y);
                y += lineHeight; 
                currentLine = word + " ";
            }
        }
        g.drawString(currentLine, x, y);
    }
}