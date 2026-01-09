package UI;

import javax.swing.*;

public class SoundButton extends IconButton {

    public SoundButton(String imagePath, String musicPath) {
        super(imagePath);

        AudioManager.playMusic(musicPath);

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
}