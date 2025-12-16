package UI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    private String currentUsername;

    public MainFrame() {
        super("Tron Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1024, 768);
        setLocationRelativeTo(null); // Center on screen

        ImagePanel backgroundPanel = new ImagePanel("images/tron_2.png");
        
        // --- NEW CODE START ---
        // Pass 'this' (the MainFrame itself) to the panel
        backgroundPanel.setParentFrame(this); 
        // --- NEW CODE END ---

        add(backgroundPanel, BorderLayout.CENTER); 
    }

    /**
     * --- NEW METHOD ---
     * This replaces the Login content with the Home content
     * instantly, without closing the window.
     */
    public void changeToHome(String username) {
        // Create the Home Panel (Just the image, no buttons)
        this.currentUsername = username;

        ImagePanel_Home homePanel = new ImagePanel_Home("images/tron_2.png", username, this);

        // Replace the current content (ImagePanel) with the new one (ImagePanel_Home)
        setContentPane(homePanel);

        // Refresh the screen
        revalidate();
        repaint();
    }

 

    public void changeToGameMode() {
 
        ImagePanel_GameMode gameModePanel = new ImagePanel_GameMode("images/tron_3.png", this, currentUsername);
    
         setContentPane(gameModePanel);
         revalidate();
        repaint();
    }


    public void changeToStoryMode() {
        // 1. Create the Story Mode Panel
        // We pass 'this' (MainFrame) so the back button works
        // We pass 'currentUsername' so we don't lose the user data
        ImagePanel_StoryMode storyPanel = new ImagePanel_StoryMode("images/tron_4.png", this, currentUsername);
        
        // 2. Switch the view
        setContentPane(storyPanel);
        revalidate();
        repaint();
    }


    public void changeToLeaderboard(String username) {
        LeaderBoardPanel leaderboard = new LeaderBoardPanel(this, username);
        setContentPane(leaderboard);
        revalidate();
        repaint();
    }

    public void changeToCharacterSelect(String username, int chapterNumber) {
        // Create the new panel, PASSING the chapterNumber
        ImagePanel_Character charPanel = new ImagePanel_Character(this, username, chapterNumber);
        
        setContentPane(charPanel);
        revalidate();
        repaint();
    }

    public void changeToAchievement() {
        // Create the achievement panel
        ImagePanel_Ac acPanel = new ImagePanel_Ac(this);
        
        // Switch the screen content
        setContentPane(acPanel);
        revalidate();
        repaint();
    }
    

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame gui = new MainFrame();
            gui.setVisible(true);
        });
    }


    public String getCurrentUsername() {
        return this.currentUsername;
    }
}