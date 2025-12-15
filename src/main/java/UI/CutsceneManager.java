package UI;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.sound.sampled.*; 
import javax.swing.ImageIcon;

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
        lines.clear();
        currentIndex = 0;
        isActive = true;

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
            
            processCurrentLine(); 

        } catch (Exception e) {
            e.printStackTrace();
            isActive = false;
        }
    }

    private void processCurrentLine() {
        if (currentIndex >= lines.size()) return;

        SceneLine current = lines.get(currentIndex);

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
                startScene(lastLine.name); 
            } else {
                stopMusic(); 
                isActive = false; 
            }
        } else {
            processCurrentLine();
        }
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

    public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
        if (!isActive || currentImage == null) return;

        g.drawImage(currentImage, 0, 0, screenWidth, screenHeight, observer);

        SceneLine current = lines.get(currentIndex);

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