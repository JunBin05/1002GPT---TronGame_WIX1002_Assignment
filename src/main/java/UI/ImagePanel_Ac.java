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
    private List<JButton> achievementIcons; 
    private DatabaseManager dbManager; 
    
    // We store the status list here so updateLayout can use it later
    private List<Boolean> unlockedStatus; 

    public ImagePanel_Ac(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dbManager = new DatabaseManager(); 

        // 1. Load Background
        this.backgroundImage = new ImageIcon("images/ac_bg.png").getImage();
        setLayout(null); 

        // 2. Create Back Button
        backButton = new BackButton("images/back_button.png");
        backButton.addActionListener(e -> {
            mainFrame.changeToHome(mainFrame.getCurrentUsername()); 
        });
        add(backButton);

        // 3. FETCH DATA: Get the list of True/False from Database
        String currentUser = mainFrame.getCurrentUsername();
        this.unlockedStatus = dbManager.getAchievements(currentUser);

        // 4. Create 6 Achievement Icons
        achievementIcons = new ArrayList<>();
        
        for (int i = 0; i < 6; i++) {
            int achID = i + 1; // 1 to 6
            
            // Check status safely
            boolean isUnlocked = false;
            if (unlockedStatus != null && i < unlockedStatus.size()) {
                isUnlocked = unlockedStatus.get(i);
            }

            // --- LOGIC: CHOOSE IMAGE BASED ON STATUS ---
            // If True  -> ac_1.png
            // If False -> ac_1i.png (Transparent/Gray version)
            String imgName = isUnlocked ? "ac_" + achID + ".png" : "ac_" + achID + "i.png";
            String imgPath = "images/" + imgName;

            JButton btn = createIcon(imgPath, isUnlocked);
            
            // Add click listener (mostly for debugging or feedback)
            boolean finalStatus = isUnlocked;
            btn.addActionListener(e -> {
                if (finalStatus) {
                    System.out.println("Achievement " + achID + " is UNLOCKED!");
                } else {
                    System.out.println("Achievement " + achID + " is LOCKED.");
                    // UNCOMMENT BELOW TO TEST UNLOCKING:
                    // dbManager.unlockAchievement(currentUser, achID);
                    // System.out.println("Cheat applied! Re-enter screen to see change.");
                }
            });
            
            achievementIcons.add(btn);
            add(btn);
        }

        // 5. Add Resize Listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    // --- Helper to create button ---
    private JButton createIcon(String path, boolean unlocked) {
        ImageIcon icon = new ImageIcon(path);
        JButton btn = new JButton(icon);
        
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        
        if (unlocked) {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setEnabled(true); 
        } else {
            btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            // We set the "Disabled Icon" to be the same transparent image
            // so Java doesn't try to add its own gray filter on top of yours.
            btn.setDisabledIcon(icon);
            btn.setEnabled(false); 
        }
        
        return btn;
    }

    // --- Layout Logic ---
    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // A. Back Button
        int btnSize = (int) (h * 0.18); 
        backButton.setBounds(30, 30, btnSize, btnSize);
        backButton.resizeIcon(btnSize);

        // B. Icons Grid
        int iconSize = (int) (h * 0.28); 
        int gap = (int) (w * 0.08);      

        int totalRowWidth = (iconSize * 3) + (gap * 2);
        int startX = (w - totalRowWidth) / 2;
        int row1Y = (int) (h * 0.30); 
        int row2Y = (int) (h * 0.62);

        for (int i = 0; i < achievementIcons.size(); i++) {
            JButton btn = achievementIcons.get(i);
            
            int row = i / 3; 
            int col = i % 3; 
            int x = startX + (col * (iconSize + gap));
            int y = (row == 0) ? row1Y : row2Y;

            btn.setBounds(x, y, iconSize, iconSize);
            
            // --- RESIZE LOGIC ---
            // We need to re-check status here to ensure we resize the CORRECT image
            boolean isUnlocked = false;
            if (unlockedStatus != null && i < unlockedStatus.size()) {
                isUnlocked = unlockedStatus.get(i);
            }
            
            String imgName = isUnlocked ? "ac_" + (i+1) + ".png" : "ac_" + (i+1) + "i.png";
            ImageIcon original = new ImageIcon("images/" + imgName);
            
            if (original.getImage() != null) {
                Image scaled = original.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaled);
                btn.setIcon(scaledIcon);
                btn.setDisabledIcon(scaledIcon); // Ensure locked version resizes too
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