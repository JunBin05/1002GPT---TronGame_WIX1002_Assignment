package UI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.Cursor; // For the hand cursor
import java.awt.event.MouseAdapter; // For click events
import java.awt.event.MouseEvent;

/**
 * A custom component that groups an icon and text into a
 * single, clickable "button".
 */
public class LoginButton extends JPanel {

    public LoginButton(String imagePath, String text) {
        // 1. Set layout and make the panel transparent
        super(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        // 2. Load and scale the icon
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image originalImage = originalIcon.getImage();
        int newWidth = 100;
        int newHeight = 100; 
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel iconLabel = new JLabel(scaledIcon);

        gbc.gridx = 0;
        gbc.gridy = 0; // Icon is in Row 0
        gbc.anchor = GridBagConstraints.CENTER;
        add(iconLabel, gbc);

        // 3. Create the text label
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        gbc.gridy = 1; // Text is in Row 1
        gbc.insets = new Insets(2, 0, 0, 0); // 2px top padding
        add(textLabel, gbc);

        // 4. Add the click and hover behavior to the *entire panel*
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Change cursor to "hand" on hover
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Change cursor back to normal
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // --- THIS IS THE ACTION ---
                // Create and show the SignUpFrame when clicked
                SignUpFrame signUp = new SignUpFrame();
                signUp.setVisible(true);
            }
        });
    }
}