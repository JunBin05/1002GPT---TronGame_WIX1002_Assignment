package UI;

import javax.swing.*;
import java.awt.*;

public class SoundButton extends JButton {

    private Image originalImage;

    public SoundButton(String imagePath, String musicPath) {
        // 1. Load image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Style the button
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. START MUSIC VIA MANAGER
        // This line will be called every time the button is made, 
        // BUT AudioManager will ignore it if music is already playing.
        AudioManager.playMusic(musicPath);

        // 4. Add Click Action
        addActionListener(e -> {
            AudioManager.toggleMute();
            updateMessage();
        });
    }

    private void updateMessage() {
        if (AudioManager.isMuted()) {
            JOptionPane.showMessageDialog(this, "Music Paused");
        } else {
            JOptionPane.showMessageDialog(this, "Music Playing");
        }
    }

    public void resizeIcon(int size) {
        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}