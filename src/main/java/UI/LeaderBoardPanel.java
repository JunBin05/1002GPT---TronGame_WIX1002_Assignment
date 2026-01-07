package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class LeaderBoardPanel extends BaseImagePanel {

    private MainFrame mainFrame; 
    private String currentUsername;
    private JPanel headerPanel;
    private JScrollPane scrollPane;
    private JTable table; 
    private BackButton backBtn;
    private DatabaseManager dbManager; 

    public LeaderBoardPanel(MainFrame mainFrame, String currentUsername) {
        this.mainFrame = mainFrame;
        this.currentUsername = currentUsername;
        this.dbManager = new DatabaseManager(); 

        // 1. Load Background Image
        setBackgroundImage("images/leaderboard_bg.png");

        setLayout(new BorderLayout());
        
        // --- HEADER (Back Button) ---
        headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 0, 0));

        // --- FIX 2: Initialize Button and Resize Icon ---
        backBtn = new BackButton("images/back_button.png"); 
        backBtn.setPreferredSize(new Dimension(160, 160)); 
        backBtn.resizeIcon(160); 
        
        backBtn.addActionListener(e -> mainFrame.changeToHome(currentUsername));
        headerPanel.add(backBtn);

        add(headerPanel, BorderLayout.NORTH);

        // --- TABLE SETUP ---
        String[] columnNames = {"RANK", "PLAYER", "HIGHEST_CHAPTER", "TOTAL_SCORE", "DATE_COMPLETED"};

        List<String[]> topPlayers = dbManager.getTop10Scores();
        String[][] data = new String[10][5];

        for (int i = 0; i < 10; i++) {
            String rank = "#" + (i + 1);
            String name = "-";
            String level = "-";
            String score = "-";
            String date = "-";

            if (i < topPlayers.size()) {
                String[] p = topPlayers.get(i);
                name = p[0];
                level = p[1];
                score = p[2];
                date = p[3];
            }
            
            data[i][0] = rank;
            data[i][1] = name;
            data[i][2] = level;
            data[i][3] = score;
            data[i][4] = date;
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(model);

        // --- DISABLE SELECTION ---
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(false);

        // --- TABLE BODY STYLING ---
        table.setOpaque(false);
        table.setBackground(new Color(0, 0, 0, 0));
        ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setOpaque(false);
        
        // --- FIX 3: Increase Font Size & Row Height ---
        table.setFont(new Font("Arial", Font.BOLD, 25)); // Bigger Font (Was 15)
        table.setRowHeight(50);                          // Bigger Rows (Was 40)
        table.setShowGrid(false); 
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);

        // --- TABLE BODY RENDERER ---
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, false, false, row, column);
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(false); 
                
                String playerName = (String) table.getValueAt(row, 1);
                
                if (playerName.equals(currentUsername)) {
                    setForeground(new Color(0, 255, 100)); // Green for YOU
                } else if (row == 0) {
                    setForeground(new Color(255, 215, 0)); // Gold
                } else if (row == 1) {
                    setForeground(Color.LIGHT_GRAY);       // Silver
                } else if (row == 2) {
                    setForeground(new Color(205, 127, 50)); // Bronze
                } else {
                    setForeground(Color.GRAY);
                }
                return c;
            }
        });

        // --- HEADER STYLING ---
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 60)); // Increase header height slightly

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(Color.BLACK);             
        headerRenderer.setForeground(new Color(0, 150, 255));  
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setFont(new Font("Arial", Font.BOLD, 50)); 
        
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // --- SCROLL PANE ---
        scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        scrollPane.setBorder(BorderFactory.createEmptyBorder(50, 50, 20, 50));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); 

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(10, 10, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}