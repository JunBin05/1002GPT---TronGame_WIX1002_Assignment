package UI;

import javax.swing.*;
import java.awt.*;

public class IconButton extends JButton {

    protected Image baseImage;
    private int currentWidth = -1;
    private int currentHeight = -1;

    public IconButton(String imagePath) {
        this.baseImage = new ImageIcon(imagePath).getImage();
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Resize with square dimensions.
    public void resizeIcon(int size) {
        if (size > 0) {
            resizeIcon(size, size);
        }
    }

    // Resize with custom width/height.
    public void resizeIcon(int width, int height) {
        if (width > 0 && height > 0) {
            this.currentWidth = width;
            this.currentHeight = height;
            Image scaled = baseImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }

    // Allow subclasses to change the base image and keep sizing.
    protected void setBaseImage(Image image) {
        if (image != null) {
            this.baseImage = image;
            if (currentWidth > 0 && currentHeight > 0) {
                Image scaled = baseImage.getScaledInstance(currentWidth, currentHeight, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(scaled));
            }
        }
    }
}
