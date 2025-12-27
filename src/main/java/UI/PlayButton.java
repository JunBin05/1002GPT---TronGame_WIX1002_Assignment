package UI;

import javax.swing.*;
import java.awt.*;

public class PlayButton extends JButton {

    private Image originalImage;

    public PlayButton(String imagePath) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make it transparent (Ghost mode)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Simple click listener (No function yet, just for testing)
        addActionListener(e -> System.out.println("Play Button Clicked!"));
    }

    public void resizeIcon(int width, int height) {
        if (width > 0 && height > 0) {
            Image scaled = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}