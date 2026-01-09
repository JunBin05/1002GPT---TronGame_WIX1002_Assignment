package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class AchievementPopup extends JPanel {

    // MEMORY TO PREVENT DUPLICATES
    private static Set<String> shownAchievements = new HashSet<>();

    public AchievementPopup(String title, String description) {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.CYAN, 4),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel iconLabel = new JLabel("🏆");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(">> " + title.toUpperCase() + " <<");
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 28));

        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font("Monospaced", Font.BOLD, 18));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        add(textPanel, BorderLayout.CENTER);

        setSize(600, 150);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void show(JFrame frame, String title, String description) {
        if (frame == null)
            return;

        // CHECK DUPLICATES
        if (shownAchievements.contains(title)) {
            return;
        }
        shownAchievements.add(title);

        AchievementPopup popup = new AchievementPopup(title, description);

        int x = (frame.getWidth() - popup.getWidth()) / 2;
        int y = 50;
        popup.setLocation(x, y);

        JLayeredPane layeredPane = frame.getLayeredPane();
        layeredPane.add(popup, JLayeredPane.POPUP_LAYER);
        layeredPane.moveToFront(popup);

        // PLAY SOUND
        playSuccessSound();

        Timer timer = new Timer(4000, e -> {
            if (popup.getParent() != null) {
                layeredPane.remove(popup);
                layeredPane.repaint();
            }
        });
        timer.setRepeats(false);
        timer.start();

        MouseAdapter closeAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timer.stop();
                if (popup.getParent() != null) {
                    layeredPane.remove(popup);
                    layeredPane.repaint();
                }
            }
        };

        popup.addMouseListener(closeAction);
        addListenersRecursively(popup, closeAction);
    }

    // UPDATED SOUND METHOD FOR YOUR FOLDER STRUCTURE
    private static void playSuccessSound() {
        try {
            // Look for the file in the project root "audio" folder
            File soundFile = new File("audio/sound_award.wav");

            if (soundFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                // Close memory when done
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

                clip.start();
            } else {
                System.err.println("Error: File not found at " + soundFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addListenersRecursively(Container container, MouseAdapter listener) {
        for (Component c : container.getComponents()) {
            c.addMouseListener(listener);
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (c instanceof Container) {
                addListenersRecursively((Container) c, listener);
            }
        }
    }

    public static void resetHistory() {
        shownAchievements.clear();
    }
}