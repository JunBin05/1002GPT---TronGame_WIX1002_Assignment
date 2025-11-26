package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ImagePanel_GameMode extends JPanel {

    private Image backgroundImage;
    private HomeButton homeButton; //declare home button
    private MainFrame mainFrameRef;
    private String username;

    // Constructor: Only needs the image path
    public ImagePanel_GameMode(String imagePath, MainFrame mainFrame, String username) {

        this.mainFrameRef = mainFrame;
        this.username = username;
        
        setLayout(null); 

        this.backgroundImage = new ImageIcon(imagePath).getImage();

        //1.Create Home Button
        homeButton = new HomeButton("images/home_button.png",this.mainFrameRef, this.username);

        homeButton.addActionListener(e -> {
            if (mainFrameRef != null) {
                // Switch back to Home, passing the username back!
                mainFrameRef.changeToHome(this.username); 
            }
        });

        add(homeButton);


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

        //
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