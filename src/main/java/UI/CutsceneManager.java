package UI;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.sound.sampled.*; 
import javax.swing.ImageIcon;
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
    
    // AUDIO
    private Clip musicClip; 

    // IMAGES
    private Image pKevin, pKevinReal, pTron, pClu, pQuorra, pQuorraEvil, pSark, pSam;

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
            stopMusic();
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
        currentIndex++;
        if (currentIndex >= lines.size()) {
            SceneLine lastLine = lines.get(lines.size() - 1);
            if (lastLine.imagePath.equalsIgnoreCase("NEXT_FILE")) {
                if (allowChaining) {
                    startScene(lastLine.name);
                } else {
                    stopMusic();
                    System.out.println("[CUTSCENE] Scene finished. Starting fade-out...");
                    startFadeOut();
                }
            } else {
                stopMusic();
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

    public void playMusic(String filename) {
        try {
            File soundFile = new File("cutscene/sounds/" + filename);
            if (!soundFile.exists()) {
                System.out.println("Sound not found: " + soundFile.getAbsolutePath());
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
            
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
            }

            musicClip = AudioSystem.getClip();
            musicClip.open(audioInput);

            // VOLUME CONTROL (-10dB)
            try {
                FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f); 
            } catch (Exception e) {}

            musicClip.start();
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
        }
    }

    /**
     * Returns whether the cutscene is currently fading out.
     * Exposed as a method to avoid making the internal field public.
     */
    public boolean isFadingOut() {
        return fadingOut;
    }

    public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
        // If nothing to draw, exit early
        if (lines.isEmpty() || currentImage == null) return;

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