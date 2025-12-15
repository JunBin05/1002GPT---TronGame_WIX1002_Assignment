package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter; 
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;   

public class ImagePanel_Character extends JPanel {

    private Image backgroundImage;
    private BackButton backButton; 
    private MainFrame mainFrame;   
    private JLabel chapterTitleLabel; 
    private KevinButton kevinButton;
    private TronButton tronButton;
    private PlayArenaButton playArenaButton;
    private int currentChapter;

    public ImagePanel_Character(MainFrame mainFrame, String username, int chapterNumber) {
        this.mainFrame = mainFrame;
        this.backgroundImage = new ImageIcon("images/character_bg.png").getImage();
        this.currentChapter = chapterNumber;
        setLayout(null); 

        // 1. Back Button
        backButton = new BackButton("images/back_button.png");
        backButton.addActionListener(e -> mainFrame.changeToStoryMode());
        add(backButton);

        // 2. Dynamic Chapter Title
        String titlePath = "images/c" + chapterNumber + "_image.png";
        
        ImageIcon chapterIcon = new ImageIcon(titlePath); 
        
        // Safety Check: If image doesn't exist, default back to Chapter 1
        if (chapterIcon.getIconWidth() == -1) {
            System.out.println("Warning: Image " + titlePath + " not found. Defaulting to c1_image.png");
            chapterIcon = new ImageIcon("images/c1_image.png");
        }
        
        chapterTitleLabel = new JLabel(chapterIcon);
        add(chapterTitleLabel);

        // 3. Create Kevin Button
        kevinButton = new KevinButton("images/kevin_c.png", "images/kevin_selected.png");
        // --- LOGIC: If Kevin clicked, turn OFF Tron ---
        kevinButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tronButton.isSelected()) {
                    tronButton.reset(); // Turn Tron Blue
                }
            }
        });
        add(kevinButton);

        // 4. Create Tron Button
        tronButton = new TronButton("images/tron_c.png", "images/tron_selected.png");
        // --- LOGIC: If Tron clicked, turn OFF Kevin ---
        tronButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (kevinButton.isSelected()) {
                    kevinButton.reset(); // Turn Kevin Blue
                }
            }
        });
        add(tronButton);

        // 5. Create Play Arena Button (With Validation)
        playArenaButton = new PlayArenaButton("images/playarena_button.png");
        playArenaButton.addActionListener(e -> {
            
            // Check if BOTH are selected (Should be impossible now, but good safety)
            if (kevinButton.isSelected() && tronButton.isSelected()) {
                JOptionPane.showMessageDialog(null, "Please choose only one character!");
            }
            // Check if NEITHER is selected
            else if (!kevinButton.isSelected() && !tronButton.isSelected()) {
                JOptionPane.showMessageDialog(null, "Please select a character to start!");
            }
            // START GAME
            else {
                String selectedChar = kevinButton.isSelected() ? "Kevin" : "Tron";
                System.out.println("Starting game with: " + selectedChar);
                // Set the current chapter and reset to stage 1
                arena.ArenaLoader.currentChapter = currentChapter;
                arena.ArenaLoader.currentStage = 1;
                // Make the window fullscreen (maximized)
                mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                // Show the Tron-style start simulation menu
                UI.StartGameMenu.showMenu(mainFrame);
                // Start the game (show arena)
                arena.ArenaLoader.mainFrame = mainFrame;
                arena.ArenaLoader.startLevel();
            }
        });
        add(playArenaButton);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // A. Back Button
        int btnSize = (int) (h * 0.18); 
        backButton.setBounds(30, 30, btnSize, btnSize);
        backButton.resizeIcon(btnSize);

        // B. Chapter Image
        int imgW = (int) (w * 0.40); 
        int imgH = (int) (imgW * 0.60); 
        int imgX = (w / 2) - (imgW / 2); 
        int imgY = -15; 
        chapterTitleLabel.setBounds(imgX, imgY, imgW, imgH);
        
        String titlePath = "images/c" + currentChapter + "_image.png";
        ImageIcon originalIcon = new ImageIcon(titlePath);
        if (originalIcon.getIconWidth() == -1) {
            originalIcon = new ImageIcon("images/c1_image.png");
        }
        if (originalIcon.getImage() != null) {
            Image scaledImg = originalIcon.getImage().getScaledInstance(imgW, imgH, Image.SCALE_SMOOTH);
            chapterTitleLabel.setIcon(new ImageIcon(scaledImg));
        }

        // C. Character Buttons
        int charWidth = (int) (w * 0.40);  
        int charHeight = (int) (charWidth * 0.70); 
        int spacing = (int) (w * 0.10); 
        int charY = (h / 2) - (charHeight / 2) + 40;
        int kevinX = (w / 2) - charWidth - (spacing / 2);
        int tronX = (w / 2) + (spacing / 2);

        kevinButton.setBounds(kevinX, charY, charWidth, charHeight);
        kevinButton.resizeIcon(charWidth, charHeight);

        tronButton.setBounds(tronX, charY, charWidth, charHeight);
        tronButton.resizeIcon(charWidth, charHeight);

        // D. Play Arena Button
        int playArenaSize = (int) (h * 0.18); 
        int playArenaX = (w / 2) - (playArenaSize / 2);
        int playArenaY = h - playArenaSize - 30;    
        playArenaButton.setBounds(playArenaX, playArenaY, playArenaSize, playArenaSize);
        playArenaButton.resizeIcon(playArenaSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}