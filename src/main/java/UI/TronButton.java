package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TronButton extends JButton {

    private Image blueImage;  
    private Image greenImage; 
    private int currentWidth, currentHeight; 
    private boolean isSelected = false; 

    public TronButton(String bluePath, String greenPath) {
        this.blueImage = new ImageIcon(bluePath).getImage();
        this.greenImage = new ImageIcon(greenPath).getImage();

        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isSelected = !isSelected;
                updateVisuals();
            }
        });
    }

    public void reset() {
        this.isSelected = false;
        updateVisuals();
    }

    private void updateVisuals() {
        Image targetImg = isSelected ? greenImage : blueImage;
        if (currentWidth > 0 && currentHeight > 0) {
            Image scaled = targetImg.getScaledInstance(currentWidth, currentHeight, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }

    public void resizeIcon(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        updateVisuals();
    }
    
    public boolean isSelected() {
        return isSelected;
    }
}