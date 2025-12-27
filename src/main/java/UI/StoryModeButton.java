package UI;

import javax.swing.*;
import java.awt.*;

public class StoryModeButton extends JButton {

    private Image originalImage;

    public StoryModeButton(String imagePath) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make the button transparent (Ghost style)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Add a simple click action
        addActionListener(e -> {
            System.out.println("Story Mode Button Clicked!");
        });
    }
    
    public void resizeIcon(int width, int height) {
        if (width > 0 && height > 0) {
            Image scaled = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}