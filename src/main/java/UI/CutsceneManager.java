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

    private volatile boolean fadingOut = false;
    private volatile float fadeAlpha = 1.0f;

    private boolean allowChaining = true;
    private Component repaintTarget = null;

    private Image pKevin, pKevinReal, pTron, pClu, pQuorra, pQuorraEvil, pSark, pSam;

    private volatile Thread autoAdvanceThread = null;

    public CutsceneManager() {

        pKevin = new ImageIcon("cutscene/images/portrait_kevin.png").getImage();
        pKevinReal = new ImageIcon("cutscene/images/portrait_kevin_real.png").getImage();
        pSam = new ImageIcon("cutscene/images/portrait_sam.png").getImage();
        pTron = new ImageIcon("cutscene/images/portrait_tron.png").getImage();
        pClu = new ImageIcon("cutscene/images/portrait_clu.png").getImage();
        pQuorra = new ImageIcon("cutscene/images/portrait_quorra.png").getImage();
        pQuorraEvil = new ImageIcon("cutscene/images/portrait_quorra_evil.png").getImage();
        pSark = new ImageIcon("cutscene/images/portrait_sark.png").getImage();
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

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

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

            if (lines.size() == 1) {

                if (autoAdvanceThread != null && autoAdvanceThread.isAlive()) {
                    try {
                        autoAdvanceThread.interrupt();
                    } catch (Exception ignored) {
                    }
                }
                autoAdvanceThread = new Thread(() -> {
                    try {
                        Thread.sleep(1200);

                        if (isActive) {
                            System.out.println("[CUTSCENE] Auto-advancing single-line scene: " + filename);
                            advance();
                        }
                    } catch (InterruptedException ignored) {
                    }
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
        if (currentIndex >= lines.size())
            return;

        // Guard against race where currentIndex may be == lines.size() during fade
        int idx = currentIndex;
        if (idx >= lines.size())
            idx = Math.max(0, lines.size() - 1);
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
            if (path.equalsIgnoreCase("SAME"))
                return;

            String fullPath = "cutscene/images/" + path;
            if (new File(fullPath).exists()) {
                currentImage = new ImageIcon(fullPath).getImage();
            }
        }
    }

    public void advance() {
        // Cancel any pending auto-advance to avoid double-advancing
        if (autoAdvanceThread != null && autoAdvanceThread.isAlive()) {
            try {
                autoAdvanceThread.interrupt();
            } catch (Exception ignored) {
            }
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
        if (fadingOut)
            return;
        fadingOut = true;
        fadeAlpha = 1.0f;
        Thread fadeThread = new Thread(() -> {
            try {
                int steps = 10;
                int delay = 40; // ~400ms total
                for (int i = 0; i < steps; i++) {
                    fadeAlpha = 1.0f - ((float) (i + 1) / steps);
                    if (repaintTarget != null)
                        SwingUtilities.invokeLater(() -> repaintTarget.repaint());
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ignored) {
            }
            // finalize
            fadingOut = false;
            isActive = false;
            currentImage = null;
            if (repaintTarget != null)
                SwingUtilities.invokeLater(() -> repaintTarget.repaint());
            System.out.println("[CUTSCENE] Fade-out complete, scene stopped.");
        });
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    public void forceStop() {

        if (autoAdvanceThread != null && autoAdvanceThread.isAlive()) {
            try {
                autoAdvanceThread.interrupt();
            } catch (Exception ignored) {
            }
            autoAdvanceThread = null;
        }
        fadingOut = false;
        fadeAlpha = 0f;
        isActive = false;
        currentImage = null;
        if (repaintTarget != null)
            SwingUtilities.invokeLater(() -> repaintTarget.repaint());
    }

    public boolean isActive() {
        return isActive;
    }

    public static boolean cutsceneExists(int chapter, int stage, String suffix) {
        String[] candidates = {
                String.format("cutscene/c%dlevel%d%s.txt", chapter, stage, suffix),
                String.format("cutscene/c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random"))
                continue;
            File file = new File(path);
            if (file.exists() && file.length() > 0)
                return true;
        }
        return false;
    }

    public static boolean showCutsceneIfExists(int chapter, int stage, String suffix, JFrame parent,
            UI.GamePanel unusedPanel, boolean allowChain) {
        String[] candidates = {
                String.format("c%dlevel%d%s.txt", chapter, stage, suffix),
                String.format("c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random"))
                continue;
            File file = new File("cutscene/" + path);
            if (file.exists() && file.length() > 0) {

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

                UI.GamePanel cutscenePanel = new UI.GamePanel();
                cutscenePanel.setPreferredSize(new Dimension(bounds.width, bounds.height));
                dlg.setContentPane(cutscenePanel);
                dlg.pack();
                dlg.setLocation(bounds.x, bounds.y);

                cutscenePanel.setFocusable(true);
                SwingUtilities.invokeLater(() -> cutscenePanel.requestFocusInWindow());
                cutscenePanel.startGameThread(parent);

                cutscenePanel.startCutscene(path, allowChain);

                Thread waiter = new Thread(() -> {
                    try {
                        while (cutscenePanel.cutscene.isActive() || cutscenePanel.cutscene.isFadingOut()) {
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException ignored) {
                    }
                    SwingUtilities.invokeLater(() -> {
                        try {
                            cutscenePanel.cutscene.forceStop();
                        } catch (Exception ignored) {
                        }
                        try {
                            cutscenePanel.stopGameThread();
                        } catch (Exception ignored) {
                        }
                        dlg.dispose();
                    });
                }, "Cutscene-Waiter");
                waiter.setDaemon(true);
                waiter.start();

                dlg.setVisible(true);
                return true;
            }
        }
        return false;
    }

    public static boolean playCutsceneFile(String relativePath, JFrame parent, boolean allowChain) {
        File file = new File("cutscene/" + relativePath);
        if (!file.exists() || file.length() <= 0)
            return false;

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

        UI.GamePanel cutscenePanel = new UI.GamePanel();
        cutscenePanel.setPreferredSize(new Dimension(bounds.width, bounds.height));
        dlg.setContentPane(cutscenePanel);
        dlg.pack();
        dlg.setLocation(bounds.x, bounds.y);

        cutscenePanel.setFocusable(true);
        SwingUtilities.invokeLater(() -> cutscenePanel.requestFocusInWindow());
        cutscenePanel.startGameThread(parent);

        cutscenePanel.startCutscene(relativePath, allowChain);

        Thread waiter = new Thread(() -> {
            try {
                while (cutscenePanel.cutscene.isActive() || cutscenePanel.cutscene.isFadingOut()) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {
            }
            SwingUtilities.invokeLater(() -> {
                try {
                    cutscenePanel.cutscene.forceStop();
                } catch (Exception ignored) {
                }
                try {
                    cutscenePanel.stopGameThread();
                } catch (Exception ignored) {
                }
                dlg.dispose();
            });
        }, "Cutscene-Waiter");
        waiter.setDaemon(true);
        waiter.start();

        dlg.setVisible(true);
        return true;
    }

    public void playMusic(String filename) {
        String path = "cutscene/sounds/" + filename;
        File soundFile = new File(path);
        if (!soundFile.exists()) {
            System.out.println("Sound not found: " + soundFile.getAbsolutePath());
            return;
        }

        UI.AudioManager.playMusic(path);
    }

    public boolean isFadingOut() {
        return fadingOut;
    }

    public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {

        if (lines.isEmpty())
            return;

        if (observer instanceof Component)
            repaintTarget = (Component) observer;

        Graphics2D g2 = (Graphics2D) g.create();
        if (fadingOut) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
        }
        g2.drawImage(currentImage, 0, 0, screenWidth, screenHeight, observer);
        g2.dispose();

        int idx = currentIndex;
        if (idx < 0)
            idx = 0;
        if (idx >= lines.size())
            idx = Math.max(0, lines.size() - 1);
        SceneLine current = lines.get(idx);

        if (current.imagePath.equalsIgnoreCase("MUSIC") ||
                current.imagePath.equalsIgnoreCase("NEXT_FILE") ||
                current.imagePath.equalsIgnoreCase("STOP_MUSIC")) {
            return;
        }

        if (current.name != null) {
            Image spriteToDraw = null;
            boolean isVillain = false;
            String n = current.name.toUpperCase();

            if (n.contains("SAM"))
                spriteToDraw = pSam;
            else if (n.contains("REAL") || n.contains("OLD"))
                spriteToDraw = pKevinReal;
            else if (n.contains("KEVIN") || n.contains("FLYNN"))
                spriteToDraw = pKevin;
            else if (n.contains("TRON"))
                spriteToDraw = pTron;
            else if (n.contains("EVIL QUORRA") || n.contains("CORRUPTED")) {
                spriteToDraw = pQuorraEvil;
                isVillain = true;
            } else if (n.contains("QUORRA"))
                spriteToDraw = pQuorra;
            else if (n.contains("CLU")) {
                spriteToDraw = pClu;
                isVillain = true;
            } else if (n.contains("SARK")) {
                spriteToDraw = pSark;
                isVillain = true;
            }

            if (spriteToDraw != null) {
                int spriteH = 600;
                int spriteW = 450;
                int x = isVillain ? screenWidth - spriteW + 50 : -50;
                int y = screenHeight - spriteH + 150;
                g.drawImage(spriteToDraw, x, y, spriteW, spriteH, observer);
            }
        }

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