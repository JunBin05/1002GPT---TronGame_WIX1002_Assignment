package UI;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets; // <--- FIXES GIBBERISH
import java.util.ArrayList;
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

    // IMAGES
    private Image pKevin, pKevinReal, pTron, pClu, pQuorra, pQuorraEvil, pSark, pSam;

    public CutsceneManager() {
        // LOAD IMAGES
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

            // --- UTF-8 FIX: Handles special characters correctly ---
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
                } else {
                    lines.add(new SceneLine(line.trim(), null, null));
                }
            }
            br.close();
            loadCurrentImage();

        } catch (Exception e) {
            e.printStackTrace();
            isActive = false;
        }
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
            // CHECK FOR NEXT_FILE COMMAND
            SceneLine lastLine = lines.get(lines.size() - 1);
            if (lastLine.imagePath.equalsIgnoreCase("NEXT_FILE")) {
                startScene(lastLine.name); 
            } else {
                isActive = false; 
            }
        } else {
            loadCurrentImage();
        }
    }

    public void draw(Graphics g, int screenWidth, int screenHeight) {
        if (!isActive || currentImage == null) return;

        // 1. BACKGROUND
        g.drawImage(currentImage, 0, 0, screenWidth, screenHeight, null);

        SceneLine current = lines.get(currentIndex);

        // 2. PORTRAITS
        if (current.name != null) {
            Image spriteToDraw = null;
            boolean isVillain = false; 
            String n = current.name.toUpperCase();
            
            // LOGIC
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
                g.drawImage(spriteToDraw, x, y, spriteW, spriteH, null);
            }
        }

        // 3. TEXT BOX
        if (current.name != null && current.text != null) {
            int boxHeight = 150; // Taller for wrapped text
            int boxY = screenHeight - boxHeight;

            g.setColor(new Color(0, 0, 0, 220));
            g.fillRect(0, boxY, screenWidth, boxHeight);
            
            // Name
            g.setColor(new Color(255, 215, 0));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(current.name, 40, boxY + 35);
            
            // Dialogue with WORD WRAP
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.PLAIN, 18));
            
            drawWrappedText(g, current.text, 40, boxY + 70, screenWidth - 80);
            
            // Next Button
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 10));
            g.drawString("SPACE", screenWidth - 50, screenHeight - 15);
        }
    }

    // --- NEW HELPER METHOD FOR WORD WRAPPING ---
    private void drawWrappedText(Graphics g, String text, int x, int y, int lineWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        String currentLine = "";
        int lineHeight = fm.getHeight();

        for (String word : words) {
            // Check width if we add the next word
            int width = fm.stringWidth(currentLine + word + " ");
            
            if (width < lineWidth) {
                currentLine += word + " ";
            } else {
                // Draw current line and start a new one
                g.drawString(currentLine, x, y);
                y += lineHeight; 
                currentLine = word + " ";
            }
        }
        // Draw the last remaining line
        g.drawString(currentLine, x, y);
    }
}