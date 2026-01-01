package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AchievementPopup extends JPanel {

    public AchievementPopup(String title, String description) {
        // 1. Neon Box Style
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0)); // Black Background
        
        // Thicker Border
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.CYAN, 4), 
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // 2. Icon (Big Trophy)
        JLabel iconLabel = new JLabel("🏆"); 
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); 
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); 
        add(iconLabel, BorderLayout.WEST);

        // 3. Text Panel
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(">> " + title.toUpperCase() + " <<");
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 28)); 
        
        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font("Monospaced", Font.BOLD, 18)); 
        
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        add(textPanel, BorderLayout.CENTER);
        
        // 4. Fixed Size
        setSize(600, 150);
        
        // 5. Hand Cursor (Let user know it's clickable)
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Static helper to show it
    public static void show(JFrame frame, String title, String description) {
        if (frame == null) return;

        AchievementPopup popup = new AchievementPopup(title, description);
        
        // Center Logic
        int x = (frame.getWidth() - popup.getWidth()) / 2;
        int y = (frame.getHeight() - popup.getHeight()) / 2;
        popup.setLocation(x, y);

        JLayeredPane layeredPane = frame.getLayeredPane();
        layeredPane.add(popup, JLayeredPane.POPUP_LAYER);
        layeredPane.moveToFront(popup);

        // --- TIMER (Auto Close) ---
        Timer timer = new Timer(4000, e -> {
            if (popup.getParent() != null) {
                layeredPane.remove(popup);
                layeredPane.repaint();
            }
        });
        timer.setRepeats(false);
        timer.start();

        // --- NEW: CLICK TO CLOSE ---
        MouseAdapter closeAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timer.stop(); // Stop the auto-timer so it doesn't fire later
                if (popup.getParent() != null) {
                    layeredPane.remove(popup);
                    layeredPane.repaint(); // Refresh screen to remove artifact
                    System.out.println("Achievement popup closed by user.");
                }
            }
        };

        // Add the listener to the POPUP and ALL its children components
        // (This ensures clicking the text or icon also closes it)
        popup.addMouseListener(closeAction);
        addListenersRecursively(popup, closeAction);
    }

    // Helper to add click listener to everything (Icon, Text, Panel)
    private static void addListenersRecursively(Container container, MouseAdapter listener) {
        for (Component c : container.getComponents()) {
            c.addMouseListener(listener);
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Make text show hand cursor too
            if (c instanceof Container) {
                addListenersRecursively((Container) c, listener);
            }
        }
    }
}