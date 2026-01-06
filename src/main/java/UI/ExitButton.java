package UI;

import javax.swing.*;
import java.awt.*;

public class ExitButton extends IconButton {

    public ExitButton(String imagePath) {
        super(imagePath);

        addActionListener(e -> {
            JLabel messageLabel = new JLabel("Are you sure you want to exit?");
            messageLabel.setFont(new Font("Arial", Font.BOLD, 20)); 

            int choice = JOptionPane.showConfirmDialog(
                null, 
                messageLabel, 
                "Exit Game", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE 
            );

            if (choice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, 
                    "Game progress has been saved.", 
                    "Goodbye!", 
                    JOptionPane.INFORMATION_MESSAGE);

                System.out.println("Exiting Tron Game...");
                System.exit(0); 
            } 
        });
    }
}