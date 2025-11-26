package UI;

import javax.sound.sampled.*;
import java.io.File;

public class AudioManager {

    private static Clip backgroundMusic;
    private static boolean isMuted = false;

    /**
     * Loads and plays music only if it isn't already playing.
     */
    public static void playMusic(String filepath) {
        // 1. If music is already running, DO NOTHING.
        // This prevents the "Double Sound" issue.
        if (backgroundMusic != null && backgroundMusic.isOpen()) {
            return;
        }

        try {
            File musicFile = new File(filepath);
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioInput);
                
                // Only start playing if we aren't muted
                if (!isMuted) {
                    backgroundMusic.start();
                    backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles Play/Pause
     */
    public static void toggleMute() {
        isMuted = !isMuted;

        if (backgroundMusic != null) {
            if (isMuted) {
                backgroundMusic.stop(); // Pause
            } else {
                backgroundMusic.start(); // Resume
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    /**
     * Helper to check state for button icons
     */
    public static boolean isMuted() {
        return isMuted;
    }
}