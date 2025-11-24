package UI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class HomeFrame extends JFrame {

    public HomeFrame() {
        // 1. Window Setup
        setTitle("Tron: Home Page");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());

        ImagePanel_Home background = new ImagePanel_Home("images/tron_2.png");
    
        add(background, BorderLayout.CENTER);
    }

    // --- Main method to run ONLY this frame for testing ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomeFrame home = new HomeFrame();
            home.setVisible(true);
        });
    }
}