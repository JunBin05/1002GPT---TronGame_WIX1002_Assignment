package UI;

import javax.swing.*;
import java.awt.*;

public class ProfileName extends JComponent {

    private String username;

    public ProfileName(String username) {
        this.username = username;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        // Turn on "Anti-Aliasing" to make text look smooth and professional
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        // 1. Set Text Color
        g2.setColor(Color.WHITE);
        
        // 2. Set Font (You can make it bigger here if you want)
        g2.setFont(new Font("Times New Roman", Font.BOLD, 25));

        // 3. Draw the Text
        // "Id : [username]"
        // Coordinates (x, y) - adjust 'y' to move text up/down
        g2.drawString("Id : " + username, 10, 30);
    }
}