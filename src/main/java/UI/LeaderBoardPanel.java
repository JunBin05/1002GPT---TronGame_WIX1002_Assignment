package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

// CHANGE: Extends JPanel instead of JDialog
public class LeaderBoardPanel extends JPanel {

    private MainFrame mainFrame; // Reference to switch back
    private String currentUsername;
    private JPanel headerPanel;
    private JLabel titleLabel;
    private JPanel listPanel;
    private JScrollPane scrollPane;
    private List<JPanel> rows = new ArrayList<>(); 
    private List<JLabel> allLabels = new ArrayList<>(); 
    private LeftButton backBtn;

    public LeaderBoardPanel(MainFrame mainFrame, String currentUsername) {
        this.mainFrame = mainFrame;
        this.currentUsername = currentUsername;

        setLayout(new BorderLayout());
        
        // --- 1. HEADER PANEL ---
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        backBtn = new LeftButton("images/back_button.png"); 
        // CHANGE: Action goes back to Home Panel instead of dispose()
        backBtn.addActionListener(e -> mainFrame.changeToHome(currentUsername));
        headerPanel.add(backBtn, BorderLayout.WEST);

        titleLabel = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        titleLabel.setForeground(Color.CYAN);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Dummy spacer to keep title centered
        headerPanel.add(Box.createRigidArea(new Dimension(50, 50)), BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. LIST PANEL ---
        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        // Header Row
        addRow(listPanel, "RANK", "PLAYER", "LEVEL", new Color(0, 150, 255)); 
        
        // User Row
        addRow(listPanel, "#1", currentUsername, "0", new Color(255, 215, 0));

        // Placeholders
        for (int i = 2; i <= 10; i++) {
            Color c = (i == 2) ? Color.LIGHT_GRAY : (i == 3) ? new Color(205, 127, 50) : Color.GRAY;
            addRow(listPanel, "#" + i, "-", "-", c);
        }

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); 
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Resize Listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    private void addRow(JPanel panel, String r, String n, String s, Color color) {
        JPanel row = new JPanel(new GridLayout(1, 3));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        
        JLabel l1 = createLabel(r, color);
        JLabel l2 = createLabel(n, color);
        JLabel l3 = createLabel(s, color);

        row.add(l1); row.add(l2); row.add(l3);
        panel.add(row);
        rows.add(row);
    }

    private JLabel createLabel(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(color);
        allLabels.add(lbl); 
        return lbl;
    }

    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        int headerHeight = (int) (h * 0.15); 
        int listHeight   = (int) (h * 0.80); 
        
        int titleSize = (int) (h * 0.06);     
        int btnSize   = (int) (h * 0.05);      
        int paddingX  = (int) (w * 0.05);      
        int fontSize  = (int) (h * 0.030);     

        headerPanel.setPreferredSize(new Dimension(w, headerHeight));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, paddingX, 0, paddingX));
        
        titleLabel.setFont(new Font("Arial", Font.BOLD, titleSize));
        backBtn.setPreferredSize(new Dimension(btnSize, btnSize));
        backBtn.resizeIcon(btnSize);

        int totalRows = rows.size(); 
        int singleRowHeight = (listHeight / totalRows) - 2; 

        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, paddingX, 20, paddingX));

        Font dataFont = new Font("Arial", Font.BOLD, fontSize);
        
        for (JPanel row : rows) {
            Dimension d = new Dimension(w, singleRowHeight);
            row.setMaximumSize(d);
            row.setPreferredSize(d);
            row.setMinimumSize(d);
        }

        for (JLabel lbl : allLabels) {
            lbl.setFont(dataFont);
        }
        
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background color directly
        g.setColor(new Color(10, 10, 20)); // Solid dark blue/black
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}