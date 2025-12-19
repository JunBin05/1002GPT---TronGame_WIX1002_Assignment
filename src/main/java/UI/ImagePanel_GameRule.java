package UI;

import javax.swing.*;
import java.awt.*;

public class ImagePanel_GameRule extends JPanel {

    private Image backgroundImage;
    private MainFrame mainFrame;

    public ImagePanel_GameRule(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        
        // 1. Load the Rule Image
        this.backgroundImage = new ImageIcon("images/gamerule1.png").getImage();
        
        setLayout(null); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}