package UI;

import javax.swing.*;
import java.awt.*;

public class ProfilePictureButton extends JButton {

    private Image originalImage;
    private int currentSize = 0;
    
    // --- NEW FIELDS ---
    private String currentUsername;
    private DatabaseManager dbManager;

    // --- UPDATED CONSTRUCTOR ---
    public ProfilePictureButton(String initialImagePath, String username, DatabaseManager db) {
        this.currentUsername = username;
        this.dbManager = db;

        // 1. Load the image
        updateInternalImage(initialImagePath);

        // 2. Make the button transparent
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Click action
        addActionListener(e -> showGenderSelection());
    }

    private void showGenderSelection(){
        Object[] options = {"Male", "Female"};
        int choice = JOptionPane.showOptionDialog(this, "Choose your avatar:", "Select Gender",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);       

        String newPath = null;
        if (choice == JOptionPane.YES_OPTION) { 
            newPath = "images/male_profile.png";
        } 
        else if (choice == JOptionPane.NO_OPTION) { 
            newPath = "images/female_profile.png";
        }

        // If user made a choice
        if (newPath != null) {
            // A. Update the screen visually
            updateInternalImage(newPath);
            
            // B. SAVE TO DATABASE
            dbManager.setProfileImage(currentUsername, newPath);
        }
    }

    private void updateInternalImage(String path) {
        ImageIcon newIcon = new ImageIcon(path);
        if (newIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
            // Fallback if image missing
            newIcon = new ImageIcon("images/default_profile.png"); 
        }
        this.originalImage = newIcon.getImage();
        if (currentSize > 0) resizeIcon(currentSize); // Refresh size
        repaint();
    }

    public void resizeIcon(int size) {
        this.currentSize = size;
        if (size > 0 && originalImage != null) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}