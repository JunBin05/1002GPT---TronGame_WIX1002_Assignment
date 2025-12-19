package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ImagePanel_GameRule extends JPanel {

    private Image backgroundImage;
    private MainFrame mainFrame;
    private BackButton backButton;
    
    // Navigation Buttons
    private JButton leftButton, rightButton;
    private int currentRuleIndex = 1; 
    private final int TOTAL_RULES = 4; 

    public ImagePanel_GameRule(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null); 

        // 1. Load Initial Image
        updateImage();

        // 2. Create Buttons
        createBackButton();
        createNavButtons(); // Creates the visible LEFT/RIGHT buttons

        // 3. Add Resize Listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    private void updateImage() {
        String imagePath = "images/gamerule" + currentRuleIndex + ".png";
        ImageIcon icon = new ImageIcon(imagePath);
        
        if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
             this.backgroundImage = icon.getImage();
        } else {
             System.out.println("Could not load: " + imagePath);
        }
        repaint(); 
    }

    private void createBackButton() {
        backButton = new BackButton("images/back_button.png");
        backButton.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.changeToHome(mainFrame.getCurrentUsername()); 
            }
        });
        add(backButton);
    }

    private void createNavButtons() {
        // --- LEFT BUTTON ---
        leftButton = new JButton(new ImageIcon("images/left_button.png"));
        leftButton.setContentAreaFilled(false); 
        leftButton.setBorderPainted(false);
        leftButton.setFocusPainted(false);
        leftButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        leftButton.addActionListener(e -> {
            currentRuleIndex--;
            if (currentRuleIndex < 1) currentRuleIndex = TOTAL_RULES;
            updateImage();
        });
        add(leftButton);

        // --- RIGHT BUTTON ---
        rightButton = new JButton(new ImageIcon("images/right_button.png"));
        rightButton.setContentAreaFilled(false); 
        rightButton.setBorderPainted(false);
        rightButton.setFocusPainted(false);
        rightButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        rightButton.addActionListener(e -> {
            currentRuleIndex++;
            if (currentRuleIndex > TOTAL_RULES) currentRuleIndex = 1;
            updateImage();
        });
        add(rightButton);
    }

    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // 1. Back Button
        int backSize = (int) (h * 0.18); 
        backButton.setBounds(30, 30, backSize, backSize);
        backButton.resizeIcon(backSize);
        
        // 2. Nav Buttons (Left & Right)
        int navSize = (int) (h * 0.18); 
        int navY = (h - navSize) / 2;   
        int padding = 20;               

        // Position Left Button
        leftButton.setBounds(padding, navY, navSize, navSize);
        resizeButtonIcon(leftButton, (int)(navSize * 0.7)); // 

        // Position Right Button
        rightButton.setBounds(w - navSize - padding, navY, navSize, navSize);
        resizeButtonIcon(rightButton, (int)(navSize * 0.7));
    }
    
    // Helper to resize the icon inside the standard button
    private void resizeButtonIcon(JButton btn, int size) {
        ImageIcon icon = (ImageIcon) btn.getIcon();
        if (icon != null && icon.getImage() != null) {
             Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
             btn.setIcon(new ImageIcon(img));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}