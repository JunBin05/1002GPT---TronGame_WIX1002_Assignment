package arena;

import javax.swing.*;
import java.awt.*;
import java.io.File; 
import java.net.URL; 
import java.util.List; 
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

// --- Package Imports ---
import characters.Character; 
import characters.Tron;
import characters.Kevin;
import characters.CharacterLoader; 
import characters.CharacterData; 
import characters.Direction;

// Root Package Imports (Files in src/):
import controller.GameController; 
import UI.StartGameMenu; 

public class ArenaLoader {

    // --- Helper: Conversion methods for Rotation ---
    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    private static BufferedImage rotateImage(BufferedImage image, double angleRadians) {
        double sin = Math.abs(Math.sin(angleRadians));
        double cos = Math.abs(Math.cos(angleRadians));
        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotatedImage.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate(newWidth / 2, newHeight / 2);
        at.rotate(angleRadians);
        at.translate(-width / 2, -height / 2);
        g2d.drawImage(image, at, null);
        g2d.dispose();
        return rotatedImage;
    }
    // ----------------------------------------------

    // --- Helper: Load + Scale Image from File System ---
    private static ImageIcon loadAndScale(String relativePath, int size) {
        try {
            String fullPath = Paths.get(relativePath).toAbsolutePath().toString();
            File imageFile = new File(fullPath);
            
            if (!imageFile.exists()) {
                imageFile = new File(System.getProperty("user.dir") + File.separator + relativePath);
                if (!imageFile.exists()) {
                    System.err.println("⚠ IMAGE NOT FOUND at path: " + relativePath);
                    return null;
                }
            }

            ImageIcon original = new ImageIcon(imageFile.getAbsolutePath());
            
            if (original.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } else {
                System.err.println("⚠ IMAGE LOADING FAILED: " + relativePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("ERROR loading image: " + relativePath + ". Exception: " + e.getMessage());
            return null;
        }
    }

    // --- Core Arena Load Logic ---
    public static Arena loadArena(int choice) {
        return switch (choice) {
            case 1 -> new ArenaOne();
            case 2 -> new ArenaTwo();
            case 3 -> new ArenaThree();
            case 4 -> new RandomArena();
            default -> new ArenaOne();
        };
    }
    
    // --- Load Icons and Generate Rotations (Called once at start) ---
    private static Map<String, ImageIcon> loadAllIcons(JFrame frame) {
        // ... (Icon loading and rotation logic remains the same)
        Map<String, ImageIcon> icons = new HashMap<>();


        final int HUD_ICON_SIZE = 40;
        final double SCALE_MULTIPLIER = 2;
        final double SCALE_MULTIPLIER_C = 3;
        final int TRUE_CELL_SIZE = frame.getWidth() / 40;
        final int CELL_SIZE = (int) (TRUE_CELL_SIZE * SCALE_MULTIPLIER);
        final int CELL_SIZE_C = (int) (TRUE_CELL_SIZE * SCALE_MULTIPLIER_C);

        icons.put("obstacle", loadAndScale("images" + File.separator + "obstacle.png", CELL_SIZE));
        icons.put("speed", loadAndScale("images" + File.separator + "Speed.png", CELL_SIZE));
        icons.put("heart_full", loadAndScale("images" + File.separator + "heart_full.png", HUD_ICON_SIZE)); // <-- NEW
        icons.put("heart_half", loadAndScale("images" + File.separator + "heart_half.png", HUD_ICON_SIZE)); // <-- NEW
        icons.put("heart_empty", loadAndScale("images" + File.separator + "heart_empty.png", HUD_ICON_SIZE)); // <-- NEW
        ImageIcon baseTronIcon = loadAndScale("images" + File.separator + "Tron.png", CELL_SIZE_C);
        ImageIcon baseKevinIcon = loadAndScale("images" + File.separator + "kevin.png", CELL_SIZE_C);
        
        if (baseTronIcon != null && baseTronIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            BufferedImage tronImg = toBufferedImage(baseTronIcon.getImage());
            icons.put("tron_NORTH", baseTronIcon);
            icons.put("tron_EAST", new ImageIcon(rotateImage(tronImg, Math.toRadians(90))));
            icons.put("tron_SOUTH", new ImageIcon(rotateImage(tronImg, Math.toRadians(180))));
            icons.put("tron_WEST", new ImageIcon(rotateImage(tronImg, Math.toRadians(270))));
        }
        
        if (baseKevinIcon != null && baseKevinIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            BufferedImage kevinImg = toBufferedImage(baseKevinIcon.getImage());
            icons.put("kevin_NORTH", baseKevinIcon);
            icons.put("kevin_EAST", new ImageIcon(rotateImage(kevinImg, Math.toRadians(90))));
            icons.put("kevin_SOUTH", new ImageIcon(rotateImage(kevinImg, Math.toRadians(180))));
            icons.put("kevin_WEST", new ImageIcon(rotateImage(kevinImg, Math.toRadians(270))));
        }

        return icons;
    }
    
    // --- NEW: Method to Create the HP/Stat Display Panel ---
    public static JPanel createHUDPanel(Character player) {
        JPanel hudPanel = new JPanel();
        // Use a static name to easily retrieve this component later for updates
        hudPanel.setName("MainHUDPanel"); 
        hudPanel.setBackground(Color.BLACK); // Dark background
        hudPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));

        JLabel title = new JLabel(player.name + " // LIFE POWER: ");
        title.setForeground(new Color(0, 255, 255)); // Cyan glow
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        hudPanel.add(title);

        JPanel hpContainer = new JPanel();
        hpContainer.setName("HPContainer"); // Give container a unique name
        hpContainer.setBackground(Color.BLACK);
        hpContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Flow layout for icons
        hudPanel.add(hpContainer); // Add the empty container to the HUD panel
            
        return hudPanel;
    }

