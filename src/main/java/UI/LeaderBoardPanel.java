package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class LeaderBoardPanel extends JPanel {

    private MainFrame mainFrame; 
    private String currentUsername;
    private JPanel headerPanel;
    private JPanel listPanel;
    private JScrollPane scrollPane;
    private List<JPanel> rows = new ArrayList<>(); 
    private List<JLabel> allLabels = new ArrayList<>(); 
    private LeftButton backBtn;
    
    // --- NEW: Background Image Variable ---
    private Image backgroundImage; 

    public LeaderBoardPanel(MainFrame mainFrame, String currentUsername) {
        this.mainFrame = mainFrame;
        this.currentUsername = currentUsername;

        // --- 1. LOAD THE IMAGE ---
        // Make sure you renamed the file to 'leaderboard_bg.jpg'
        this.backgroundImage = new ImageIcon("images/leaderboard_bg.png").getImage();

        setLayout(new BorderLayout());
        
        // --- 2. HEADER PANEL ---
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        backBtn = new LeftButton("images/back_button.png"); 
        backBtn.addActionListener(e -> mainFrame.changeToHome(currentUsername));
        headerPanel.add(backBtn, BorderLayout.WEST);


        headerPanel.add(Box.createRigidArea(new Dimension(50, 50)), BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 3. LIST PANEL ---
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

        // --- DYNAMIC SIZING ---
        // Increase header height slightly so the list starts BELOW your image's banner
        int headerHeight = (int) (h * 0.22); // Increased to 22% to clear the banner
        int listHeight   = (int) (h * 0.75); 
        
        int btnSize   = (int) (h * 0.18);      
        int paddingX  = (int) (w * 0.05);      
        int fontSize  = (int) (h * 0.030);     

        headerPanel.setPreferredSize(new Dimension(w, headerHeight));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, paddingX, 0, paddingX));
        
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

    // --- DRAW BACKGROUND IMAGE ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Draws your new image to fill the entire panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback color if image fails
            g.setColor(new Color(10, 10, 20)); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}