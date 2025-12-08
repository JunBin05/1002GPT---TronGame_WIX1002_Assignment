package UI;

import javax.swing.*;
import java.awt.*;

public class Character_ImagePanel extends JPanel {

    private Image backgroundImage;

    public Character_ImagePanel(MainFrame mainFrame, String username) {

        this.backgroundImage = new ImageIcon("images/character_bg.jpg").getImage();
        
        // No layout needed since we are just painting an image
        setLayout(null); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw the image to fill the entire panel
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback color if image is missing
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}