package UI;

import javax.swing.*;
import java.awt.*;

public class LeaderBoardDialog extends JDialog {

    public LeaderBoardDialog(JFrame parent, String currentUsername) {
        super(parent, true); 
        setUndecorated(true); 
        setBackground(new Color(0, 0, 0, 0)); 
        setSize(800, 500); 
        setLocationRelativeTo(parent);

        LeaderboardPanel content = new LeaderboardPanel();
        content.setLayout(new BorderLayout());

        // --- 2. HEADER PANEL (Title + Buttons) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // A. LEFT BUTTON (Top Left - New!)
        // Make sure "left_button.png" is in your images folder
        LeftButton backBtn = new LeftButton("images/left_button.png");
        // Resize it to look nice in the header (e.g., 40x40)
        backBtn.setPreferredSize(new Dimension(40, 40)); 
        backBtn.resizeIcon(40); 
        // Add Action: Close the window
        backBtn.addActionListener(e -> dispose());
        
        headerPanel.add(backBtn, BorderLayout.WEST); // Add to Left Side

        // B. TITLE (Center)
        JLabel title = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36)); 
        title.setForeground(Color.CYAN);
        headerPanel.add(title, BorderLayout.CENTER);

        // C. CLOSE "X" BUTTON (Top Right - Optional if you have LeftButton)
        JButton closeX = new JButton("X");
        closeX.setForeground(Color.RED);
        closeX.setBackground(Color.BLACK);
        closeX.setBorder(null);
        closeX.setFocusPainted(false);
        closeX.setFont(new Font("Arial", Font.BOLD, 20));
        closeX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeX.addActionListener(e -> dispose());
        headerPanel.add(closeX, BorderLayout.EAST);

        content.add(headerPanel, BorderLayout.NORTH);

        // --- 3. LIST PANEL ---
        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        // Header Row
        listPanel.add(createRowPanel("RANK", "PLAYER", "LEVEL", new Color(0, 50, 100))); 
        listPanel.add(Box.createRigidArea(new Dimension(0, 10))); 

        // Data Rows
        listPanel.add(createRowPanel("#1", currentUsername, "0", new Color(255, 215, 0)));
        listPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        for (int i = 2; i <= 10; i++) {
            Color rankColor = (i == 2) ? new Color(192, 192, 192) : 
                              (i == 3) ? new Color(205, 127, 50) : Color.GRAY;
            listPanel.add(createRowPanel("#" + i, "-", "-", rankColor));
            listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 50, 20, 50)); 
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0,0)); // Hide scrollbar
        content.add(scroll, BorderLayout.CENTER);

        add(content);
    }

    private JPanel createRowPanel(String rank, String name, String score, Color color) {
        JPanel row = new JPanel(new GridLayout(1, 3)); 
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(800, 40)); 
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255,255,255,50))); 

        Font font = new Font("Arial", Font.BOLD, 18);

        JLabel rankLbl = new JLabel(rank, SwingConstants.CENTER);
        rankLbl.setFont(font); rankLbl.setForeground(color);

        JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
        nameLbl.setFont(font); nameLbl.setForeground(color);

        JLabel scoreLbl = new JLabel(score, SwingConstants.CENTER);
        scoreLbl.setFont(font); scoreLbl.setForeground(color);

        row.add(rankLbl);
        row.add(nameLbl);
        row.add(scoreLbl);

        return row;
    }

    private class LeaderboardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(10, 10, 20, 230));
            g2.fillRoundRect(0, 0, w, h, 30, 30);

            g2.setColor(Color.CYAN);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(2, 2, w-4, h-4, 30, 30);
        }
    }
}