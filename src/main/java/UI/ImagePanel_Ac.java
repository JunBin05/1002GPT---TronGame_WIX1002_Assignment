package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class ImagePanel_Ac extends BaseImagePanel {
    private MainFrame mainFrame; 
    private List<JButton> achievementIcons; 
    private DatabaseManager dbManager; 
    private List<Boolean> unlockedStatus; 

    public ImagePanel_Ac(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dbManager = new DatabaseManager(); 

        // 1. Load Background
        setBackgroundImage("images/ac_bg.png");
        setLayout(null); 

        // 2. Create Back Button
        setupBackButton(() -> mainFrame.changeToHome(mainFrame.getCurrentUsername()));

        // 3. FETCH DATA: Get the list of True/False from Database
        String currentUser = mainFrame.getCurrentUsername();
        this.unlockedStatus = dbManager.getAchievements(currentUser);

        String[] descriptions = {
            "First Blood: Defeat your very first enemy.",                   // For Icon 1
            "Flawless Victory: Complete a level without losing any life.",  // For Icon 2
            "Learning the Hard Way: Experience your first dead.",           // For Icon 3
            "Boss Slayer: Defeat a boss for the first time.",               // For Icon 4
            "Into the void: Fall Outside the map.",                         // For Icon 5
            "Game Conqueror: Complete the first game."                      // For Icon 6
        };      

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

            JButton btn = createIcon(imgPath, isUnlocked, descriptions[i]);
            
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

    // Added 'String tooltip' parameter
    private JButton createIcon(String path, boolean unlocked, String tooltip) {
        ImageIcon icon = new ImageIcon(path);
        JButton btn = new JButton(icon);
        
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        
        // --- THIS LINE APPLIES TO EVERYONE (LOCKED & UNLOCKED) ---
        btn.setToolTipText(tooltip); 
        // ---------------------------------------------------------

        if (unlocked) {
            // Unlocked: Hand Cursor
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            // Locked: Default Cursor
            btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        
        // IMPORTANT: We keep the button ENABLED for everyone.
        // If we setEnabled(false), the tooltip would NOT show.
        // Since we handle the "gray" image manually, this is safe.
        btn.setEnabled(true); 
        
        return btn;
    }

    // --- Layout Logic ---
    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // A. Back Button
        positionBackButton(h);

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
            }
        }
    }
}