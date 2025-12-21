package arena;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

// Imports
import characters.Character;
import characters.Tron;
import characters.Kevin;
import characters.CharacterLoader;
import characters.CharacterData;
import characters.Direction;
import controller.GameController;
import UI.StartGameMenu;
import UI.CutsceneManager;
import arena.CutsceneUtil;
import designenemies.*;
import XPSystem.TronRules; // Math for XP

public class ArenaLoader {

    public static int currentChapter = 2;
    public static int currentStage = 5;
    public static JFrame mainFrame;
    // Track the last pre-stage cutscene that was shown to avoid showing it twice
    private static int lastPreCutsceneChapter = -1;
    private static int lastPreCutsceneStage = -1;

    // Mark that the pre-stage cutscene for a given chapter/stage was shown
    public static void markPreCutsceneShown(int chapter, int stage) {
        lastPreCutsceneChapter = chapter;
        lastPreCutsceneStage = stage;
    }

    private static GameController activeController;
    private static Thread gameThread;
    private static final Object startLock = new Object();
    private static boolean levelStarting = false;

    // --- PERSISTENT CHARACTERS (So Level doesn't reset!) ---
    private static Tron persistentTron;
    private static Kevin persistentKevin;
    // Convenience reference to whichever character the player selected
    private static Character persistentPlayer;

