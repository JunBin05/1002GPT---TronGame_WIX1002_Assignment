package UI;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Tron Game");
        
        // 1. REMOVE BORDERS (Optional: delete if you want the X button)
        window.setUndecorated(true); 

        // 2. MAXIMIZE WINDOW
        window.setExtendedState(JFrame.MAXIMIZED_BOTH); 

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        
        window.pack(); // Resize GamePanel to fit window
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // 3. START GAME FLOW
        gamePanel.setupGame(); 
        gamePanel.startGameThread();
    }
}