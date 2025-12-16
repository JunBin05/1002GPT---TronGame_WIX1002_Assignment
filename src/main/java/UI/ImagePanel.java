package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ImagePanel extends JPanel {

    private Image backgroundImage;
    private Image gameIcon;
    private Rectangle gameIconBounds;
    private SignUpFrame currentSignUpWindow = null;
    private MainFrame parentFrame; 

    public void setParentFrame(MainFrame frame) {
        this.parentFrame = frame;
    }

    public ImagePanel(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath).getImage();
        this.gameIcon = new ImageIcon("images/game_icon.jpeg").getImage();
        this.gameIconBounds = new Rectangle();

        // --- ANIMATION TIMER ---
        // Refreshes the screen every 50ms to create the "vibe" effect
        new javax.swing.Timer(50, e -> repaint()).start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameIconBounds.contains(e.getPoint())) {
                    if (currentSignUpWindow == null || !currentSignUpWindow.isVisible()) {
                        currentSignUpWindow = new SignUpFrame(parentFrame);
                        currentSignUpWindow.setVisible(true);

                        currentSignUpWindow.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                currentSignUpWindow = null;
                            }
                        });
                    } else {
                        currentSignUpWindow.toFront();
                        currentSignUpWindow.requestFocus();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
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

        // 2. Calculate Button Position
        int iconSize = (int) (panelHeight * 0.20); // Button is 20% of screen height
        int iconX= (int) (panelWidth * 0.10);
        int iconY = (int) (panelHeight * 0.55); 

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 3. Draw ONLY the Icon
        if (gameIcon != null) {
            g2.drawImage(gameIcon, iconX, iconY, iconSize, iconSize, this);
            gameIconBounds.setBounds(iconX, iconY, iconSize, iconSize);

        // 4. NEW: Draw Semi-Transparent Text Below Icon
        String text = "click the icon to start";
        int fontSize = (int) (panelHeight * 0.03); 
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));

        // A. Calculate Pulse (0.0 to 1.0)
        long currentTime = System.currentTimeMillis();
        // Speed: 1000 = 1 second per cycle. Change to 2000 for slower vibe.
        double angle = (currentTime % 1500) / 1500.0 * (2 * Math.PI); 
        double pulse = (Math.sin(angle) + 1) / 2.0; 
        
        // B. Calculate Alpha (Opacity)
        // Range: 128 (Half Visible) to 255 (Fully Visible)
        int minAlpha = 128; 
        int maxAlpha = 255;
        int alpha = (int) (minAlpha + (pulse * (maxAlpha - minAlpha))); 

        // C. Set Color
        g2.setColor(new Color(255, 255, 255, alpha)); 

        // D. Center Text Below Icon
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = iconX + (iconSize / 2) - (textWidth / 2);
        int textY = iconY + iconSize + fontSize + 5; 

        g2.drawString(text, textX, textY);
        }
    }
}