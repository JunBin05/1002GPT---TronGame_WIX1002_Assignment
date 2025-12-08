package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
 
public class ImagePanel_StoryMode extends JPanel {

    private Image backgroundImage;
    private MainFrame mainFrameRef;
    private String username;
    private BackButton backButton;
    private RightButton rightButton;
    private LeftButton leftButton;

    private JLabel chapterImageLabel;
    
    private java.util.List<Image> chapterImages; // Holds all 3 images
    private int currentIndex = 0;      // Tracks current page 


    // Constructor: Only needs the image path
    public ImagePanel_StoryMode(String imagePath, MainFrame mainFrame, String username) {
        this.mainFrameRef = mainFrame;
        this.username = username;

        setLayout(null); 

        this.backgroundImage = new ImageIcon(imagePath).getImage();

        // 1. Create Back Button
        backButton = new BackButton("images/back_button.png");
        backButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                // Switch back to Game Mode, passing the username back!
                mainFrameRef.changeToGameMode(); 
            }
        });
        add(backButton);
        // 2. Setup Chapter Image
        chapterImages = new ArrayList<>();
        // Make sure these files exist in your images folder!
        chapterImages.add(new ImageIcon("images/chapter1.png").getImage()); // Index 0
        chapterImages.add(new ImageIcon("images/chapter2.png").getImage()); // Index 1
        chapterImages.add(new ImageIcon("images/chapter3.png").getImage()); // Index 2
        chapterImages.add(new ImageIcon("images/chapter4.png").getImage()); // Index 3
        chapterImages.add(new ImageIcon("images/chapter5.png").getImage()); // Index 4

        // Setup Label
        chapterImageLabel = new JLabel();
        // 1. Change cursor to Hand so user knows they can click
        chapterImageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 2. Add Click Event
        chapterImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (currentIndex == 0) { 
                    // Index 0 means "Chapter 1"
                    System.out.println("Entering Character Selection...");
                    
                    // --- SWITCH TO CHARACTER SCREEN ---
                    mainFrameRef.changeToCharacterSelect(username);
                    // ----------------------------------
                    
                } else {
                    // For Chapter 2, 3, etc. show "Coming Soon"
                    int realChapterNumber = currentIndex + 1;
                    JOptionPane.showMessageDialog(ImagePanel_StoryMode.this, 
                        "Chapter " + realChapterNumber + " is locked or coming soon!", 
                        "Locked", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
         });
        add(chapterImageLabel);

        // 3. Create Right Button
        rightButton = new RightButton("images/right_button.png");
        rightButton.addActionListener(e -> changeChapter(1)); // Go Forward
        add(rightButton);

        //4. Create Left Button
        leftButton = new LeftButton("images/left_button.png");
        leftButton.addActionListener(e -> changeChapter(-1)); // Go Backward
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

        // Loop Logic:
        // If at the end (index 3), go back to start (index 0)
        if (currentIndex >= chapterImages.size()) {
            currentIndex = 0;
        } 
        // If at the start (index -1), go to end (index 2)
        else if (currentIndex < 0) {
            currentIndex = chapterImages.size() - 1;
        }

        // Refresh the screen to show the new image
        updateLayout();
    }

    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        //1. Set Back Button
        int backSize = (int) (h * 0.18);
        backButton.setBounds(30, 30, backSize, backSize);
        backButton.resizeIcon(backSize);

        //2. Set Chapter Image Position
        int imgW = (int) (w * 0.50);
        int imgH = (int) (h * 0.60);
        int imgX = (w / 2) - (imgW / 2);
        int imgY = (h / 2) - (imgH / 2) + 60;
        chapterImageLabel.setBounds(imgX, imgY, imgW, imgH);

         if (!chapterImages.isEmpty()) {
            // Get the current image from the list using currentIndex
            Image currentImg = chapterImages.get(currentIndex);
            
            // Resize it to fit
            Image scaled = currentImg.getScaledInstance(imgW, imgH, Image.SCALE_SMOOTH);
            chapterImageLabel.setIcon(new ImageIcon(scaled));
        }
        //3. Set Right Button
        int rightSize = (int) (h * 0.18);
        int rightX = w - rightSize - 80;
        rightButton.setBounds(rightX, 350, rightSize, rightSize);
        rightButton.resizeIcon(rightSize);

        //4. Set Left Button
        int leftSize = (int) (h * 0.18);
        int leftX = 80;
        leftButton.setBounds(leftX, 350, leftSize, leftSize);
        leftButton.resizeIcon(leftSize);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw image to fill the screen
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}