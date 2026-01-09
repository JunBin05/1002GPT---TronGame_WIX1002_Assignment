package UI;

import javax.swing.*;
import java.awt.*;

public class ProfilePictureButton extends IconButton {

    private final String currentUsername;
    private final DatabaseManager dbManager;

    public ProfilePictureButton(String initialImagePath, String username, DatabaseManager db) {
        super(initialImagePath);
        this.currentUsername = username;
        this.dbManager = db;

        // Ensure the initial image is loaded via base class
        updateInternalImage(initialImagePath);
        addActionListener(e -> showGenderSelection());
    }

    private void showGenderSelection() {
        Object[] options = { "Male", "Female" };
        int choice = JOptionPane.showOptionDialog(this, "Choose your avatar:", "Select Gender",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        String newPath = null;
        if (choice == JOptionPane.YES_OPTION) {
            newPath = "images/male_profile.png";
        } else if (choice == JOptionPane.NO_OPTION) {
            newPath = "images/female_profile.png";
        }

        // If user made a choice
        if (newPath != null) {
            // Update the screen visually
            updateInternalImage(newPath);

            // Save to Database
            dbManager.setProfileImage(currentUsername, newPath);
        }
    }

    private void updateInternalImage(String path) {
        ImageIcon newIcon = new ImageIcon(path);
        if (newIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
            newIcon = new ImageIcon("images/default_profile.png");
        }
        setBaseImage(newIcon.getImage());
        repaint();
    }
}