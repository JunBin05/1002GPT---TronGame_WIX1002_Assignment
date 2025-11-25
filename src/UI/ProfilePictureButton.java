package UI;

import javax.swing.*;
import java.awt.*;

public class ProfilePictureButton extends JButton {

    private Image originalImage;
    private int currentSize = 0; // To remember the current size for refreshing

    public ProfilePictureButton(String imagePath) {
        // 1. Load the image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Make the button transparent (Ghost style)
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Add a simple click action
        addActionListener(e -> showGenderSelection());
    }

    private void showGenderSelection(){
        // Options for the buttons
        Object[] options = {"Male", "Female"};

        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose your avatar:",
            "Select Gender",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,     // No custom icon
            options,  // The button text options
            options[0] // Default button
        );       

        // Check what user clicked
        if (choice == JOptionPane.YES_OPTION) { // User clicked "Male" (Index 0)
            updateProfileImage("images/male_profile.png");
        } 
        else if (choice == JOptionPane.NO_OPTION) { // User clicked "Female" (Index 1)
            updateProfileImage("images/female_profile.png");
        }
    }

    private void updateProfileImage(String newPath) {
        // 1. Load the new image
        ImageIcon newIcon = new ImageIcon(newPath);
        
        // Check if file exists to prevent errors
        if (newIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
            System.err.println("Error: Could not find image at " + newPath);
            return;
        }

        // 2. Update the 'originalImage' variable so future resizing works on the NEW image
        this.originalImage = newIcon.getImage();

        // 3. Re-apply the current size immediately so it looks correct instantly
        if (currentSize > 0) {
            resizeIcon(currentSize);
        }
        
        // 4. Refresh the button
        repaint();
    }


    /**
     * Resizes the icon image to fit a specific size.
     * Call this from the main panel when the window resizes.
     */
    public void resizeIcon(int size) {

        // Save the size so we can use it in updateProfileImage() later
        this.currentSize = size;

        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}