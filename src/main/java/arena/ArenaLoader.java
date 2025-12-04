package arena;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File; 
import java.nio.file.Paths;
import java.util.List; 
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

// --- Imports ---
import characters.Character; 
import characters.Tron;
import characters.Kevin;
import characters.CharacterLoader; 
import characters.CharacterData; 
import characters.Direction;
import controller.GameController; 
import UI.StartGameMenu; 

public class ArenaLoader {

    // --- Helper: Conversion methods ---
    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) return (BufferedImage) img;
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

    private static ImageIcon loadAndScale(String relativePath, int size) {
        try {
            String fullPath = Paths.get(relativePath).toAbsolutePath().toString();
            File imageFile = new File(fullPath);
            if (!imageFile.exists()) {
                imageFile = new File(System.getProperty("user.dir") + File.separator + relativePath);
                if (!imageFile.exists()) return null;
            }
            ImageIcon original = new ImageIcon(imageFile.getAbsolutePath());
            if (original.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static Arena loadArena(int choice) {
        return new ArenaOne(); 
    }
    
    // --- UPDATED: MAXIMIZED ICONS ---
    private static Map<String, ImageIcon> loadAllIcons(JFrame frame) {
        Map<String, ImageIcon> icons = new HashMap<>();
        
        // 1. GRID ICON SIZE: Use FULL screen height divided by 40 rows
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int TRUE_CELL_SIZE = (int) ((screenHeight*2.5) / 40); 
        int TRUE_CELL_SIZE_char = (int) ((screenHeight) / 40); 

        // 2. HUD ICON SIZE
        final int HUD_ICON_SIZE = 60; 
        
        icons.put("obstacle", loadAndScale("images" + File.separator + "obstacle.png", TRUE_CELL_SIZE));
        icons.put("speed", loadAndScale("images" + File.separator + "Speed.png", TRUE_CELL_SIZE));
        icons.put("heart_full", loadAndScale("images" + File.separator + "heart_full.png", HUD_ICON_SIZE)); 
        icons.put("heart_half", loadAndScale("images" + File.separator + "heart_half.png", HUD_ICON_SIZE)); 
        icons.put("heart_empty", loadAndScale("images" + File.separator + "heart_empty.png", HUD_ICON_SIZE)); 
        
        // 3. PROFILE SIZE
        icons.put("tron_profile", loadAndScale("images" + File.separator + "Tron.png", 200)); 

        ImageIcon baseTronIcon = loadAndScale("images" + File.separator + "Tron.png", TRUE_CELL_SIZE_char);
        ImageIcon baseKevinIcon = loadAndScale("images" + File.separator + "kevin.png", TRUE_CELL_SIZE_char);
        
        if (baseTronIcon != null) {
            BufferedImage img = toBufferedImage(baseTronIcon.getImage());
            icons.put("tron_NORTH", baseTronIcon);
            icons.put("tron_EAST", new ImageIcon(rotateImage(img, Math.toRadians(90))));
            icons.put("tron_SOUTH", new ImageIcon(rotateImage(img, Math.toRadians(180))));
            icons.put("tron_WEST", new ImageIcon(rotateImage(img, Math.toRadians(270))));
        }
        if (baseKevinIcon != null) {
            BufferedImage img = toBufferedImage(baseKevinIcon.getImage());
            icons.put("kevin_NORTH", baseKevinIcon);
            icons.put("kevin_EAST", new ImageIcon(rotateImage(img, Math.toRadians(90))));
            icons.put("kevin_SOUTH", new ImageIcon(rotateImage(img, Math.toRadians(180))));
            icons.put("kevin_WEST", new ImageIcon(rotateImage(img, Math.toRadians(270))));
        }
        return icons;
    }
    
    public static JPanel createSidebarPanel(Character player, Map<String, ImageIcon> icons) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(3, 1)); 
        sidebar.setBackground(new Color(5, 10, 20)); // Deep Navy Sidebar
        sidebar.setPreferredSize(new Dimension(350, 0)); 
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0, 255, 255))); 

        // ROW 1: PROFILE
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setOpaque(false);
        JLabel profilePic = new JLabel();
        profilePic.setIcon(icons.get("tron_profile")); 
        profilePic.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        JLabel nameLabel = new JLabel(player.name);
        nameLabel.setForeground(Color.CYAN);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        nameLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        profilePanel.add(profilePic, gbc);
        gbc.gridy = 1;
        profilePanel.add(nameLabel, gbc);
        sidebar.add(profilePanel);

        // ROW 2: STATS
        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel lvlLabel = new JLabel("LEVEL: 01");
        lvlLabel.setForeground(Color.WHITE);
        lvlLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JLabel stageLabel = new JLabel("STAGE: 01");
        stageLabel.setForeground(Color.GRAY);
        stageLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
        statsPanel.add(lvlLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(stageLabel);
        sidebar.add(statsPanel);

        // ROW 3: HP / INVENTORY
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        JLabel hpTitle = new JLabel("LIFE POWER", SwingConstants.CENTER);
        hpTitle.setForeground(new Color(255, 100, 100));
        hpTitle.setFont(new Font("Monospaced", Font.BOLD, 20));
        JPanel hpContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        hpContainer.setName("HPContainer"); 
        hpContainer.setOpaque(false);
        bottomPanel.add(hpTitle, BorderLayout.NORTH);
        bottomPanel.add(hpContainer, BorderLayout.CENTER);
        
        JPanel invPanel = new JPanel(new FlowLayout());
        invPanel.setOpaque(false);
        for(int i=0; i<4; i++) {
            JLabel slot = new JLabel("[ ]");
            slot.setForeground(Color.DARK_GRAY);
            slot.setFont(new Font("Monospaced", Font.PLAIN, 24));
            invPanel.add(slot);
        }
        bottomPanel.add(invPanel, BorderLayout.SOUTH);
        sidebar.add(bottomPanel);

        return sidebar;
    }

    public static void updateHUD(JPanel sidebar, Character player, Map<String, ImageIcon> icons) { 
        JPanel hpContainer = null;
        for (Component row : sidebar.getComponents()) { 
            if (row instanceof JPanel) {
                for (Component sub : ((JPanel)row).getComponents()) {
                     if ("HPContainer".equals(sub.getName())) {
                         hpContainer = (JPanel) sub;
                         break;
                     }
                }
            }
            if (hpContainer != null) break;
        }
        if (hpContainer == null) return;

        hpContainer.removeAll();
        double currentLives = player.getLives();
        int totalHalfUnits = (currentLives <= 0.001) ? 0 : (int) Math.round(currentLives * 2);
        
        for (int i = 0; i < 3; i++) { 
            ImageIcon icon = null;
            int currentSlotHalfUnits = totalHalfUnits - (i * 2);
            if (currentSlotHalfUnits >= 2) icon = icons.get("heart_full");
            else if (currentSlotHalfUnits == 1) icon = icons.get("heart_half");
            else icon = icons.get("heart_empty");
            if (icon != null) hpContainer.add(new JLabel(icon));
        }
    }

    // --- MAIN REDRAW METHOD (VISUAL UPGRADE) ---
    public static void redrawArena(JFrame frame, Arena arena, List<Character> cycles, Map<String, ImageIcon> icons, JPanel arenaPanel, JPanel sidebar) {
        
        arenaPanel.removeAll(); 
        char[][] grid = arena.getGrid();
        
        // Define Colors
        Color NEON_BG = new Color(10, 10, 20);      
        Color GRID_LINE = new Color(30, 40, 60);    
        Color WALL_COLOR = new Color(0, 50, 80);    
        Color WALL_BORDER = new Color(0, 200, 255); 
        
        for (int r = 0; r < 40; r++) {
            for (int c = 0; c < 40; c++) {

                JLabel cell = new JLabel();
                cell.setOpaque(true);
                cell.setHorizontalAlignment(JLabel.CENTER);
                cell.setVerticalAlignment(JLabel.CENTER);
                
                // Default Grid Style
                cell.setBackground(NEON_BG);
                cell.setBorder(BorderFactory.createLineBorder(GRID_LINE, 1));
                
                ImageIcon cycleIconToDraw = null;
                
                // Find Player Logic
                for (Character cycle : cycles) {
                    if (cycle.getRow() == r && cycle.getCol() == c) {
                        String iconKey = cycle.name.toLowerCase() + "_" + cycle.currentDirection.toString();
                        cycleIconToDraw = icons.get(iconKey);
                        
                        // DEBUG: If icon is missing, print why
                        if (cycleIconToDraw == null && cycle.name.equals("Tron")) {
                            System.out.println("⚠ WARNING: Icon missing for key: " + iconKey);
                        }
                        break;
                    }
                }

                if (cycleIconToDraw != null) {
                    // --- PLAYER IS HERE ---
                    cell.setIcon(cycleIconToDraw);

                    // 1. Backlight (Lighter Blue to make it pop)
                    cell.setBackground(new Color(0, 100, 180)); 

                    // 2. Thinner Double Border (Prevent Icon Squeezing)
                    // Previous code was too thick (6px total). New code is 2px/3px total.
                    if (grid[r][c] == 'S') {
                        // Turbo Mode: Magenta (2px) + White (1px)
                        javax.swing.border.Border outer = BorderFactory.createLineBorder(Color.MAGENTA, 2);
                        javax.swing.border.Border inner = BorderFactory.createLineBorder(Color.WHITE, 1);
                        cell.setBorder(BorderFactory.createCompoundBorder(outer, inner));
                    } else {
                        // Normal Mode: Cyan (2px) + White (1px)
                        javax.swing.border.Border outer = BorderFactory.createLineBorder(Color.CYAN, 2);
                        javax.swing.border.Border inner = BorderFactory.createLineBorder(Color.WHITE, 1);
                        cell.setBorder(BorderFactory.createCompoundBorder(outer, inner));
                    }

                } else {
                    // --- STATIC ELEMENTS ---
                    switch (grid[r][c]) {
                        case '#': // WALL
                            cell.setBackground(WALL_COLOR);
                            cell.setBorder(BorderFactory.createLineBorder(WALL_BORDER, 1));
                            break;
                        case 'O': // OBSTACLE
                            cell.setBackground(NEON_BG); 
                            if (icons.get("obstacle") != null) cell.setIcon(icons.get("obstacle"));
                            else cell.setBackground(new Color(255, 140, 0)); 
                            break;
                        case 'S': // SPEED RAMP
                            cell.setBackground(NEON_BG);
                            if (icons.get("speed") != null) cell.setIcon(icons.get("speed"));
                            else cell.setBackground(Color.CYAN); 
                            break;
                        case 'T': // TRON TRAIL
                            cell.setBackground(new Color(0, 180, 255)); 
                            cell.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                            break;
                        case 'K': // KEVIN TRAIL
                            cell.setBackground(new Color(255, 100, 0));
                            cell.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                            break;
                    }
                }
                arenaPanel.add(cell);
            }
        }

        // Sidebar Flash Logic
        if (cycles.get(0).isStunned) {
            sidebar.setBackground(new Color(150, 0, 0)); 
        } else {
            sidebar.setBackground(new Color(5, 10, 20)); 
        }

        updateHUD(sidebar, cycles.get(0), icons);
        
        arenaPanel.revalidate(); 
        arenaPanel.repaint();
    }

    public static void showGameOverDialog(JFrame parentFrame) {
        JDialog gameOverDialog = new JDialog(parentFrame, "Game Over", true);
        gameOverDialog.setSize(500, 250); // Made it slightly wider
        gameOverDialog.setLayout(new BorderLayout());
        gameOverDialog.setLocationRelativeTo(parentFrame);
        
        // Dark Purple/Black Background
        Color DIALOG_BG = new Color(15, 0, 40);
        gameOverDialog.getContentPane().setBackground(DIALOG_BG); 
        
        // --- TITLE ---
        JLabel title = new JLabel("::: DEREZZED :::", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 42)); // Bigger font
        title.setForeground(Color.RED); 
        title.setBorder(new EmptyBorder(30, 0, 0, 0)); // Add spacing on top
        gameOverDialog.add(title, BorderLayout.NORTH);

        // --- SUBTITLE (Optional) ---
        JLabel subtitle = new JLabel("END OF LINE.", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 16));
        subtitle.setForeground(Color.GRAY);
        gameOverDialog.add(subtitle, BorderLayout.CENTER);

        // --- STYLED BUTTON ---
        JButton closeButton = new JButton("EXIT GRID");
        
        // 1. FONT: Match the game style
        closeButton.setFont(new Font("Monospaced", Font.BOLD, 20));
        
        // 2. COLORS: Black background, Neon Cyan text
        closeButton.setBackground(Color.BLACK);
        closeButton.setForeground(Color.CYAN);
        
        // 3. BORDER: Neon Line Border + Internal Padding
        closeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.CYAN, 2), // Outer Neon Line
            BorderFactory.createEmptyBorder(10, 30, 10, 30) // Inner Padding (Top, Left, Bot, Right)
        ));
        
        // 4. REMOVE JUNK: Get rid of the default "clicked" focus box
        closeButton.setFocusPainted(false);
        
        // 5. INTERACTION: Simple Hover Effect
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(Color.CYAN);
                closeButton.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(Color.BLACK);
                closeButton.setForeground(Color.CYAN);
            }
        });
        
        closeButton.addActionListener(e -> System.exit(0)); 
        
        // Put button in a panel so it doesn't stretch to fill the whole bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(DIALOG_BG); // Match dialog background
        buttonPanel.setBorder(new EmptyBorder(0, 0, 30, 0)); // Spacing at bottom
        buttonPanel.add(closeButton);
        
        gameOverDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        gameOverDialog.setVisible(true);
    }

    public static void main(String[] args) {
        Arena arena = loadArena(1); 
        JFrame frame = new JFrame("Tron Legacy: Grid Arena");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setLayout(new BorderLayout()); 
        
        Map<String, ImageIcon> icons = loadAllIcons(frame);
        
        CharacterData tronData = CharacterLoader.loadCharacterData("Tron");
        CharacterData kevinData = CharacterLoader.loadCharacterData("Kevin");
        Tron tron = new Tron();
        Kevin kevin = new Kevin();
        if (tronData != null) tron.loadInitialAttributes(tronData);
        if (kevinData != null) kevin.loadInitialAttributes(kevinData);
        tron.r = 20; tron.c = 15; tron.currentDirection = Direction.EAST;
        List<Character> cycles = List.of(tron, kevin);

        JPanel arenaPanel = new JPanel(new GridLayout(40, 40));
        arenaPanel.setBackground(new Color(10, 10, 20)); 
        
        JPanel sidebarPanel = createSidebarPanel(tron, icons);
        
        frame.add(arenaPanel, BorderLayout.CENTER);
        frame.add(sidebarPanel, BorderLayout.EAST);
        
        redrawArena(frame, arena, cycles, icons, arenaPanel, sidebarPanel);
        frame.setVisible(true);

        StartGameMenu.showMenu(frame);
        
        GameController controller = new GameController(frame, arena, cycles, icons, arenaPanel, sidebarPanel);
        new Thread(controller).start();
    }
}