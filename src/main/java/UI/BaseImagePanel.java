package UI;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

//Shared panel base: background image handling + optional back button helpers.

public abstract class BaseImagePanel extends JPanel {

    protected Image backgroundImage;
    protected BackButton backButton;

    protected BaseImagePanel() {
        super();
    }

    protected BaseImagePanel(String imagePath) {
        super();
        setBackgroundImage(imagePath);
    }

    protected void setBackgroundImage(String imagePath) {
        if (imagePath == null) {
            backgroundImage = null;
            return;
        }
        backgroundImage = new ImageIcon(imagePath).getImage();
    }

    protected void setBackgroundImage(Image image) {
        backgroundImage = image;
    }

    protected void setupBackButton(Runnable onBack) {
        backButton = new BackButton("images/back_button.png");
        if (onBack != null) {
            backButton.addActionListener(e -> onBack.run());
        }
        add(backButton);
    }

    protected void positionBackButton(int height) {
        if (backButton == null)
            return;
        int size = (int) (height * 0.18);
        backButton.setBounds(30, 30, size, size);
        backButton.resizeIcon(size);
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