    // --- NEW: Method to Update the HP Display ---
    public static void updateHUD(JPanel hudPanel, Character player, Map<String, ImageIcon> icons) { 
        // 1. Find the specific HP container panel
        JPanel hpContainer = null;
        for (Component comp : hudPanel.getComponents()) {
            if ("HPContainer".equals(comp.getName()) && comp instanceof JPanel) {
                hpContainer = (JPanel) comp;
                break;
            }
        }
        if (hpContainer == null) return;

        // 2. Clear all previous heart icons
        hpContainer.removeAll();

        // --- Logic for drawing hearts ---
        double currentLives = player.getLives();
        
        // CRITICAL FIX: If lives are effectively zero (or less), force the count to zero.
        int totalHalfUnits;
        if (currentLives <= 0.001) { 
            totalHalfUnits = 0; // The player is dead. Display 0 half-units.
        } else {
            // Otherwise, use rounding to accurately convert 2.5 to 5, 2.0 to 4, etc.
            totalHalfUnits = (int) Math.round(currentLives * 2);
        }
        
        int maxHearts = 3; 

        // Iterate over the number of *full heart slots* (3 times total)
        for (int heartIndex = 0; heartIndex < maxHearts; heartIndex++) { 
            ImageIcon icon = null;
            
            // Calculate the current half-unit count remaining for this specific heart slot
            int currentSlotHalfUnits = totalHalfUnits - (heartIndex * 2);
            
            if (currentSlotHalfUnits >= 2) {
                // Full Heart (2 half-units or more remaining for this slot)
                icon = icons.get("heart_full");
            } else if (currentSlotHalfUnits == 1) {
                // Half Heart (Exactly 1 half-unit remaining)
                icon = icons.get("heart_half");
            } else {
                // Empty Heart (0 half-units remaining for this slot)
                icon = icons.get("heart_empty");
            }
            
            // Add the heart icon if found
            if (icon != null) {
                JLabel heartLabel = new JLabel(icon);
                hpContainer.add(heartLabel);
            }
        }

        // 3. Redraw the container
        hudPanel.revalidate();
        hudPanel.repaint();
    }


    /**
     * Updates the existing JFrame with the current state of the arena and cycles.
     * This method is called repeatedly by the GameController thread.
     */
    public static void redrawArena(JFrame frame, Arena arena, List<Character> cycles, Map<String, ImageIcon> icons, JPanel arenaPanel, JPanel hudPanel) {
        
        // 1. Clear previous components from the Arena Panel
        arenaPanel.removeAll(); 
        
        Character playerCycle = cycles.get(0);
        char[][] grid = arena.getGrid();
<<<<<<< HEAD
        if (tron.r >= 0 && tron.r < 40 && tron.c >= 0 && tron.c < 40) {
            grid[tron.r][tron.c] = '.';
        }
=======
>>>>>>> main
        
        for (int r = 0; r < 40; r++) {
            for (int c = 0; c < 40; c++) {

                JLabel cell = new JLabel();
                cell.setOpaque(true);
                cell.setHorizontalAlignment(JLabel.CENTER);
                cell.setVerticalAlignment(JLabel.CENTER);
                cell.setIcon(null); 
                
                ImageIcon cycleIconToDraw = null;
                
                // A. Check if a Character is at this (r, c) location
                for (Character cycle : cycles) {
                    if (cycle.getRow() == r && cycle.getCol() == c) {
                        String iconKey = cycle.name.toLowerCase() + "_" + cycle.currentDirection.toString();
                        cycleIconToDraw = icons.get(iconKey);
                        break;
                    }
                }

                if (cycleIconToDraw != null) {
                    cell.setBackground(Color.BLACK);
                    cell.setIcon(cycleIconToDraw);
                } else {
                    // B. Draw the static Arena elements
                    switch (grid[r][c]) {
                        case '#': // WALL
                            cell.setBackground(new Color(0, 120, 255));
                            break;
                        case 'O': // OBSTACLE
                            cell.setBackground(Color.BLACK);
                            if (icons.get("obstacle") != null) cell.setIcon(icons.get("obstacle"));
                            else cell.setBackground(new Color(255, 140, 0)); 
                            break;
                        case 'S': // SPEED RAMP
                            cell.setBackground(Color.BLACK);
                            if (icons.get("speed") != null) cell.setIcon(icons.get("speed"));
                            else cell.setBackground(new Color(0, 255, 255)); 
                            break;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 3bba3f20c0e789dc0b50cfccdb6f291f5c6ff90c
                        case 'T':
                            cell.setBackground(new Color(0, 150, 255));
                            break;
                        case 'K':
                            cell.setBackground(Color.WHITE);
                            break;
<<<<<<< HEAD
=======
>>>>>>> main
=======
>>>>>>> 3bba3f20c0e789dc0b50cfccdb6f291f5c6ff90c
                        default:
                            cell.setBackground(Color.BLACK);
                    }
                }
                arenaPanel.add(cell);
            }
        }

        // 2. Refresh the HUD 
        updateHUD(hudPanel, cycles.get(0), icons);
        
        // 3. Finalize and display changes
        arenaPanel.revalidate(); 
        arenaPanel.repaint();
        frame.revalidate(); // Revalidate the whole frame to show HUD changes
    }

