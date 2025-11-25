package UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.sound.sampled.*;

public class SoundButton extends JButton {

    private Image originalImage;
    private Clip musicClip;
    private boolean isMuted = false;

    public SoundButton(String imagePath, String musicPath) {
        // 1. Load the Image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make the button transparent (Ghost style)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Add Click Action
        addActionListener(e -> toggleSound());

        // 4. Start Music
        loadAndPlayMusic(musicPath);
    }

    /**
     * Resizes the icon image to fit a specific size (width/height).
     * Call this whenever the window size changes.
     */
    public void resizeIcon(int size) {
        if (size <= 0) return;
        Image scaledImg = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(scaledImg));
    }

    private void loadAndPlayMusic(String filepath) {
        try {
            File musicFile = new File(filepath);
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioInput);
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleSound() {
        isMuted = !isMuted;
        if (isMuted) {
            if (musicClip != null && musicClip.isRunning()) musicClip.stop();
            JOptionPane.showMessageDialog(this, "Music Paused");
        } else {
            if (musicClip != null) {
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            JOptionPane.showMessageDialog(this, "Music Playing");
        }
    }
}