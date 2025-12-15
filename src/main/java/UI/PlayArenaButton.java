package UI;

import javax.swing.*;
import java.awt.*;

public class PlayArenaButton extends JButton {

    private Image originalImage;

    public PlayArenaButton(String imagePath) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make the button transparent
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Add a simple click action (Modify this later to start the game)
        addActionListener(e -> {
            System.out.println("Play Arena Button Clicked! (Game Start immediately...)");
        });
    }

    /**
     * Resizes the icon image to fit a specific size.
     */
    public void resizeIcon(int size) {
        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
    
}