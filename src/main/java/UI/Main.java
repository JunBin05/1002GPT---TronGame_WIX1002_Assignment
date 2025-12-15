package UI;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Tron Game");
        
        // 1. REMOVE BORDERS (Optional: delete this line if you want the X button)
        window.setUndecorated(true); 

        // 2. MAXIMIZE WINDOW
        window.setExtendedState(JFrame.MAXIMIZED_BOTH); 

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        
        // 3. PACK AND SHOW
        window.pack(); // This will resize the GamePanel to fit the full window
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}