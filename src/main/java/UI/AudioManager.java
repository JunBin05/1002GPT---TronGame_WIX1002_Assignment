package UI;

import javax.sound.sampled.*;
import java.io.File;

public class AudioManager {

    private static Clip backgroundMusic;
    private static boolean isMuted = false;
    private static String currentTrackPath = null;

    // Loads and plays music only if it isn't already playing.

    public static void playMusic(String filepath) {
        // If the same track is already playing, do nothing
        if (backgroundMusic != null && backgroundMusic.isOpen() && filepath.equals(currentTrackPath)) {
            return;
        }

        // If a different track is playing, fade it out before switching
        fadeOutAndClose(1000);

        try {
            File musicFile = new File(filepath);
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioInput);
                currentTrackPath = filepath;

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

    // Toggles Play/Pause
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

    // Internal helper: fade out current clip then close
    private static void fadeOutAndClose(int durationMs) {
        if (backgroundMusic == null)
            return;

        Clip clipRef = backgroundMusic;
        FloatControl gain = getGainControl(clipRef);
        if (gain != null && durationMs > 0) {
            float startGain = gain.getValue();
            float minGain = gain.getMinimum();
            int steps = 15;
            int stepDelay = Math.max(10, durationMs / steps);
            try {
                for (int i = 0; i < steps; i++) {
                    float ratio = (float) (i + 1) / steps;
                    float newGain = startGain + (minGain - startGain) * ratio;
                    gain.setValue(newGain);
                    Thread.sleep(stepDelay);
                }
            } catch (InterruptedException ignored) {
            } catch (Exception ignored) {
            }
        }

        try {
            clipRef.stop();
            clipRef.close();
        } catch (Exception ignored) {
        } finally {
            if (backgroundMusic == clipRef) {
                backgroundMusic = null;
                currentTrackPath = null;
            }
        }
    }

    private static FloatControl getGainControl(Clip clip) {
        try {
            return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper to check state for button icons
     */
    public static boolean isMuted() {
        return isMuted;
    }
}