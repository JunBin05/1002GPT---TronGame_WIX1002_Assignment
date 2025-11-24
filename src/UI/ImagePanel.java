package UI;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Rectangle; // To store the button's area
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * A custom JPanel that paints a scalable background AND UI components.
 * It now handles clicks on the 'Game Id' icon area.
 */
public class ImagePanel extends JPanel {

    private Image backgroundImage;
    private Image gameIcon;
    private Font baseFont;

    // --- NEW ---
    // This will store the clickable area of our "button"
    private Rectangle gameIconBounds;

    public ImagePanel(String imagePath) {
        // 1. Load images and font
        this.backgroundImage = new ImageIcon(imagePath).getImage();
        this.gameIcon = new ImageIcon("images/game_icon.jpeg").getImage();
        this.baseFont = new Font("Arial", Font.BOLD, 1);
        
        // 2. Initialize the bounds to an empty rectangle
        this.gameIconBounds = new Rectangle();

        // --- 3. ADD CLICK LISTENER ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check if the click (e.getPoint()) was inside the button's area
                if (gameIconBounds.contains(e.getPoint())) {
                    // --- ACTION ---
                    // Open the sign-up frame
                    SignUpFrame signUp = new SignUpFrame();
                    signUp.setVisible(true);
                }
            }
        });

        // --- 4. ADD HOVER LISTENER ---
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Check if the mouse is over the button area
                if (gameIconBounds.contains(e.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // 1. Draw Background
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);
        }

        // 2. Calculate All Proportional Sizes
        int leftMargin = (int) (panelWidth * 0.07); 

        // Title Text
        float titleFontSize = (float) (panelHeight * 0.04); 
        int titleTopMargin = (int) (panelHeight * 0.5); 
        
        // Icon
        int iconSize = (int) (panelHeight * 0.17); 
        int iconTopMargin = (int) (titleTopMargin + titleFontSize + (panelHeight * 0.02)); 
        int iconLeftMargin = (int) (leftMargin * 0.9);
        
        // Icon Text
        float textFontSize = (float) (panelHeight * 0.02); 
        int textTopMargin = (int) (iconTopMargin + iconSize + (panelHeight * 0.02));

        // 3. Draw Components
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw "Login Methods:"
        g2.setColor(Color.WHITE);
        g2.setFont(baseFont.deriveFont(titleFontSize));
        g2.drawString("Login Methods:", leftMargin, titleTopMargin);

        // Draw the Game Icon
        if (gameIcon != null) {
            g2.drawImage(gameIcon, iconLeftMargin, iconTopMargin, iconSize, iconSize, this);
            
            // --- 4. UPDATE THE CLICKABLE AREA ---
            // Store the *exact* pixel coordinates where we just drew the icon
            gameIconBounds.setBounds(iconLeftMargin, iconTopMargin, iconSize, iconSize);
        }

        // Draw "Game Id Account"
        g2.setFont(baseFont.deriveFont(Font.PLAIN, textFontSize));
        g2.drawString("Game Id Account", leftMargin, textTopMargin);
    }
}