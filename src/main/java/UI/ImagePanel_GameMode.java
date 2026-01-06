package UI;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ImagePanel_GameMode extends BaseImagePanel {

    private HomeButton homeButton; // declare home button
    private StoryModeButton storyButton; // declare story mode button
    private MainFrame mainFrameRef;
    private String username;

    // Constructor: Only needs the image path
    public ImagePanel_GameMode(String imagePath, MainFrame mainFrame, String username) {

        this.mainFrameRef = mainFrame;
        this.username = username;
        
        setLayout(null); 

        setBackgroundImage(imagePath);

        //1.Create Home Button
        homeButton = new HomeButton("images/home_button.png",this.mainFrameRef, this.username);
        homeButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                // Switch back to Home, passing the username back!
                mainFrameRef.changeToHome(this.username); 
            }
        });
        add(homeButton);

        //2.Create Story Mode Button
        storyButton = new StoryModeButton("images/storymode_button.png");
        add(storyButton);

        storyButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                mainFrameRef.changeToStoryMode(); // <--- SWITCH SCREENS HERE
            }
        });

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

        // 1. Set Home Button
        int homeSize = (int) (h * 0.18);
        homeButton.setBounds(30, 30, homeSize, homeSize);
        homeButton.resizeIcon(homeSize);

        // 2. Set Story Mode Button
        int storyHeight = (int)(h * 0.18);
        int storyWidth  = (int)(storyHeight * 2.5); 

        int storyX = (int) (w - storyWidth) / 2;
        int storyY = ((h / 2) - (storyHeight / 2)) + 280;

        storyButton.setBounds(storyX, storyY, storyWidth, storyHeight);
        storyButton.resizeIcon(storyWidth, storyHeight);

    }

}