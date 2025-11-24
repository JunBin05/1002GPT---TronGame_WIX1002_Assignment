package UI;

import javax.swing.JFrame;
import java.awt.BorderLayout;

// Notice all the imports for JLabel, Font, Color, GridBagLayout, etc. are GONE

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Tron Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Use BorderLayout for the JFrame itself

        // 1. Create an instance of our new, powerful ImagePanel
        //    This panel now handles ALL drawing and clicks.
        ImagePanel backgroundPanel = new ImagePanel("images/tron_2.png");
        
        // 2. Add it to the frame
        add(backgroundPanel, BorderLayout.CENTER); 
        
        // ALL the code for JLabels, GridBagConstraints, and LoginButton is REMOVED.
    }

        public static void main(String[] args) {
        // 1. Ensure GUI updates happen on the Event Dispatch Thread (EDT)
            javax.swing.SwingUtilities.invokeLater(() -> {
            
            // 2. Launch the SignUp/Authentication Frame first
                SignUpFrame authFrame = new SignUpFrame();
                authFrame.setVisible(true);

            /*
            * NOTE: In a real application, you would NOT launch MainFrame here. 
            * You would launch MainFrame ONLY after the user successfully
            * registers or signs in within the SignUpFrame.
            * For testing, we launch MainFrame immediately to see it.
            */
            
            // // Launch MainFrame (The game window) for testing:
            // MainFrame gui = new MainFrame();
            // gui.setSize(1000, 600); 
            // gui.setVisible(true);
        });
    }
}