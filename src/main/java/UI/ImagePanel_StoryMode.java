package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
 
public class ImagePanel_StoryMode extends JPanel {

    private Image backgroundImage;
    private MainFrame mainFrameRef;
    private String username;
    private BackButton backButton;
    private RightButton rightButton;
    private LeftButton leftButton;

    private JLabel chapterImageLabel;
    private JLabel resumeLabel;
    
    // --- NEW: Database and Logic Variables ---
    private DatabaseManager dbManager;
    private int highestChapterUnlocked; 
    // ----------------------------------------
    
    private List<Image> chapterImages; 
    private int currentIndex = 0; 

    public ImagePanel_StoryMode(String imagePath, MainFrame mainFrame, String username) {
        this.mainFrameRef = mainFrame;
        this.username = username;
        this.dbManager = new DatabaseManager(); // Init DB
        setLayout(null); 

        // 1. Fetch User Progress (Default to 1 if new)
        this.highestChapterUnlocked = dbManager.getHighestChapter(username);
        if (this.highestChapterUnlocked == 0) this.highestChapterUnlocked = 1; 

        this.backgroundImage = new ImageIcon(imagePath).getImage();

        // 2. Create Back Button
        backButton = new BackButton("images/back_button.png");
        backButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                mainFrameRef.changeToGameMode(); 
            }
        });
        add(backButton);

        // 3. Load UNLOCKED Images (Index 0-4)
        chapterImages = new ArrayList<>();
        chapterImages.add(new ImageIcon("images/chapter1.png").getImage());
        chapterImages.add(new ImageIcon("images/chapter2.png").getImage());
        chapterImages.add(new ImageIcon("images/chapter3.png").getImage());
        chapterImages.add(new ImageIcon("images/chapter4.png").getImage());
        chapterImages.add(new ImageIcon("images/chapter5.png").getImage());

        // 4. Setup Label
        chapterImageLabel = new JLabel();
        chapterImageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Small label to show resume hint (e.g., "Resume: Stage 3")
        resumeLabel = new JLabel("");
        resumeLabel.setForeground(Color.WHITE);
        resumeLabel.setFont(resumeLabel.getFont().deriveFont(Font.BOLD, 16f));
        resumeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(resumeLabel);

        // --- CLICK EVENT: CHECK LOCK STATUS ---
        chapterImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                
                int selectedChapter = currentIndex + 1; // 1 to 5
                
                // --- LOGIC: Only allow if unlocked ---
                if (selectedChapter <= highestChapterUnlocked) {
                    System.out.println("User selected Chapter " + selectedChapter);
                    if (mainFrameRef != null) {
                        mainFrameRef.changeToCharacterSelect(username, selectedChapter);
                    }
                } else {
                    System.out.println("Chapter " + selectedChapter + " is LOCKED! You must finish Chapter " + (selectedChapter - 1) + " first.");
                    // Optional: Show a small popup message
                    JOptionPane.showMessageDialog(null, "This chapter is locked! Complete the previous chapter to unlock it.");
                }
            }
         });
        add(chapterImageLabel);

        // 5. Create Navigation Buttons
        rightButton = new RightButton("images/right_button.png");
        rightButton.addActionListener(e -> changeChapter(1)); 
        add(rightButton);

        leftButton = new LeftButton("images/left_button.png");
        leftButton.addActionListener(e -> changeChapter(-1)); 
        add(leftButton);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });
    }

    private void changeChapter(int direction) {
        currentIndex += direction;

        if (currentIndex >= chapterImages.size()) {
            currentIndex = 0;
        } else if (currentIndex < 0) {
            currentIndex = chapterImages.size() - 1;
        }

        updateLayout();
    }

    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // 1. Back Button
        int backSize = (int) (h * 0.18);
        backButton.setBounds(30, 30, backSize, backSize);
        backButton.resizeIcon(backSize);

        // 2. Chapter Image Logic
        int imgW = (int) (w * 0.50);
        int imgH = (int) (h * 0.60);
        int imgX = (w / 2) - (imgW / 2);
        int imgY = (h / 2) - (imgH / 2) + 60;
        chapterImageLabel.setBounds(imgX, imgY, imgW, imgH);

        // --- NEW LOGIC: DECIDE WHICH IMAGE TO SHOW ---
        int currentChapterNum = currentIndex + 1;
        Image displayImg;

        if (currentChapterNum <= highestChapterUnlocked) {
            // UNLOCKED: Show the normal image from the list
            displayImg = chapterImages.get(currentIndex);
            chapterImageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor

            // Show resume hint if available
            int savedStage = 0;
            try { savedStage = dbManager.getChapterStage(username, currentChapterNum); } catch (Exception ignored) {}
            if (savedStage > 1) {
                resumeLabel.setText("Resume: Stage " + savedStage);
                resumeLabel.setVisible(true);
            } else {
                resumeLabel.setText("");
                resumeLabel.setVisible(false);
            }

        } else {
            // LOCKED: Load the specific lock image file
            // Expecting files: chapter2_lock.jpg, chapter3_lock.jpg, etc.
            String lockPath = "images/chapter" + currentChapterNum + "_lock.png";
            displayImg = new ImageIcon(lockPath).getImage();
            
            // If the specific lock image doesn't exist, fallback to normal one (or a generic lock)
            if (displayImg.getWidth(null) == -1) { 
                 // Fallback if specific lock image missing
                 displayImg = chapterImages.get(currentIndex); 
            }
            
            chapterImageLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Normal cursor (not clickable)
            resumeLabel.setText("");
            resumeLabel.setVisible(false);
        }

        // Resize and Set
        if (displayImg != null) {
            Image scaled = displayImg.getScaledInstance(imgW, imgH, Image.SCALE_SMOOTH);
            chapterImageLabel.setIcon(new ImageIcon(scaled));
        }
        // ---------------------------------------------

        // 3. Navigation Buttons
        int rightSize = (int) (h * 0.18);
        int rightX = w - rightSize - 80;
        rightButton.setBounds(rightX, 350, rightSize, rightSize);
        rightButton.resizeIcon(rightSize);

        int leftSize = (int) (h * 0.18);
        int leftX = 80;
        leftButton.setBounds(leftX, 350, leftSize, leftSize);
        leftButton.resizeIcon(leftSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}