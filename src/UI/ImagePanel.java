package UI;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ImagePanel extends JPanel {

    private Image backgroundImage;
    private Image gameIcon;
    private Font baseFont;
    private Rectangle gameIconBounds;
    private SignUpFrame currentSignUpWindow = null;

    // --- NEW FIELD ---
    private MainFrame parentFrame; 

    // --- NEW SETTER ---
    public void setParentFrame(MainFrame frame) {
        this.parentFrame = frame;
    }

    public ImagePanel(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath).getImage();
        this.gameIcon = new ImageIcon("images/game_icon.jpeg").getImage();
        this.baseFont = new Font("Arial", Font.BOLD, 1);
        this.gameIconBounds = new Rectangle();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameIconBounds.contains(e.getPoint())) {
                    
                    if (currentSignUpWindow == null || !currentSignUpWindow.isVisible()) {
                        
                        // --- CHANGE HERE: Pass parentFrame to the constructor ---
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

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);
        }

        // ... (Rest of your painting code remains exactly the same) ...
        
        // Recalculate bounds for painting
        int leftMargin = (int) (panelWidth * 0.07); 
        float titleFontSize = (float) (panelHeight * 0.04); 
        int titleTopMargin = (int) (panelHeight * 0.5); 
        int iconSize = (int) (panelHeight * 0.17); 
        int iconTopMargin = (int) (titleTopMargin + titleFontSize + (panelHeight * 0.02)); 
        int iconLeftMargin = (int) (leftMargin * 0.9);
        float textFontSize = (float) (panelHeight * 0.02); 
        int textTopMargin = (int) (iconTopMargin + iconSize + (panelHeight * 0.02));

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.setFont(baseFont.deriveFont(titleFontSize));
        g2.drawString("Login Methods:", leftMargin, titleTopMargin);

        if (gameIcon != null) {
            g2.drawImage(gameIcon, iconLeftMargin, iconTopMargin, iconSize, iconSize, this);
            gameIconBounds.setBounds(iconLeftMargin, iconTopMargin, iconSize, iconSize);
        }

        g2.setFont(baseFont.deriveFont(Font.PLAIN, textFontSize));
        g2.drawString("Game Id Account", leftMargin, textTopMargin);
    }
}