    /**
     * Set the currently selected player by name ("Tron" or "Kevin"). This
     * ensures persistent instances are created and used for the session.
     */
    public static void setSelectedPlayer(String name) {
        if ("Kevin".equalsIgnoreCase(name)) {
            if (persistentKevin == null) {
                persistentKevin = new Kevin();
                CharacterData data = CharacterLoader.loadCharacterData("Kevin");
                if (data != null) persistentKevin.loadInitialAttributes(data);
                // Try to load saved XP for Kevin if user logged in
                try {
                    if (mainFrame instanceof UI.MainFrame) {
                        String user = ((UI.MainFrame) mainFrame).getCurrentUsername();
                        if (user != null && !user.trim().isEmpty()) {
                            UI.DatabaseManager db = new UI.DatabaseManager();
                            long savedXp = db.getKevinXp(user);
                            if (savedXp > 0) {
                                persistentKevin.setXp(savedXp);
                                System.out.println("Loaded saved Kevin XP for " + user + ": " + savedXp);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
            persistentPlayer = persistentKevin;
        } else {
            if (persistentTron == null) {
                persistentTron = new Tron();
                CharacterData data = CharacterLoader.loadCharacterData("Tron");
                if (data != null) persistentTron.loadInitialAttributes(data);
                // Try to load saved XP for Tron if user logged in
                try {
                    if (mainFrame instanceof UI.MainFrame) {
                        String user = ((UI.MainFrame) mainFrame).getCurrentUsername();
                        if (user != null && !user.trim().isEmpty()) {
                            UI.DatabaseManager db = new UI.DatabaseManager();
                            long savedXp = db.getTronXp(user);
                            if (savedXp > 0) {
                                persistentTron.setXp(savedXp);
                                System.out.println("Loaded saved Tron XP for " + user + ": " + savedXp);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
            persistentPlayer = persistentTron;
        }
    }

    // --- HELPER METHODS (Images) ---
    private static BufferedImage toBufferedImage(Image img) { if (img instanceof BufferedImage) return (BufferedImage) img; BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB); Graphics2D bGr = bimage.createGraphics(); bGr.drawImage(img, 0, 0, null); bGr.dispose(); return bimage; }
    private static BufferedImage rotateImage(BufferedImage image, double angleRadians) { double sin = Math.abs(Math.sin(angleRadians)); double cos = Math.abs(Math.cos(angleRadians)); int width = image.getWidth(); int height = image.getHeight(); int newWidth = (int) Math.floor(width * cos + height * sin); int newHeight = (int) Math.floor(height * cos + width * sin); BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType()); Graphics2D g2d = rotatedImage.createGraphics(); AffineTransform at = new AffineTransform(); at.translate(newWidth / 2, newHeight / 2); at.rotate(angleRadians); at.translate(-width / 2, -height / 2); g2d.drawImage(image, at, null); g2d.dispose(); return rotatedImage; }

    private static ImageIcon loadAndScale(String relativePath, int size) { 
        try { File imageFile = new File(System.getProperty("user.dir") + File.separator + relativePath);
            if (!imageFile.exists()) return null;
            ImageIcon original = new ImageIcon(imageFile.getAbsolutePath()); 
            if (original.getImageLoadStatus() == MediaTracker.COMPLETE) { 
                Image srcImg = original.getImage();
                int origW = srcImg.getWidth(null);
                int origH = srcImg.getHeight(null);
                int newW = size;
                int newH = size;

                if (origW > origH) {
                    newH = (int) (size * ((double) origH / origW));
                } else {
                    newW = (int) (size * ((double) origW / origH));
                }

                // Create a transparent square canvas and center the image
                BufferedImage combined = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = combined.createGraphics();
                
                // High quality rendering hints
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = (size - newW) / 2;
                int y = (size - newH) / 2;
                g.drawImage(srcImg, x, y, newW, newH, null);
                g.dispose();

                return new ImageIcon(combined);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static Arena loadArena(int choice) {
        try {
            switch (currentChapter) {
                case 1: return new ArenaOne();
                case 2: return new ArenaTwo();
                case 3: return new ArenaThree();
                case 4: case 5: default: return new RandomArena();
            }
        } catch (Exception ex) {
            System.err.println("[ArenaLoader] Failed to load arena for chapter " + currentChapter + ": " + ex.getMessage());
            ex.printStackTrace();
            // Fallback: return RandomArena to avoid crashing
            try { return new RandomArena(); } catch (Exception e) { throw new RuntimeException("No arena available", e); }
        }
    }

    private static Map<String, ImageIcon> loadAllIcons(JFrame frame) {
        Map<String, ImageIcon> icons = new HashMap<>();
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int TRUE_CELL_SIZE = (int) ((screenHeight*2) / 40);
        int FACE_SIZE = (int) (screenHeight / 45);
        final int HUD_ICON_SIZE = 60;
        final int DISC_INVENTORY_SIZE = 50;

        icons.put("obstacle", loadAndScale("images" + File.separator + "obstacle.png", TRUE_CELL_SIZE));
        icons.put("speed", loadAndScale("images" + File.separator + "Speed.png", TRUE_CELL_SIZE));
        icons.put("disc", loadAndScale("images" + File.separator + "disc.png", 25));
        icons.put("disc_inventory", loadAndScale("images" + File.separator + "disc.png", DISC_INVENTORY_SIZE));
        icons.put("heart_full", loadAndScale("images" + File.separator + "heart_full.png", HUD_ICON_SIZE));
        icons.put("heart_half", loadAndScale("images" + File.separator + "heart_half.png", HUD_ICON_SIZE));
        icons.put("heart_empty", loadAndScale("images" + File.separator + "heart_empty.png", HUD_ICON_SIZE));
        icons.put("tron_profile", loadAndScale("images" + File.separator + "Tron.png", 200));
        icons.put("kevin_profile", loadAndScale("images" + File.separator + "Kevin.png", 200));

        loadCharSet(icons, "tron", "Tron.png", TRUE_CELL_SIZE);
        loadCharSet(icons, "kevin", "kevin.png", 150);
        loadCharSet(icons, "sark", "Sark (Face).png", FACE_SIZE);
        loadCharSet(icons, "clu", "Clu (Face).png", FACE_SIZE);
        loadCharSet(icons, "rinzler", "Rinzler (Face).png", FACE_SIZE);
        loadCharSet(icons, "koura", "Koura (Face).png", FACE_SIZE);
        return icons;
    }

    private static void loadCharSet(Map<String, ImageIcon> icons, String prefix, String filename, int size) {
        ImageIcon base = loadAndScale("images" + File.separator + filename, size);
        if (base != null) {
            BufferedImage img = toBufferedImage(base.getImage());
            icons.put(prefix + "_NORTH", base);
            icons.put(prefix + "_EAST", new ImageIcon(rotateImage(img, Math.toRadians(90))));
            icons.put(prefix + "_SOUTH", new ImageIcon(rotateImage(img, Math.toRadians(180))));
            icons.put(prefix + "_WEST", new ImageIcon(rotateImage(img, Math.toRadians(270))));
        }
    }

    // --- UPDATED SIDEBAR (4 Sections: Profile, Chapter/Stage, Hearts, Discs) ---
    public static JPanel createSidebarPanel(Character player, Map<String, ImageIcon> icons) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(5, 10, 20));
        sidebar.setPreferredSize(new Dimension(350, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0, 255, 255)));

        // ========== SECTION 1: PROFILE ==========
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setOpaque(false);
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        profilePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;

        // Icon: choose a profile icon based on the current player's name
        JLabel profilePic = new JLabel();
        String profileKey = player.name.toLowerCase() + "_profile";
        ImageIcon profileIcon = icons.getOrDefault(profileKey, icons.get("tron_profile"));
        profilePic.setIcon(profileIcon);
        profilePic.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        profilePanel.add(profilePic, gbc);

        // Name
        gbc.gridy = 1;
        JLabel nameLabel = new JLabel(player.name.toUpperCase());
        nameLabel.setForeground(Color.CYAN);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        nameLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        profilePanel.add(nameLabel, gbc);

        // Level
        gbc.gridy = 2;
        JLabel levelLabel = new JLabel("LEVEL " + player.getLevel());
        levelLabel.setName("LevelLabel");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        profilePanel.add(levelLabel, gbc);

        // XP
        gbc.gridy = 3;
        JLabel xpLabel = new JLabel("XP: 0 / 100");
        xpLabel.setName("XPLabel");
        xpLabel.setForeground(Color.LIGHT_GRAY);
        xpLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        profilePanel.add(xpLabel, gbc);
        
        // Speed
        gbc.gridy = 4;
        JLabel speedLabel = new JLabel("SPD: " + String.format("%.2f", player.getSpeed()));
        speedLabel.setName("SpeedLabel");
        speedLabel.setForeground(new Color(0, 255, 100));
        speedLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        profilePanel.add(speedLabel, gbc);
        
        // Handling (Precision)
        gbc.gridy = 5;
        JLabel handlingLabel = new JLabel("PRE: " + String.format("%.2f", player.getHandling()));
        handlingLabel.setName("HandlingLabel");
        handlingLabel.setForeground(new Color(100, 200, 255));
        handlingLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        profilePanel.add(handlingLabel, gbc);
        
        sidebar.add(profilePanel);
        sidebar.add(Box.createVerticalStrut(10));

        // ========== SECTION 2: CHAPTER & STAGE ==========
        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel chLabel = new JLabel("CHAPTER: " + currentChapter);
        chLabel.setForeground(Color.YELLOW);
        chLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        chLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel stgLabel = new JLabel("STAGE: " + currentStage);
        stgLabel.setForeground(Color.YELLOW);
        stgLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
        stgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.add(chLabel);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(stgLabel);
        
        sidebar.add(statsPanel);
        sidebar.add(Box.createVerticalStrut(10));

        // ========== SECTION 3: LIFE POWER (HEARTS) ==========
        JPanel heartsSection = new JPanel();
        heartsSection.setOpaque(false);
        heartsSection.setLayout(new BoxLayout(heartsSection, BoxLayout.Y_AXIS));
        heartsSection.setBorder(new EmptyBorder(10, 15, 10, 15));
        heartsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        heartsSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel hpTitle = new JLabel("LIFE POWER");
        hpTitle.setForeground(new Color(255, 100, 100));
        hpTitle.setFont(new Font("Monospaced", Font.BOLD, 16));
        hpTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel hpContainer = new JPanel(new GridLayout(0, 6, 5, 5));
        hpContainer.setName("HPContainer");
        hpContainer.setOpaque(false);
        hpContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        heartsSection.add(hpTitle);
        heartsSection.add(Box.createVerticalStrut(8));
        heartsSection.add(hpContainer);
        
        sidebar.add(heartsSection);
        sidebar.add(Box.createVerticalStrut(10));

        // ========== SECTION 4: DISC INVENTORY ==========
        JPanel discsSection = new JPanel();
        discsSection.setOpaque(false);
        discsSection.setLayout(new BoxLayout(discsSection, BoxLayout.Y_AXIS));
        discsSection.setBorder(new EmptyBorder(10, 15, 10, 15));
        discsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        discsSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel discTitle = new JLabel("DISC INVENTORY");
        discTitle.setForeground(new Color(0, 200, 255));
        discTitle.setFont(new Font("Monospaced", Font.BOLD, 16));
        discTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel discSlotsContainer = new JPanel(new GridLayout(0, 4, 8, 8));
        discSlotsContainer.setName("DiscSlotsContainer");
        discSlotsContainer.setOpaque(false);
        discSlotsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        discsSection.add(discTitle);
        discsSection.add(Box.createVerticalStrut(8));
        discsSection.add(discSlotsContainer);
        
        sidebar.add(discsSection);
        sidebar.add(Box.createVerticalGlue()); // Push everything to top

        return sidebar;
    }

    // --- UPDATED HUD LOGIC ---
    public static void updateHUD(JPanel sidebar, Character player, Map<String, ImageIcon> icons) {
        JPanel hpContainer = null;
        JPanel discSlotsContainer = null;
        JLabel levelLabel = null;
        JLabel xpLabel = null;
        JLabel discLabel = null;
        JLabel speedLabel = null;
        JLabel handlingLabel = null;

        // Deep recursive search for components through all nested panels (3 levels deep)
        for (Component row : sidebar.getComponents()) {
            if (row instanceof JPanel) {
                JPanel p = (JPanel) row;
                // Level 1: Check direct children
                if ("HPContainer".equals(p.getName())) hpContainer = p;
                if ("DiscSlotsContainer".equals(p.getName())) discSlotsContainer = p;
                
                // Level 2: Check nested children
                for (Component sub : p.getComponents()) {
                    if (sub instanceof JPanel) {
                        JPanel subPanel = (JPanel) sub;
                        if ("HPContainer".equals(subPanel.getName())) hpContainer = subPanel;
                        if ("DiscSlotsContainer".equals(subPanel.getName())) discSlotsContainer = subPanel;
                        
                        // Level 3: Check deeper nested children
                        for (Component subsub : subPanel.getComponents()) {
                            if (subsub instanceof JPanel) {
                                JPanel subsubPanel = (JPanel) subsub;
                                if ("HPContainer".equals(subsubPanel.getName())) hpContainer = subsubPanel;
                                if ("DiscSlotsContainer".equals(subsubPanel.getName())) discSlotsContainer = subsubPanel;
                            }
                        }
                    }
                    if (sub instanceof JLabel) {
                        JLabel subLabel = (JLabel) sub;
                        if ("LevelLabel".equals(subLabel.getName())) levelLabel = subLabel;
                        if ("XPLabel".equals(subLabel.getName())) xpLabel = subLabel;
                        if ("DiscLabel".equals(subLabel.getName())) discLabel = subLabel;
                        if ("SpeedLabel".equals(subLabel.getName())) speedLabel = subLabel;
                        if ("HandlingLabel".equals(subLabel.getName())) handlingLabel = subLabel;
                    }
                }
            }
        }

        // 1. UPDATE DISC SLOTS - Minecraft style inventory with disc icons
        if (discSlotsContainer != null) {
            discSlotsContainer.removeAll();
            int maxDiscs = Math.max(player.getDiscCapacity(), 1); // Show at least 1 slot, based on actual disc capacity
            int currentDiscs = player.currentDiscCount; // Current discs available
            
            for (int i = 0; i < maxDiscs; i++) {
                JLabel slot = new JLabel();
                slot.setHorizontalAlignment(JLabel.CENTER);
                slot.setVerticalAlignment(JLabel.CENTER);
                slot.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120), 2));
                slot.setPreferredSize(new Dimension(60, 60));
                slot.setOpaque(true);
                slot.setBackground(new Color(30, 30, 50));
                
                if (i < currentDiscs) {
                    // Filled slot - show disc icon
                    ImageIcon discIcon = icons.get("disc_inventory");
                    if (discIcon != null) {
                        slot.setIcon(discIcon);
                    } else {
                        // Fallback if no disc icon
                        slot.setText("●"); 
                        slot.setFont(new Font("Arial", Font.BOLD, 24));
                        slot.setForeground(new Color(0, 200, 255));
                    }
                } else {
                    // Empty slot - dark background only
                    slot.setText("");
                }
                discSlotsContainer.add(slot);
            }
            discSlotsContainer.revalidate();
            discSlotsContainer.repaint();
        }

        // 2. UPDATE HEARTS - Dynamic based on maxLives (Prefer saved max-level stats if user is logged-in)
        double displayMaxLives = player.getMaxLives();
        double displayCurrentLives = player.getLives();
        int displayDiscCap = player.getDiscCapacity();
        double displaySpeed = player.getSpeed();
        double displayHandling = player.getHandling();
        int displayLevel = player.getLevel();

        // Use cached persistent character stats to avoid synchronous DB reads on the EDT.
        // persistentTron / persistentKevin are loaded at arena start when the user is logged in.
        if (player.name.equals("Tron") && persistentTron != null) {
            int savedLevel = persistentTron.getLevel();
            displayLevel = Math.max(displayLevel, savedLevel);
            displaySpeed = persistentTron.getSpeed();
            displayHandling = persistentTron.getHandling();
            displayDiscCap = persistentTron.getDiscCapacity();
            displayMaxLives = persistentTron.getMaxLives();
            if (displayCurrentLives > displayMaxLives) displayCurrentLives = displayMaxLives;
        } else if (player.name.equals("Kevin") && persistentKevin != null) {
            int savedLevel = persistentKevin.getLevel();
            displayLevel = Math.max(displayLevel, savedLevel);
            displaySpeed = persistentKevin.getSpeed();
            displayHandling = persistentKevin.getHandling();
            displayDiscCap = persistentKevin.getDiscCapacity();
            displayMaxLives = persistentKevin.getMaxLives();
            if (displayCurrentLives > displayMaxLives) displayCurrentLives = displayMaxLives;
        }

        if (hpContainer != null) {
            hpContainer.removeAll();
            double currentLives = displayCurrentLives;
            double maxLives = displayMaxLives;
            int totalHalfUnits = (currentLives <= 0.001) ? 0 : (int) Math.round(currentLives * 2);
            int maxHalfUnits = (int) Math.round(maxLives * 2);
            int maxHearts = (maxHalfUnits + 1) / 2; // Round up: 2 half-units = 1 heart
            
            for (int i = 0; i < maxHearts; i++) {
                ImageIcon icon = null;
                int currentSlotHalfUnits = totalHalfUnits - (i * 2);
                if (currentSlotHalfUnits >= 2) icon = icons.get("heart_full");
                else if (currentSlotHalfUnits == 1) icon = icons.get("heart_half");
                else icon = icons.get("heart_empty");
                if (icon != null) hpContainer.add(new JLabel(icon));
            }
            hpContainer.revalidate();
            hpContainer.repaint();
        }

        // 3. UPDATE LEVEL & XP
        if (levelLabel != null) {
            levelLabel.setText("LEVEL " + displayLevel);
        }
        if (xpLabel != null) {
            long currentXP = player.getXp();
            long nextXP = TronRules.getTotalXpForLevel(displayLevel + 1);
            xpLabel.setText("XP: " + currentXP + " / " + nextXP);
        }
        
        // 4. UPDATE SPEED & HANDLING
        if (speedLabel != null) {
            speedLabel.setText("SPD: " + String.format("%.2f", displaySpeed));
        }
        if (handlingLabel != null) {
            handlingLabel.setText("PRE: " + String.format("%.2f", displayHandling));
        }
        
        // 5. UPDATE DISC LABEL
        if (discLabel != null) {
            discLabel.setText("DISCS: " + displayDiscCap);
        }
    }

    public static void redrawArena(JFrame frame, Arena arena, List<Character> cycles, Map<String, ImageIcon> icons, JPanel arenaPanel, JPanel sidebar) {
        arenaPanel.removeAll();
        char[][] grid = arena.getGrid();
        Color NEON_BG = new Color(10, 10, 20); Color GRID_LINE = new Color(30, 40, 60);
        Color WALL_COLOR = new Color(0, 50, 80); Color WALL_BORDER = new Color(0, 200, 255);
        
        // Check if player is on speed ramp for booster effect
        int playerR = cycles.get(0).getRow();
        int playerC = cycles.get(0).getCol();
        boolean playerBoosting = playerR >= 0 && playerR < 40 && playerC >= 0 && playerC < 40 && grid[playerR][playerC] == 'S';

        for (int r = 0; r < 40; r++) { for (int c = 0; c < 40; c++) {
                JPanel cellPanel = new JPanel(new BorderLayout());
                cellPanel.setOpaque(true); cellPanel.setBackground(NEON_BG);
                cellPanel.setBorder(BorderFactory.createLineBorder(GRID_LINE, 1));
                JLabel iconLabel = new JLabel();
                iconLabel.setHorizontalAlignment(JLabel.CENTER); iconLabel.setVerticalAlignment(JLabel.CENTER);
                cellPanel.add(iconLabel, BorderLayout.CENTER);

                Character characterHere = null;
                for (Character cycle : cycles) { if (cycle.getRow() == r && cycle.getCol() == c) { characterHere = cycle; break; } }

                if (characterHere != null) {
                    String iconKey = characterHere.imageBaseName + "_" + characterHere.currentDirection.toString();
                    if (icons.containsKey(iconKey)) iconLabel.setIcon(icons.get(iconKey));
                    else iconLabel.setText("?");

                    if (characterHere.name.equals("Tron")) { 
                        cellPanel.setBackground(new Color(0, 100, 180)); 
                        cellPanel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2)); 
                    }
                    else { 
                        cellPanel.setBackground(new Color(100, 40, 0)); 
                        // Boss enemies get special visual indicator (bright red border + purple background)
                        if (characterHere instanceof designenemies.Enemy) {
                            designenemies.Enemy enemy = (designenemies.Enemy) characterHere;
                            if (enemy.isBoss()) {
                                cellPanel.setBackground(new Color(150, 0, 50)); // Dark purple/red
                                cellPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3)); // Thick magenta border
                               
                            } else {
                                cellPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                            }
                        } else {
                            cellPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                        }
                    }

                    // Only show an HP bar for enemies (player doesn't need it visible)
                    if (characterHere instanceof designenemies.Enemy) {
                        double hpPercent = characterHere.getLives() / characterHere.getMaxLives();
                        hpPercent = Math.max(0, Math.min(1, hpPercent));
                        JProgressBar hpBar = new JProgressBar(0, 100);
                        hpBar.setValue((int)(hpPercent * 100)); hpBar.setPreferredSize(new Dimension(0, 5));
                        hpBar.setForeground(hpPercent > 0.5 ? Color.GREEN : Color.RED); hpBar.setBackground(Color.BLACK); hpBar.setBorderPainted(false);
                        cellPanel.add(hpBar, BorderLayout.SOUTH);
                    }
                } else {
                    switch (grid[r][c]) {
                        case '#': cellPanel.setBackground(WALL_COLOR); cellPanel.setBorder(BorderFactory.createLineBorder(WALL_BORDER, 1)); break;
                        case 'O': cellPanel.setBackground(NEON_BG); if (icons.get("obstacle") != null) iconLabel.setIcon(icons.get("obstacle")); else cellPanel.setBackground(new Color(255, 140, 0)); break;
                        case 'S': cellPanel.setBackground(NEON_BG); if (icons.get("speed") != null) iconLabel.setIcon(icons.get("speed")); else cellPanel.setBackground(Color.CYAN); break;
                        case 'D': cellPanel.setBackground(new Color(0, 30, 50)); if (icons.get("disc") != null) iconLabel.setIcon(icons.get("disc")); else { iconLabel.setText("O"); iconLabel.setForeground(Color.CYAN); } cellPanel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1)); break;
                        case 'T': 
                            // BOOSTER EFFECT: Tron tail glows bright when boosting
                            if (playerBoosting) {
                                cellPanel.setBackground(new Color(0, 255, 200)); // Bright neon cyan
                                cellPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2)); // Yellow glow border
                            } else {
                                cellPanel.setBackground(new Color(0, 180, 255)); 
                                cellPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1)); 
                            }
                            break;
                        case 'K':
                            // Kevin's tail (white)
                            if (playerBoosting) {
                                cellPanel.setBackground(Color.WHITE);
                                cellPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                            } else {
                                cellPanel.setBackground(Color.WHITE);
                                cellPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                            }
                            break;
                        // --- NEW COLORED TRAILS ---
                        case 'C': // Clu (orange for stronger contrast)
                            cellPanel.setBackground(new Color(255, 140, 0)); // bright orange
                            cellPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 100, 0), 2)); 
                            break;
                        case 'Y': // Sark (vivid yellow)
                            cellPanel.setBackground(new Color(255, 230, 0)); // vivid yellow
                            cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); 
                            break;
                        case 'G': // Koura (Green)
                            cellPanel.setBackground(new Color(50, 205, 50)); 
                            cellPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0), 1)); 
                            break;
                        case 'R': // Rinzler (Red)
                            cellPanel.setBackground(new Color(220, 20, 60)); 
                            cellPanel.setBorder(BorderFactory.createLineBorder(new Color(100, 0, 0), 1)); 
                            break;
                        case 'M': cellPanel.setBackground(new Color(255, 50, 0)); cellPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1)); break;
                    }
                }
                arenaPanel.add(cellPanel);
        }}
        if (cycles.get(0).isStunned) sidebar.setBackground(new Color(150, 0, 0)); else sidebar.setBackground(new Color(5, 10, 20));
        updateHUD(sidebar, cycles.get(0), icons);
        arenaPanel.revalidate(); arenaPanel.repaint();
    }

    public static void showGameOverDialog(JFrame parentFrame) {
        // Present a friendly choice to the player: try the level again or quit
        JLabel title = new JLabel("::: DEREZZED :::", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 42));
        title.setForeground(Color.RED);
        title.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel message = new JLabel("<html><div style='text-align:center;'>You have lost this stage.<br/>Would you like to try again?</div></html>", SwingConstants.CENTER);
        message.setFont(new Font("Monospaced", Font.PLAIN, 16));
        message.setBorder(new EmptyBorder(10, 10, 10, 10));

        Object[] options = {"Try Again", "Quit"};
        int choice = JOptionPane.showOptionDialog(parentFrame, new Object[]{title, message}, "Game Over",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            // Restart the current stage
            startLevel();
        } else {
            System.exit(0);
        }
    }

    public static void startLevel() {
        System.out.println("[ArenaLoader] startLevel() called");
        synchronized (startLock) {
            if (levelStarting) {
                System.out.println("[ArenaLoader] startLevel already in progress, ignoring duplicate call.");
                return;
            }
            levelStarting = true;
        }

        try {
            // ALWAYS clean up old listeners first, regardless of thread state
            if (mainFrame != null) {
                java.awt.event.KeyListener[] listeners = mainFrame.getKeyListeners();
                for (java.awt.event.KeyListener kl : listeners) {
                    mainFrame.removeKeyListener(kl);
                }
            }

            // Stop old thread if still running. DO NOT block the EDT — defer start until previous thread exits.
            if (gameThread != null && gameThread.isAlive()) {
                System.out.println("[ArenaLoader] Previous game thread is still alive; requesting stop and will restart after it finishes.");
                activeController.stopGame();
                Thread stopper = new Thread(() -> {
                    try {
                        gameThread.join(3000);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                    System.out.println("[ArenaLoader] Previous game thread stopped; resuming startLevel on EDT.");
                    SwingUtilities.invokeLater(() -> startLevel());
                }, "ArenaLoader-Starter");
                stopper.setDaemon(true);
                stopper.start();
                // Allow future startLevel calls when the stopper re-invokes startLevel
                synchronized (startLock) { levelStarting = false; }
                return;
            }
            activeController = null;
            mainFrame.getContentPane().removeAll();

            // Select arena based on the current chapter so transitions load correct map
            Arena arena = loadArena(currentChapter);
            Map<String, ImageIcon> icons = loadAllIcons(mainFrame);

            // --- PERSISTENCE LOGIC ---
            // Ensure we have a selected persistent player (default to Tron)
            if (persistentPlayer == null) {
                setSelectedPlayer("Tron");
            }

            // If the user is logged-in, ensure we load saved XP (in case setSelectedPlayer was called
            // before the mainFrame/username was available). We do this once per startLevel to be safe.
            try {
                if (mainFrame instanceof UI.MainFrame) {
                    String user = ((UI.MainFrame) mainFrame).getCurrentUsername();
                    if (user != null && !user.trim().isEmpty()) {
                        UI.DatabaseManager db = new UI.DatabaseManager();
                        // Load and reconcile saved level and XP so we don't show mismatched state
                        int savedTronLevel = db.getTronLevel(user);
                        long savedTronXp = db.getTronXp(user);
                        if (savedTronLevel > 0) {
                            // Ensure saved XP is at least the min total XP for that saved level
                            long minXpForLevel = XPSystem.TronRules.getTotalXpForLevel(savedTronLevel);
                            long originalXp = savedTronXp;
                            if (savedTronXp < minXpForLevel) savedTronXp = minXpForLevel;
                            if (persistentTron != null) { persistentTron.setXp(savedTronXp); System.out.println("[ArenaLoader] Reconciled and loaded Tron level/XP for " + user + ": L" + savedTronLevel + ", XP=" + savedTronXp); }
                            if (originalXp < savedTronXp) {
                                final long newXp = savedTronXp;
                                final long oldXp = originalXp;
                                new Thread(() -> {
                                    try {
                                        db.setTronXp(user, newXp);
                                        System.out.println("[DB FIX] Fixed TRON_XP for user=" + user + ": " + oldXp + " -> " + newXp);
                                    } catch (Exception e) {
                                        System.out.println("[DB FIX] Failed to fix TRON_XP for user=" + user + ": " + e.getMessage());
                                    }
                                }, "DBFix-TronXp").start();
                            }
                        } else {
                            long saved = db.getTronXp(user);
                            if (saved > 0 && persistentTron != null) { persistentTron.setXp(saved); System.out.println("[ArenaLoader] Loaded Tron XP for " + user + ": " + saved); }
                        }

                        int savedKevinLevel = db.getKevinLevel(user);
                        long savedKevinXp = db.getKevinXp(user);
                        if (savedKevinLevel > 0) {
                            long minXpForLevel = XPSystem.TronRules.getTotalXpForLevel(savedKevinLevel);
                            long originalKevinXp = savedKevinXp;
                            if (savedKevinXp < minXpForLevel) savedKevinXp = minXpForLevel;
                            if (persistentKevin != null) { persistentKevin.setXp(savedKevinXp); System.out.println("[ArenaLoader] Reconciled and loaded Kevin level/XP for " + user + ": L" + savedKevinLevel + ", XP=" + savedKevinXp); }
                            if (originalKevinXp < savedKevinXp) {
                                final long newXp = savedKevinXp;
                                final long oldXp = originalKevinXp;
                                new Thread(() -> {
                                    try {
                                        db.setKevinXp(user, newXp);
                                        System.out.println("[DB FIX] Fixed KEVIN_XP for user=" + user + ": " + oldXp + " -> " + newXp);
                                    } catch (Exception e) {
                                        System.out.println("[DB FIX] Failed to fix KEVIN_XP for user=" + user + ": " + e.getMessage());
                                    }
                                }, "DBFix-KevinXp").start();
                            }
                        } else {
                            long saved = db.getKevinXp(user);
                            if (saved > 0 && persistentKevin != null) { persistentKevin.setXp(saved); System.out.println("[ArenaLoader] Loaded Kevin XP for " + user + ": " + saved); }
                        }

                        // --- PERSIST LAST PLAYED STAGE FOR RESUME FEATURE ---
                        try {
                            String username = ((UI.MainFrame) mainFrame).getCurrentUsername();
                            if (username != null && !username.trim().isEmpty()) {
                                final int ch = currentChapter;
                                final int st = currentStage;
                                new Thread(() -> {
                                    try {
                                        UI.DatabaseManager db2 = new UI.DatabaseManager();
                                        db2.setChapterStage(username, ch, st);
                                    } catch (Exception e) { e.printStackTrace(); }
                                }).start();
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}

            // Reset Position for new level
            persistentPlayer.r = 20;
            persistentPlayer.c = 15;
            persistentPlayer.currentDirection = Direction.EAST;
            persistentPlayer.setStunned(false); // Make sure the player isn't stunned from last level
            // Refill discs and reset health for new stage
            persistentPlayer.prepareForNextStage();

            // Add to active cycle list
            List<Character> cycles = new ArrayList<>();
            cycles.add(persistentPlayer);

            // Load Enemies
            List<Character> enemies = LevelManager.loadStage(currentChapter, currentStage, arena.getGrid());
            cycles.addAll(enemies);

            // Check if any boss enemies spawned and show notification
            StringBuilder bossWarning = new StringBuilder();
            for (Character c : enemies) {
                if (c instanceof designenemies.Enemy) {
                    designenemies.Enemy enemy = (designenemies.Enemy) c;
                    if (enemy.isBoss()) {
                        bossWarning.append("⚠ BOSS ALERT: ").append(enemy.getName()).append(" [BOSS]\n");
                    }
                }
            }
            if (bossWarning.length() > 0) {
                // Show boss warning and FREEZE the game (blocking dialog)
                JLabel msgLabel = new JLabel("<html><b style='color:red;font-size:16px;'>⚠ BOSS ENCOUNTER ⚠</b><br>" +
                        bossWarning.toString().replace("\n", "<br>") + 
                        "<br>Watch out for faster movement and longer trails!</html>");
                JOptionPane.showMessageDialog(mainFrame, msgLabel, "BOSS WARNING", JOptionPane.WARNING_MESSAGE);
            }

            System.out.println("Starting C" + currentChapter + ":S" + currentStage + " | Player Level: " + persistentPlayer.getLevel());

            // --- CREATE GAME PANEL (for cutscene integration) ---
            UI.GamePanel gamePanel = new UI.GamePanel();
            JPanel arenaPanel = new JPanel(new GridLayout(40, 40));
            arenaPanel.setBackground(new Color(10, 10, 20));

            // Pass the selected persistent player to sidebar
            JPanel sidebarPanel = createSidebarPanel(persistentPlayer, icons);

            JPanel container = new JPanel(new BorderLayout());
            container.add(gamePanel, BorderLayout.CENTER); // Use gamePanel for cutscene overlay
            container.add(sidebarPanel, BorderLayout.EAST);
            mainFrame.setContentPane(container);
            mainFrame.revalidate();
            mainFrame.repaint();

            // Ensure the GamePanel has keyboard focus and a running thread so it can receive
            // Space (and other) key events while the pre-stage cutscene plays.
            gamePanel.setFocusable(true);
            // Request focus on the EDT to increase reliability
            SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
            // Start the internal game loop and attach KeyListener to the frame
            gamePanel.startGameThread(mainFrame);

            // Show pre-stage cutscene (suffix "a") if available, using CutsceneUtil and the real GamePanel
            // Avoid showing it again if it was already shown earlier (e.g., after character selection)
            if (!(lastPreCutsceneChapter == currentChapter && lastPreCutsceneStage == currentStage)) {
                // Play the per-stage pre-cutscene WITHOUT following NEXT_FILE links so it doesn't chain to other stages
                CutsceneUtil.showCutsceneIfExists(currentChapter, currentStage, "a", mainFrame, gamePanel, false);
                // Record that we showed it
                lastPreCutsceneChapter = currentChapter;
                lastPreCutsceneStage = currentStage;
            }

            // Wait for cutscene to finish before showing start simulation dialog.
            // IMPORTANT: Do not block the EDT here (cutscene animations run on the GamePanel's thread),
            // so spawn a background waiter thread and continue work on the EDT when ready.
            Thread waiter = new Thread(() -> {
                try {
                    while (gamePanel.cutscene.isActive()) {
                        Thread.sleep(50);
                    }

                    // Now show the start dialog and initialize the active controller on the EDT
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(mainFrame, "Press OK to start simulation!\nUse arrow keys to move.", "Start Simulation", JOptionPane.INFORMATION_MESSAGE);
                        // Ensure frame is focused for keyboard input
                        mainFrame.requestFocusInWindow();

                        // --- SANITIZE GRID: remove stray 'D' markers so discs are visible on new stage ---
                        // Ensure any lingering cutscene overlay is force-stopped so arena renders cleanly
                        try { gamePanel.cutscene.forceStop(); } catch (Exception ignored) {}
                        sanitizeGrid(arena);

                        // Replace the temporary GamePanel with the actual arena panel so the arena is visible
                        Container content = mainFrame.getContentPane();
                        try {
                            // Stop the game panel thread before removing it to avoid stray repaints
                            try { gamePanel.stopGameThread(); } catch (Exception ignored) {}
                            content.remove(gamePanel);
                            content.add(arenaPanel, BorderLayout.CENTER);
                        } catch (Exception e) {
                            System.out.println("WARN: Failed to swap panels: " + e.getMessage());
                        }

                        // Populate arena and refresh UI
                        redrawArena(mainFrame, arena, cycles, icons, arenaPanel, sidebarPanel);
                        mainFrame.revalidate(); mainFrame.repaint();

                        // Start the active controller (game loop)
                        activeController = new GameController(mainFrame, arena, cycles, icons, arenaPanel, sidebarPanel);
                        gameThread = new Thread(activeController);
                        gameThread.start();
                        // Allow future startLevel calls after initialization completes
                        synchronized (startLock) { levelStarting = false; }
                    });
                } catch (InterruptedException ignored) {}
            });
            waiter.setDaemon(true);
            waiter.start();
        } catch (Exception ex) {
            System.err.println("[ArenaLoader] Error during startLevel: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Failed to load the next chapter: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // Return to chapter selection to let user choose again
            if (mainFrame instanceof UI.MainFrame) ((UI.MainFrame) mainFrame).changeToStoryMode();
            synchronized (startLock) { levelStarting = false; }
        }
        // Note: the rest of the initialization (sanitizing grid, swapping panels, and starting
        // the GameController) is handled in the background waiter thread above. We purposely
        // avoid duplicating that logic here to prevent double-initialization when startLevel
        // is called multiple times during chapter transitions.

        // levelStarting will be reset by the waiter thread once initialization completes.
    }

    /**
     * Minimal, non-invasive fix:
     * Clear any leftover 'D' characters from the grid so new discs spawned in the new stage
     * are not immediately considered "occupi.
     *
     * We intentionally only touch 'D' here to avoid changing any trail/wall logic.
     */
    private static void sanitizeGrid(Arena arena) {
        try {
            char[][] grid = arena.getGrid();
            if (grid == null) return;
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[r].length; c++) {
                    if (grid[r][c] == 'D') {
                        char base = arena.getBaseTile(r, c);
                        grid[r][c] = (base != '\0') ? base : '.';
                    }
                }
            }
        } catch (Exception ex) {
            // If Arena.getGrid() has special behavior, don't crash the loader.
            System.out.println("WARN: sanitizeGrid failed: " + ex.getMessage());
        }
    }

    public static void showLevelCompleteDialog() {
        // Show post-stage cutscene (suffix "b") if available. Use a temporary modal GamePanel
        // so we can play the cutscene even when the regular GamePanel is not present.
        if (CutsceneUtil.cutsceneExists(currentChapter, currentStage, "b")) {
            JDialog dlg = new JDialog(mainFrame, "Cutscene", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setUndecorated(true);
            // Fullscreen bounds to match the main frame
            if (mainFrame != null) dlg.setBounds(mainFrame.getBounds()); else dlg.setSize(800, 600);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            UI.GamePanel temp = new UI.GamePanel();
            temp.setPreferredSize(dlg.getSize());
            dlg.getContentPane().add(temp);
            dlg.pack();
            dlg.setLocationRelativeTo(mainFrame);
            temp.setFocusable(true);
            SwingUtilities.invokeLater(() -> temp.requestFocusInWindow());
            temp.startGameThread(mainFrame);
            temp.startCutscene(String.format("c%dlevel%d%s.txt", currentChapter, currentStage, "b"), false);

            Thread waiter = new Thread(() -> {
                try {
                    while (temp.cutscene.isActive() || temp.cutscene.isFadingOut()) Thread.sleep(50);
                } catch (InterruptedException ignored) {}
                SwingUtilities.invokeLater(() -> { try { temp.cutscene.forceStop(); } catch (Exception ignored) {} try { temp.stopGameThread(); } catch (Exception ignored) {} dlg.dispose(); });
            });
            waiter.setDaemon(true);
            waiter.start();
            dlg.setVisible(true);
        }
        TronRules.StageType type = TronRules.StageType.NORMAL;
        if (currentStage == 1 && currentChapter == 1) type = TronRules.StageType.TUTORIAL;

        long xpReward = 0;
        double currentLives = 0;
        double maxLives = 0;

        if (activeController != null) {
            Character player = activeController.getPlayer();
            // Use stage-clear-only XP (no per-kill farming). The award is applied in GameController
            xpReward = TronRules.calculateStageClearXp(currentChapter, currentStage);
            currentLives = player.getLives();
            maxLives = player.getMaxLives();

            // Persist the player's progress (non-blocking)
            if (mainFrame instanceof UI.MainFrame) {
                String username = ((UI.MainFrame) mainFrame).getCurrentUsername();
                if (username != null && !username.trim().isEmpty()) {
                    long totalXp = player.getXp();
                    int chapterToSave = currentChapter; // save current unlocked chapter
                    int tronLvl = (persistentTron != null) ? persistentTron.getLevel() : 0;
                    int kevinLvl = (persistentKevin != null) ? persistentKevin.getLevel() : 0;
                    String timeNow = java.time.Instant.now().toString();
                    new Thread(() -> {
                        try {
                            UI.DatabaseManager db = new UI.DatabaseManager();
                            db.updateCompletion(username, chapterToSave, totalXp, timeNow, tronLvl, kevinLvl);
                            // Persist precise XP as well so level and XP stay consistent after restart
                            if (persistentPlayer != null) {
                                if ("Tron".equalsIgnoreCase(persistentPlayer.name)) db.setTronXp(username, totalXp);
                                else if ("Kevin".equalsIgnoreCase(persistentPlayer.name)) db.setKevinXp(username, totalXp);
                            }

                            // --- IMPORTANT: Save next stage progress so returning to chapter goes to the next stage ---
                            try {
                                int nextStage = currentStage + 1;
                                int max = XPSystem.TronRules.getStagesForChapter(currentChapter);
                                if (nextStage <= max) {
                                    // Only update if it advances the saved stage
                                    int prev = db.getChapterStage(username, currentChapter);
                                    if (nextStage > prev) db.setChapterStage(username, currentChapter, nextStage);
                                } else {
                                    // Completed final stage -> ensure next chapter is unlocked and set its stage to 1
                                    int nextChapter = currentChapter + 1;
                                    if (nextChapter <= 5) {
                                        int prev = db.getChapterStage(username, nextChapter);
                                        if (1 > prev) db.setChapterStage(username, nextChapter, 1);
                                    }
                                }
                            } catch (Exception e) { System.out.println("[WARN] Failed to persist next-stage progress: " + e.getMessage()); }

                        } catch (Exception e) { e.printStackTrace(); }
                    }).start();
                }
            }
        }

        String message = String.format(
            "STAGE CLEARED!\n\nXP Earned: %d\nCurrent Lives: %.1f / %.1f\n\nWhat would you like to do next?",
            xpReward, currentLives, maxLives
        );

        Object[] options = {"NEXT STAGE", "RETURN TO CHAPTER SELECT", "QUIT"};
        int choice = JOptionPane.showOptionDialog(mainFrame, message, "SECTOR SECURE",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
            options, options[0]);

        if (choice == 0) { // NEXT STAGE
            currentStage++;

            // General progression: if we've advanced past the last stage of the chapter,
            // move to the next chapter (if any) and show chapter complete dialog.
            int maxStages = XPSystem.TronRules.getStagesForChapter(currentChapter);
            if (currentStage > maxStages) {
                int nextChapter = currentChapter + 1;
                if (nextChapter <= 5) {
                    System.out.println(String.format("[ArenaLoader] Chapter %d completed. Advancing to Chapter %d", currentChapter, nextChapter));
                    currentChapter = nextChapter;
                    currentStage = 1;
                    showChapterClearDialog();
                    return;
                } else {
                    // Player finished all chapters
                    System.out.println("[ArenaLoader] All chapters completed. Returning to Chapter Select.");
                    // Stop any running game thread safely, then return to story mode
                    try { if (activeController != null) activeController.stopGame(); } catch (Exception ignored) {}
                    try { if (gameThread != null && gameThread.isAlive()) gameThread.interrupt(); } catch (Exception ignored) {}
                    if (mainFrame instanceof UI.MainFrame) ((UI.MainFrame) mainFrame).changeToStoryMode();
                    else JOptionPane.showMessageDialog(mainFrame, "Congratulations — you completed all chapters!");
                    return;
                }
            }

            startLevel();

        } else if (choice == 1) { // RETURN TO CHAPTER SELECT
            // Safely stop the active game and return to Story Mode
            try {
                if (activeController != null) activeController.stopGame();
            } catch (Exception ignored) {}
            try {
                if (gameThread != null && gameThread.isAlive()) {
                    gameThread.interrupt();
                }
            } catch (Exception ignored) {}

            if (mainFrame instanceof UI.MainFrame) {
                ((UI.MainFrame) mainFrame).changeToStoryMode();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Returning to Chapter Selection.");
            }

        } else { // QUIT
            System.exit(0);
        }
    }

    private static void showChapterClearDialog() {
        Object[] options = {"Next Chapter", "Chapter Select", "Exit"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
                "CHAPTER COMPLETE!\nWhat would you like to do next?",
                "CHAPTER COMPLETE",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0 || choice == 1) {
            // Persist chapter completion (unlock saved) before returning/starting
            if (mainFrame instanceof UI.MainFrame) {
                String username = ((UI.MainFrame) mainFrame).getCurrentUsername();
                if (username != null && !username.trim().isEmpty()) {
                    final int chapterToSave = currentChapter; // already incremented by caller
                    final long totalXp = (persistentPlayer != null) ? persistentPlayer.getXp() : 0L;
                    final int tronLvl = (persistentTron != null) ? persistentTron.getLevel() : 0;
                    final int kevinLvl = (persistentKevin != null) ? persistentKevin.getLevel() : 0;
                    final String timeNow = java.time.Instant.now().toString();
                    new Thread(() -> {
                        try {
                            UI.DatabaseManager db = new UI.DatabaseManager();
                            db.updateCompletion(username, chapterToSave, totalXp, timeNow, tronLvl, kevinLvl);
                            if (persistentPlayer != null) {
                                if ("Tron".equalsIgnoreCase(persistentPlayer.name)) db.setTronXp(username, totalXp);
                                else if ("Kevin".equalsIgnoreCase(persistentPlayer.name)) db.setKevinXp(username, totalXp);
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }).start();
                }
            }
        }

        if (choice == 0) {
            // Proceed to next chapter (start next level)
            startLevel();
        } else if (choice == 1) {
            // Return to Chapter Selection (Story Mode)
            if (mainFrame instanceof UI.MainFrame) {
                ((UI.MainFrame) mainFrame).changeToStoryMode();
            } else {
                // Fallback: just show a message and return to main frame
                JOptionPane.showMessageDialog(mainFrame, "Returning to Chapter Selection.");
            }
        } else {
            // Exit the game
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        mainFrame = new JFrame("Tron Legacy: Grid Arena");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setLayout(new BorderLayout());
        StartGameMenu.showMenu(mainFrame);
        mainFrame.setVisible(true);
        startLevel();
    }
}
