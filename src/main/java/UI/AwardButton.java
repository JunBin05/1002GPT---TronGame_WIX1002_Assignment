package UI;

import javax.swing.*;
import java.awt.*;

public class AwardButton extends JButton {

    private Image originalImage;

    public AwardButton(String imagePath) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make the button transparent (Ghost style)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Add a simple click action
        addActionListener(e -> {
            System.out.println("Award Button Clicked!");
        });
    }

    public void resizeIcon(int size) {
        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}