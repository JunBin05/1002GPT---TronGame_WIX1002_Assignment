package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpFrame extends JFrame {

    private static final String SIGN_IN_PANEL = "SIGN_IN";
    private static final String REGISTER_PANEL = "REGISTER";
    private JPanel cards; 
    private JTextField registerUserIdField;
    private JPasswordField registerPasswordField;
    private JTextField signInUserIdField; 
    private JPasswordField signInPasswordField; 
    private DatabaseManager dbManager; 

    // --- NEW FIELD ---
    private MainFrame mainFrameRef; 

    // --- MODIFIED CONSTRUCTOR ---
    public SignUpFrame(MainFrame mainFrame) { 
        super("Authentication");
        
        // Save the reference
        this.mainFrameRef = mainFrame;
        
        setSize(400, 200); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); 

        dbManager = new DatabaseManager();
        cards = new JPanel(new CardLayout());
        
        JPanel signInPanel = createSignInPanel(); 
        JPanel registerPanel = createRegisterPanel();

        cards.add(signInPanel, SIGN_IN_PANEL);
        cards.add(registerPanel, REGISTER_PANEL);

        add(cards, BorderLayout.CENTER);
        showCard(SIGN_IN_PANEL);
    }

    // Default constructor for safety (optional)
    public SignUpFrame() {
        this(null);
    }

    private JPanel createSignInPanel() {
        signInUserIdField = new JTextField(15);
        signInPasswordField = new JPasswordField(15);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(signInUserIdField); 
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(signInPasswordField); 

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton signInButton = new JButton("Log In");
        JButton registerSwitchButton = new JButton("Register"); 

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dbManager.isReady()) {
                    JOptionPane.showMessageDialog(SignUpFrame.this, "System Error", "Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }

                String userId = signInUserIdField.getText().trim();
                String password = new String(signInPasswordField.getPassword());

                
                if (dbManager.checkLogin(userId, password)) {
                    JOptionPane.showMessageDialog(SignUpFrame.this,
                        "Log In successful! Welcome, " + userId + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // --- NEW LOGIC: SWITCH MAINFRAME TO HOME ---
                    if (mainFrameRef != null) {
                        mainFrameRef.changeToHome(userId);
                    }
                    // -------------------------------------------

                    // Close the popup window
                    SignUpFrame.this.dispose();
                    
                } else {
                    JOptionPane.showMessageDialog(SignUpFrame.this, "Login failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                signInPasswordField.setText(""); 
            }
        });

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

    private JPanel createRegisterPanel() {
        registerUserIdField = new JTextField(15);
        registerPasswordField = new JPasswordField(15);
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(registerUserIdField); 
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(registerPasswordField); 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton signUpButton = new JButton("Sign Up");
        JButton cancelSwitchButton = new JButton("Cancel"); 
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dbManager.isReady()) return; 
                String userId = registerUserIdField.getText().trim();
                String password = new String(registerPasswordField.getPassword()); 

                if (userId.trim().isEmpty() || password.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(SignUpFrame.this, 
                        "Username and Password cannot be empty!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return; 
                }
                boolean success = dbManager.registerUser(userId, password);
                if (success) {
                    JOptionPane.showMessageDialog(SignUpFrame.this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    registerUserIdField.setText("");
                    registerPasswordField.setText("");
                    showCard(SIGN_IN_PANEL);
                    setTitle("Authentication");
                } else {
                    JOptionPane.showMessageDialog(SignUpFrame.this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
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