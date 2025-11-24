package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File; // Import for File
import java.io.IOException; // Import for Errors

// --- IMPORTS FOR AUDIO ---
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ImagePanel_Home extends JPanel {

    private Image backgroundImage;
    private JButton soundButton;
    private Image originalSoundImage; 
    private boolean isMuted = false;

    // --- NEW FIELD FOR MUSIC ---
    private Clip musicClip; 

    public ImagePanel_Home(String imagePath) {
        setLayout(null); 

        this.backgroundImage = new ImageIcon(imagePath).getImage();
        this.originalSoundImage = new ImageIcon("images/sound_button.png").getImage();

        soundButton = new JButton();
        soundButton.setBorderPainted(false);
        soundButton.setContentAreaFilled(false);
        soundButton.setFocusPainted(false);
        soundButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        soundButton.addActionListener(e -> toggleSound());
        add(soundButton);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateButtonPosition();
            }
        });

        // --- LOAD AND START MUSIC IMMEDIATELY ---
        // Change "images/bg_music.wav" to your actual file path
        playMusic("images/game_music.wav");
    }

    /**
     * --- NEW METHOD: LOAD & PLAY MUSIC ---
     */
    private void playMusic(String filepath) {
        try {
            File musicPath = new File(filepath);

            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioInput);
                
                // Start playing
                musicClip.start();
                
                // Loop the music continuously
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                
            } else {
                System.out.println("Can't find file");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateButtonPosition() {
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW == 0 || panelH == 0) return;

        // Button Size: 15% of screen height
        int btnSize = (int) (panelH * 0.18); 

        // Position: 12% Right, Center Vertical + 100px Down
        int x = (int) (panelW * 0.30);
        int y = ((panelH / 2) - (btnSize / 2)) + 150;

        Image scaledImg = originalSoundImage.getScaledInstance(btnSize, btnSize, Image.SCALE_SMOOTH);
        soundButton.setIcon(new ImageIcon(scaledImg));
        soundButton.setBounds(x, y, btnSize, btnSize);
    }

    /**
     * --- UPDATED TOGGLE LOGIC ---
     */
    private void toggleSound() {
        isMuted = !isMuted;
        
        if (isMuted) {
            // STOP MUSIC
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
            }
            JOptionPane.showMessageDialog(this, "Music Paused");
        } else {
            // RESUME MUSIC
            if (musicClip != null) {
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            JOptionPane.showMessageDialog(this, "Music Playing");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}