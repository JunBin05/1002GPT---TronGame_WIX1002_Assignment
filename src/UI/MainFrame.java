package UI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Tron Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1024, 768);
        setLocationRelativeTo(null); // Center on screen

        ImagePanel backgroundPanel = new ImagePanel("images/tron_2.png");
        
        // --- NEW CODE START ---
        // Pass 'this' (the MainFrame itself) to the panel
        backgroundPanel.setParentFrame(this); 
        // --- NEW CODE END ---

        add(backgroundPanel, BorderLayout.CENTER); 
    }

    /**
     * --- NEW METHOD ---
     * This replaces the Login content with the Home content
     * instantly, without closing the window.
     */
    public void changeToHome() {
        // Create the Home Panel (Just the image, no buttons)
        ImagePanel_Home homePanel = new ImagePanel_Home("images/tron_2.png");

        // Replace the current content (ImagePanel) with the new one (ImagePanel_Home)
        setContentPane(homePanel);

        // Refresh the screen
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame gui = new MainFrame();
            gui.setVisible(true);
        });
    }
}