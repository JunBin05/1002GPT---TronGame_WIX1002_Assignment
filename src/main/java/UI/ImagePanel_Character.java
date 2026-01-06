package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter; 
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;   

public class ImagePanel_Character extends BaseImagePanel {
    private MainFrame mainFrame;   
    private JLabel chapterTitleLabel; 
    private KevinButton kevinButton;
    private TronButton tronButton;
    private PlayArenaButton playArenaButton;
    private int currentChapter;
    private String username;

    public ImagePanel_Character(MainFrame mainFrame, String username, int chapterNumber) {
        this.mainFrame = mainFrame;
        this.username = username;
        setBackgroundImage("images/character_bg.png");
        this.currentChapter = chapterNumber;
        setLayout(null); 

        // 1. Back Button
        setupBackButton(() -> mainFrame.changeToStoryMode());

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
                // Inform ArenaLoader which character the player selected (Tron or Kevin)
                arena.ArenaLoader.setSelectedPlayer(selectedChar);
                // Set the current chapter
                arena.ArenaLoader.currentChapter = currentChapter;

                // --- RESUME PROMPT: If the user has a saved stage for this chapter, offer to resume ---
                UI.DatabaseManager db = new UI.DatabaseManager();
                int savedStage = db.getChapterStage(username, currentChapter);
                if (savedStage > 1) {
                    int opt = JOptionPane.showConfirmDialog(null, "You last played up to Stage " + savedStage + " in this chapter. Resume from that stage?", "Resume Progress", JOptionPane.YES_NO_OPTION);
                    if (opt == JOptionPane.YES_OPTION) {
                        arena.ArenaLoader.currentStage = savedStage;
                    } else {
                        arena.ArenaLoader.currentStage = 1;
                        // Persist reset to stage 1 for this chapter
                        new Thread(() -> db.setChapterStage(username, currentChapter, 1)).start();
                    }
                } else {
                    arena.ArenaLoader.currentStage = 1;
                    // Persist start at stage 1 for this chapter
                    new Thread(() -> db.setChapterStage(username, currentChapter, 1)).start();
                }

                // Make the window fullscreen (maximized)
                mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                // Prepare the arena GamePanel (so cutscene can play inside it)
                arena.ArenaLoader.mainFrame = mainFrame;

                UI.GamePanel gamePanel = new UI.GamePanel();
                JPanel container = new JPanel(new BorderLayout());
                container.add(gamePanel, BorderLayout.CENTER);
                mainFrame.setContentPane(container);
                mainFrame.revalidate();
                mainFrame.repaint();
                gamePanel.setFocusable(true);
                SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());

                // Start the game loop so GIFs / animations will animate during the cutscene
                gamePanel.startGameThread(mainFrame);

                // Show the pre-stage cutscene for the selected chapter/stage (no NEXT_FILE chaining)
                arena.CutsceneUtil.showCutsceneIfExists(arena.ArenaLoader.currentChapter, arena.ArenaLoader.currentStage, "a", mainFrame, gamePanel, false);
                // Record that we've shown the pre-stage cutscene so ArenaLoader won't show it again.
                arena.ArenaLoader.markPreCutsceneShown(arena.ArenaLoader.currentChapter, arena.ArenaLoader.currentStage);

                // Wait for cutscene to finish on a background thread (do not block the EDT)
                Thread waitThread = new Thread(() -> {
                    try {
                        while (gamePanel.cutscene.isActive()) {
                            Thread.sleep(50);
                        }
                        // When cutscene finishes, show the Start Simulation menu on the EDT and then start the level
                        SwingUtilities.invokeLater(() -> {
                            UI.StartGameMenu.showMenu(mainFrame);
                            arena.ArenaLoader.startLevel();
                        });
                    } catch (InterruptedException ignored) {}
                });
                waitThread.setDaemon(true);
                waitThread.start();
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
        positionBackButton(h);

        // B. Chapter Image
        int imgW = (int) (w * 0.28); // slightly smaller so it doesn't overlap character cards
        int imgH = (int) (imgW * 0.60); 
        int imgX = (w / 2) - (imgW / 2); 
        int imgY = 10; // drop a bit lower for nicer spacing
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
}