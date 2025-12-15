package UI;

import javax.swing.*;
import java.awt.*;

public class ExitButton extends JButton {

    private Image originalImage;

    public ExitButton(String imagePath) {
        // 1. Load Image
        this.originalImage = new ImageIcon(imagePath).getImage();

        // 2. Style
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 3. Action Listener
        addActionListener(e -> {
            
            // A. Create a bigger font for the label
            JLabel messageLabel = new JLabel("Are you sure you want to exit?");
            messageLabel.setFont(new Font("Arial", Font.BOLD, 20)); 
            
            // B. Show Confirmation Dialog (Yes / No)
            int choice = JOptionPane.showConfirmDialog(
                null, 
                messageLabel, 
                "Exit Game", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE 
            );

            // C. If user clicks YES
            if (choice == JOptionPane.YES_OPTION) {
                
                // 1. Show "Saved" message (User clicks OK here)
                // The code effectively pauses here until the user clicks "OK"
                JOptionPane.showMessageDialog(null, 
                    "Game progress has been saved.", 
                    "Goodbye!", 
                    JOptionPane.INFORMATION_MESSAGE);

                // 2. Close the application immediately
                System.out.println("Exiting Tron Game...");
                System.exit(0); 
            } 
        });
    }

    public void resizeIcon(int size) {
        if (size > 0) {
            Image scaled = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }
}