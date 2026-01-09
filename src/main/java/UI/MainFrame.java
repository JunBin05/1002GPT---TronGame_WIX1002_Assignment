package UI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    private String currentUsername;

    public MainFrame() {
        super("Tron Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Start in maximized state so the main menu fills the entire screen like the
        // arena
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        ImagePanel backgroundPanel = new ImagePanel("images/tron_2.png");
        backgroundPanel.setPreferredSize(getSize());

        backgroundPanel.setParentFrame(this);
        add(backgroundPanel, BorderLayout.CENTER);
    }

    public void changeToHome(String username) {
        // Create the Home Panel (Just the image, no buttons)
        this.currentUsername = username;

        ImagePanel_Home homePanel = new ImagePanel_Home("images/tron_2.png", username, this);
        // Ensure background music is running on home
        AudioManager.playMusic("audio/sound_background.wav");

        setView(homePanel);
    }

    public void changeToGameMode() {
        ImagePanel_GameMode gameModePanel = new ImagePanel_GameMode("images/tron_3.png", this, currentUsername);
        setView(gameModePanel);
    }

    public void changeToStoryMode() {
        // 1. Create the Story Mode Panel
        ImagePanel_StoryMode storyPanel = new ImagePanel_StoryMode("images/tron_4.png", this, currentUsername);
        // Ensure background music resumes when returning from arena
        AudioManager.playMusic("audio/sound_background.wav");
        setView(storyPanel);
    }

    public void changeToLeaderboard(String username) {
        LeaderBoardPanel leaderboard = new LeaderBoardPanel(this, username);
        setView(leaderboard);
    }

    public void changeToCharacterSelect(String username, int chapterNumber) {
        // Create the new panel, PASSING the chapterNumber
        ImagePanel_Character charPanel = new ImagePanel_Character(this, username, chapterNumber);
        setView(charPanel);
    }

    public void changeToAchievement() {
        // Create the achievement panel
        ImagePanel_Ac acPanel = new ImagePanel_Ac(this);
        setView(acPanel);
    }

    public void changeToGameRule() {
        ImagePanel_GameRule gameRulePanel = new ImagePanel_GameRule(this);
        setView(gameRulePanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame gui = new MainFrame();
            gui.setVisible(true);
        });
    }

    private void setView(JComponent panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    public String getCurrentUsername() {
        return this.currentUsername;
    }

}