package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ImagePanel_Home extends JPanel {

    private Image backgroundImage;
    private String username;
    
    // Define the buttons
    private SoundButton soundButton;
    private PlayButton playButton;  // <--- NEW
    private LeaderBoardButton leaderboardButton; // <--- NEW
    private AwardButton awardButton; // <--- NEW
    private QuestionButton questionButton; // <--- NEW
    private MainFrame mainFrameRef;
    private ProfilePictureButton profileButton; // <--- NEW
    private ProfileName profileNameBox = new ProfileName("Player1"); // <--- NEW
    private ExitButton exitButton; // <--- NEW

    
    public ImagePanel_Home(String imagePath, String username, MainFrame mainFrame) {
        this.mainFrameRef = mainFrame;
        this.username = username;

        DatabaseManager dbManager = new DatabaseManager();
        setLayout(null); // Absolute positioning

        this.backgroundImage = new ImageIcon(imagePath).getImage();

        // 1. Create Sound Button
        soundButton = new SoundButton("images/sound_button.png", "images/game_music.wav");
        add(soundButton);

        // 2. Create Play Button
        playButton = new PlayButton("images/play_button.png");
        add(playButton);
        playButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                // This triggers the jump!
                mainFrameRef.changeToGameMode(); 
            }
        });

        // 3. Create Leaderboard Button
        leaderboardButton = new LeaderBoardButton("images/leaderboard_button.png");
        add(leaderboardButton);
    
        leaderboardButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                // Switch the panel instead of opening a popup
                mainFrameRef.changeToLeaderboard(this.username);
            }
        });

        // 4. Create Award Button
        awardButton = new AwardButton("images/award_button.png");
        add(awardButton);

        awardButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                System.out.println("Switching to Achievement Screen...");
                mainFrameRef.changeToAchievement(); // Call the new method
            }
        });

        // 5. Create Question Button
        questionButton = new QuestionButton("images/qna_button.png");
        add(questionButton);

        questionButton.addActionListener(e -> {
            if (mainFrameRef != null) {
            mainFrameRef.changeToGameRule(); 
            }
        });

        //6. Create Profile Picture Button
        String savedImagePath = dbManager.getProfileImage(username);
        profileButton = new ProfilePictureButton(savedImagePath, username, dbManager);
        add(profileButton);

        //7. Create Profile Name Box
        profileNameBox = new ProfileName(username);
        add(profileNameBox);

        //8. Create Exit Button
        exitButton = new ExitButton("images/exit_button.png");
        add(exitButton);



        // Resize Listener (Adjusts positions when window stretches)
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

        // --- A. Setup Sound Button (Bottom Left) ---
        int soundSize = (int) (h * 0.18); // 15% of screen height
        int soundX = (int) (w * 0.02);    // 12% from left
        int soundY = ((h / 2) - (soundSize / 2)) + 150; // Lower middle

        soundButton.setBounds(soundX, soundY, soundSize, soundSize);
        soundButton.resizeIcon(soundSize);

        // --- B. Setup Play Button (Above Sound Button) ---
        // Width is roughly 2.5 times the height for a rectangular button
        int playHeight = (int) (h * 0.19);  
        int playWidth = (int) (playHeight * 2.5); 
        
       
        int playX = (int) (w * 0.05);
        int playY = ((h / 2) - (playHeight / 2)) - 40;

        playButton.setBounds(playX, playY, playWidth, playHeight);
        playButton.resizeIcon(playWidth, playHeight);

        // --- C. Setup Leaderboard Button (Beside Sound Button) ---
        int lbSize = (int) (h * 0.18); // 15% of screen height
        int lbX = (int) (w * 0.02); // Right of sound button
        int lbY = ((h / 2) - (lbSize / 2)) + 150; // Align with sound button

        leaderboardButton.setBounds(lbX + soundSize + 5, lbY, lbSize, lbSize);
        leaderboardButton.resizeIcon(lbSize); 

        // --- D. Setup Award Button (Beside Leaderboard Button) ---
        int awardSize = (int) (h * 0.18); // 15
        int awardX = (int) (w * 0.02); // Right of leaderboard button
        int awardY = ((h / 2) - (awardSize / 2)) + 150; // Align with sound button

        awardButton.setBounds(awardX + soundSize + lbSize + 10, awardY, awardSize, awardSize);
        awardButton.resizeIcon(awardSize);

        // --- E. Setup Question Button (Right bottom of the window) ---
        int qnaSize = (int) (h * 0.18); // 15% of screen height
        int qnaX = w - qnaSize - (int) (w * 0.15); // 2% from right
        int qnaY = h - qnaSize - 30;

        questionButton.setBounds(qnaX, qnaY, qnaSize, qnaSize);
        questionButton.resizeIcon(qnaSize);

        // --- F. Setup Profile Picture Button (Top Right) ---
        int profileSize = (int) (h * 0.20); // 12% of screen height
        int profileX = w - profileSize - (int) (w * 0.29); // 2% from right
        int profileY = 45; 

        profileButton.setBounds(profileX, profileY, profileSize, profileSize);
        profileButton.resizeIcon(profileSize);

        //--- G. Setup ID Box (Beside Profile Picture) ---
        int boxWidth = 260;
        // CHANGE HEIGHT: Reduced from 85 to 50 because we have less text
        int boxHeight = 50; 
        int boxX = (int) (w * 0.73);
        int boxY = (int) (h * 0.10);
        profileNameBox.setBounds(boxX, boxY, boxWidth, boxHeight);

        // --- H. Setup Exit Button (Top Right Corner) --- (make it beside qna button)
        int exitSize = (int) (h * 0.175); 
        int exitX = w - exitSize - (int) (w * 0.02); 
        int exitY = h - exitSize - 30;
        exitButton.setBounds(exitX, exitY, exitSize, exitSize);
        exitButton.resizeIcon(exitSize);

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
} 