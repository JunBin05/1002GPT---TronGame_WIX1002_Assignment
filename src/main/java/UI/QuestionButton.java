package UI;

import javax.swing.*;
import java.awt.*;

public class QuestionButton extends JButton {

    private Image originalImage;

    public QuestionButton(String imagePath) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make the button transparent (Ghost style)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Add a simple click action
        addActionListener(e -> {
            System.out.println("QNA Button Clicked!");
        });
    }

    /**
     * Resizes the icon image to fit a specific size.
     * Call this from the main panel when the window resizes.
     */
    public void resizeIcon(int size) {
        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}