    // Inside ArenaLoader.java (Add this method)

    public static void showGameOverDialog(JFrame parentFrame) {
        // Define the dark neon background color used in the MainMenu
        Color NEON_PURPLE_BG = new Color(15, 0, 40); 
        
        JDialog gameOverDialog = new JDialog(parentFrame, "Game Over", true);
        gameOverDialog.setSize(400, 200);
        gameOverDialog.setLayout(new BorderLayout());
        gameOverDialog.setLocationRelativeTo(parentFrame);
        gameOverDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // --- FIX: Set the Content Pane background to the neon color ---
        gameOverDialog.getContentPane().setBackground(NEON_PURPLE_BG); 

        // --- Title ---
        JLabel title = new JLabel("::: DEREZZED :::", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 36));
        title.setForeground(Color.RED); 
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        gameOverDialog.add(title, BorderLayout.NORTH);

        // --- Message ---
        JLabel message = new JLabel("Your Light Cycle has been terminated.", SwingConstants.CENTER);
        message.setFont(new Font("Monospaced", Font.PLAIN, 14));
        message.setForeground(Color.LIGHT_GRAY);
        gameOverDialog.add(message, BorderLayout.CENTER);

        // --- Button to close ---
        JButton closeButton = new JButton("EXIT GRID");
        closeButton.setFont(new Font("Monospaced", Font.BOLD, 14));
        closeButton.setBackground(new Color(50, 50, 50)); 
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> System.exit(0)); 
        
        JPanel buttonPanel = new JPanel();
        
        // --- FIX: Set the Button Panel background to the neon color ---
        buttonPanel.setBackground(NEON_PURPLE_BG); 
        
        buttonPanel.add(closeButton);
        
        gameOverDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        gameOverDialog.setVisible(true);
    }

    public static void showArenaWindow(Arena arena) {
        // This static viewer method is now obsolete for the game loop
    }

    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        Color NEON_PURPLE_BG = new Color(15, 0, 40);
        // 1. SETUP: Load Arena and Create Frame
        Arena arena = loadArena(1); 
        JFrame frame = new JFrame("Tron Arena Viewer - Dynamic");
        frame.getContentPane().setBackground(NEON_PURPLE_BG);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 850);
        // --- KEY CHANGE: Use BorderLayout ---
        frame.setLayout(new BorderLayout()); 

        // 2. SETUP: Load Icons and Characters (File I/O)
        Map<String, ImageIcon> icons = loadAllIcons(frame);
        
        CharacterData tronData = CharacterLoader.loadCharacterData("Tron");
        CharacterData kevinData = CharacterLoader.loadCharacterData("Kevin");
        
        Tron tron = new Tron();
        Kevin kevin = new Kevin();
        
        if (tronData != null) tron.loadInitialAttributes(tronData);
        if (kevinData != null) kevin.loadInitialAttributes(kevinData);
        
        // Set initial state
        tron.r = 20; tron.c = 15;
        tron.currentDirection = Direction.EAST;
        List<Character> cycles = List.of(tron, kevin);

        // 3. SETUP: Create Panels
        JPanel arenaPanel = new JPanel(new GridLayout(40, 40));
        JPanel hudPanel = createHUDPanel(tron); // Create HUD for Tron
        
        frame.add(hudPanel, BorderLayout.NORTH);
        frame.add(arenaPanel, BorderLayout.CENTER);
        
        // Display the frozen map
        redrawArena(frame, arena, cycles, icons, arenaPanel, hudPanel);
        frame.setVisible(true);

        // 4. SHOW MAIN MENU (Execution pauses here until 'START' is clicked)
        StartGameMenu.showMenu(frame);

        // 5. START GAME LOOP (Runs ONLY after menu closes)
        System.out.println("Starting Game Simulation...");
        
        GameController controller = new GameController(frame, arena, cycles, icons, arenaPanel, hudPanel); // Pass new panels
        
        new Thread(controller).start();
        
        // Re-draw once more to finalize loop start
        redrawArena(frame, arena, cycles, icons, arenaPanel, hudPanel);
    }
}