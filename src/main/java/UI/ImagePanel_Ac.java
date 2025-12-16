package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class ImagePanel_Ac extends JPanel {

    private Image backgroundImage;
    private MainFrame mainFrame; 
    private BackButton backButton;
    
    // List to hold our 6 achievement icons
    private List<JButton> achievementIcons; 

    public ImagePanel_Ac(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 1. Load Background
        // Ensure this image exists!
        this.backgroundImage = new ImageIcon("images/ac_bg.png").getImage();
        setLayout(null); 

        // 2. Create Back Button
        backButton = new BackButton("images/back_button.png");
        backButton.addActionListener(e -> {
            mainFrame.changeToHome(mainFrame.getCurrentUsername()); 
        });
        add(backButton);

        // 3. Create 6 Achievement Icons
        achievementIcons = new ArrayList<>();
        
        // Loop to create 6 buttons (ach_1.png to ach_6.png)
        for (int i = 1; i <= 6; i++) {
            String imgPath = "images/ach_" + i + ".png"; // filenames: ach_1.png, ach_2.png...
            JButton btn = createIcon(imgPath);
            
            // Optional: Add click action for each achievement
            int finalI = i; 
            btn.addActionListener(e -> System.out.println("Clicked Achievement " + finalI));
            
            achievementIcons.add(btn);
            add(btn);
        }

        // 4. Add Resize Listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    // --- Helper to create a transparent icon button ---
    private JButton createIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        JButton btn = new JButton(icon);
        
        // Make it transparent/clean
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    // --- Layout Logic ---
    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // A. Back Button (Top Left)
        int btnSize = (int) (h * 0.18); 
        backButton.setBounds(30, 30, btnSize, btnSize);
        backButton.resizeIcon(btnSize);

        // B. Achievement Icons Grid (2 Rows, 3 Columns)
        int iconSize = (int) (h * 0.28); 
        int gap = (int) (w * 0.08);      

        // Calculate total width of one row (3 icons + 2 gaps)
        int totalRowWidth = (iconSize * 3) + (gap * 2);
        
        // Starting X position to center the row
        int startX = (w - totalRowWidth) / 2;
        
        // Starting Y positions for Row 1 and Row 2
        // Row 1 starts at 35% down, Row 2 starts at 60% down
        int row1Y = (int) (h * 0.30); 
        int row2Y = (int) (h * 0.62);

        for (int i = 0; i < achievementIcons.size(); i++) {
            JButton btn = achievementIcons.get(i);
            
            // Determine Row and Column
            int row = i / 3; // 0 for first 3, 1 for next 3
            int col = i % 3; // 0, 1, 2
            
            int x = startX + (col * (iconSize + gap));
            int y = (row == 0) ? row1Y : row2Y;

            btn.setBounds(x, y, iconSize, iconSize);
            
            // Resize image smoothly
            ImageIcon original = new ImageIcon("images/ac_" + (i+1) + "i.png");
            if (original.getImage() != null) {
                Image scaled = original.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaled));
            }
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