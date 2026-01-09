package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartGameMenu {

    public static void showMenu(JFrame parentFrame) {
        // Define Tron-inspired colors
        Color NEON_PURPLE_BG = new Color(15, 0, 40); // Dark, deep purple
        Color NEON_CYAN_GLOW = new Color(0, 255, 255); // Bright cyan
        Color NEON_BLUE_BTN = new Color(0, 120, 255); // Tron Blue

        JDialog menuDialog = new JDialog(parentFrame, "Tron Arena: Main Menu", true);
        menuDialog.setSize(450, 350); // Slightly larger
        menuDialog.setLayout(new BorderLayout(20, 20));
        menuDialog.setLocationRelativeTo(parentFrame);
        menuDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // --- Title Panel ---
        JLabel title = new JLabel("TRON ARENA", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 40)); // Larger font
        title.setForeground(NEON_CYAN_GLOW);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        // Instructions Label
        JLabel instructions = new JLabel("Prepare to enter the Grid...", SwingConstants.CENTER);
        instructions.setFont(new Font("Monospaced", Font.PLAIN, 14));
        instructions.setForeground(Color.LIGHT_GRAY);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton playButton = new JButton("START SIMULATION");
        playButton.setFont(new Font("Monospaced", Font.BOLD, 18));
        playButton.setBackground(NEON_BLUE_BTN);
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setPreferredSize(new Dimension(300, 50)); // Larger button

        // Button Action Listener
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDialog.dispose();
            }
        });

        buttonPanel.add(playButton);

        // Final Assembly
        menuDialog.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setBackground(NEON_PURPLE_BG);
        centerPanel.add(instructions);
        centerPanel.add(buttonPanel);

        menuDialog.add(centerPanel, BorderLayout.CENTER);

        // Set the main background color
        menuDialog.getContentPane().setBackground(NEON_PURPLE_BG);
        buttonPanel.setBackground(NEON_PURPLE_BG);

        menuDialog.setVisible(true);
    }
}