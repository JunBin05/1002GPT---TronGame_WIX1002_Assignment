package UI;

import javax.swing.*;
import java.awt.*;

public class HomeButton extends JButton {

    private Image originalImage;

    public HomeButton(String imagePath, MainFrame mainFrame, String username) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make it transparent (Ghost mode)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Simple click listener (No function yet, just for testing)
        addActionListener(e -> System.out.println("Home Button Clicked!"));
    }

    /**
     * Helper to resize the icon when window changes size
     */
    public void resizeIcon(int size) {
        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}