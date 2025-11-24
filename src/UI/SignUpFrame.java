package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpFrame extends JFrame {

    private static final String SIGN_IN_PANEL = "SIGN_IN";
    private static final String REGISTER_PANEL = "REGISTER";

    private JPanel cards; 

    // --- MODIFIED FIELDS ---
    // Fields for the Register panel
    private JTextField registerUserIdField;
    private JPasswordField registerPasswordField;
    
    // Fields for the Sign In panel
    private JTextField signInUserIdField; 
    private JPasswordField signInPasswordField; 

    private DatabaseManager dbManager; 
    // ----------------------------

    public SignUpFrame() {
        super("Authentication");
        setSize(350, 250); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); 

        // Initialize the Database Manager
        dbManager = new DatabaseManager();

        cards = new JPanel(new CardLayout());
        
        JPanel signInPanel = createSignInPanel(); 
        JPanel registerPanel = createRegisterPanel();

        cards.add(signInPanel, SIGN_IN_PANEL);
        cards.add(registerPanel, REGISTER_PANEL);

        add(cards, BorderLayout.CENTER);

        showCard(SIGN_IN_PANEL);
    }

    /**
     * Creates the "Sign In" panel view with the login logic.
     */
    private JPanel createSignInPanel() {
        // Initialize fields for data retrieval
        signInUserIdField = new JTextField(15);
        signInPasswordField = new JPasswordField(15);

        // --- Input Fields Panel (Top) ---
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(signInUserIdField); 
        
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(signInPasswordField); 

        // --- Buttons Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton signInButton = new JButton("Sign In");
        JButton registerSwitchButton = new JButton("Register"); 

        // *** CORRECTED SIGN IN LOGIC ***
        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Critical Database Check
                if (!dbManager.isReady()) {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                        "Database System Error: Application is offline. Check console for driver error.",
                        "System Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }

                String userId = signInUserIdField.getText().trim();
                String password = new String(signInPasswordField.getPassword());
                
                if (dbManager.checkLogin(userId, password)) {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                        "Sign In successful! Welcome, " + userId + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // TODO: Close this frame and open MainFrame here.
                    
                } else {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                        "Login failed. Invalid User ID or Password.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                signInPasswordField.setText(""); // Clear password field
            }
        });
        // *** END SIGN IN LOGIC ***

        registerSwitchButton.addActionListener(e -> {
            showCard(REGISTER_PANEL);
            setTitle("Create Account"); 
        });
        
        buttonPanel.add(signInButton);
        buttonPanel.add(registerSwitchButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Creates the "Register" (Sign Up) panel view.
     */
    private JPanel createRegisterPanel() {
        // Initialize fields for data retrieval
        registerUserIdField = new JTextField(15);
        registerPasswordField = new JPasswordField(15);
        
        // --- Input Fields Panel (Top) ---
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(registerUserIdField); 
        
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(registerPasswordField); 

        // --- Buttons Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton signUpButton = new JButton("Sign Up");
        JButton cancelSwitchButton = new JButton("Cancel"); 

        // ACTION LISTENER FOR SIGN UP (Registration logic)
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 // Critical Database Check
                if (!dbManager.isReady()) {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                        "Database System Error: Application is offline. Check console for driver error.",
                        "System Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }

                String userId = registerUserIdField.getText().trim();
                String password = new String(registerPasswordField.getPassword()); 

                boolean success = dbManager.registerUser(userId, password);

                if (success) {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                            "Registration successful! You can now sign in.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    registerUserIdField.setText("");
                    registerPasswordField.setText("");
                    
                    showCard(SIGN_IN_PANEL);
                    setTitle("Authentication");
                    
                } else {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                            "Registration failed. User ID may already exist or fields are empty.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelSwitchButton.addActionListener(e -> {
            showCard(SIGN_IN_PANEL);
            setTitle("Authentication"); 
        });

        buttonPanel.add(cancelSwitchButton);
        buttonPanel.add(signUpButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showCard(String cardName) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, cardName);
    }
}