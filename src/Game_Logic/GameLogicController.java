package Game_Logic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

// This class is the "Brain" of the game.
public class GameLogicController implements ActionListener {

    // References to your teammates' work
    private Arena map;
    private Player player;
    private ArrayList<Enemy> enemies;
    private GameUI ui;

    // Game State
    private boolean isRunning = false;
    private Timer gameLoop;
    private int currentStage = 1;
    private Random random = new Random();

    // Weapon System
    private ArrayList<Disc> activeDiscs = new ArrayList<>();
    private long lastFireTime = 0;
    private final long FIRE_COOLDOWN = 500;

    // Story System
    private StoryManager storyManager;

    // Constructor
    public GameLogicController(Arena map, Player player, ArrayList<Enemy> enemies, GameUI ui) {
        this.map = map;
        this.player = player;
        this.enemies = enemies;
        this.ui = ui;
        this.gameLoop = new Timer(80, this);
        this.storyManager = new StoryManager();
    }

    public void startGame() {
        // Initial Setup
        if (storyManager.hasStory(1)) {
            ui.showStoryScreen(storyManager.getStory(1));
        } else {
            startStageLogic(1);
        }
    }

    public void startStageLogic(int stage) {
        currentStage = stage;
        setupStage(stage);
        isRunning = true;
        gameLoop.start();

        // Refocus for controls
        if (ui instanceof MockUI) {
            ((MockUI) ui).hideStoryScreen();
            ((MockUI) ui).hideStageMenu();
            ((MockUI) ui).canvas.requestFocusInWindow();
        }
    }

    // --- NEW: RESTART & EXIT FUNCTIONS ---
    public void restartGame() {
        gameLoop.stop();
        currentStage = 1;
        isRunning = true;
        activeDiscs.clear();

        startGame(); // Will trigger Stage 1 story again
        ui.showStatusMessage("System Rebooted. Grid Online.");
    }

    public void exitGame() {
        System.exit(0);
    }

    // --- WEAPON LOGIC ---
    public void attemptFireWeapon() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime >= FIRE_COOLDOWN) {
            lastFireTime = currentTime;
            // Spawn Disc with 3 bounces
            activeDiscs.add(new Disc(player.getX(), player.getY(), player.getDirection(), true));
            ui.showStatusMessage("Disc Launched!");
        } else {
            ui.showStatusMessage("Weapon Recharging...");
        }
    }

    // --- STAGE LOGIC ---
    private void handleStageWin() {
        gameLoop.stop();
        isRunning = false;
        activeDiscs.clear();

        // Calculate Stage Bonus XP
        int bonusXP = currentStage * 50;
        player.addXP(bonusXP);

        // Show "Continue?" Screen
        ui.showStatusMessage("Stage " + currentStage + " Cleared. Processing XP...");
        ui.showStageWinScreen(currentStage, bonusXP);
    }

    public void continueToNextStage() {
        int nextStage = currentStage + 1;

        // Check if the NEXT stage has a story cutscene
        if (storyManager.hasStory(nextStage)) {
            ui.showStoryScreen(storyManager.getStory(nextStage));
        } else {
            startStageLogic(nextStage);
        }
    }

    private void setupStage(int stage) {
        // Reset Player Position & Health
        player.reset();
        player.setX(20);
        player.setY(20);
        activeDiscs.clear();
        lastFireTime = 0; // Reset cooldown

        ui.showStatusMessage("Stage " + stage + " Initiated.");

        // Reset/Spawn Enemies Logic
        for (Enemy e : enemies) {
            e.reset(); // Revive
            // Randomize positions
            int safeX, safeY;
            do {
                safeX = random.nextInt(35) + 2;
                safeY = random.nextInt(35) + 2;
            } while (Math.abs(safeX - player.getX()) < 5 && Math.abs(safeY - player.getY()) < 5); // Don't spawn on
                                                                                                  // player

            e.setX(safeX);
            e.setY(safeY);
        }
    }

    // --- THE MAIN LOOP ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isRunning)
            return;

        updatePositions();
        checkCollisions();
        checkGameState();

        ui.repaint();
    }

    // --- STEP 1: MOVEMENT & AI ---
    private void updatePositions() {
        player.move();

        // Move Discs (High Speed Logic)
        Iterator<Disc> discIter = activeDiscs.iterator();
        while (discIter.hasNext()) {
            Disc d = discIter.next();
            boolean keepDisc = true;

            // Move 2 steps per frame (Speed = 2x Player)
            for (int i = 0; i < 2; i++) {
                // Move and handle bouncing inside this method
                keepDisc = d.moveAndBounce(map);

                // Check Collision IMMEDIATELY after each sub-step to prevent tunneling
                if (keepDisc && checkDiscHitEnemy(d)) {
                    keepDisc = false; // Hit enemy, destroy disc
                    break;
                }

                if (!keepDisc)
                    break; // Hit wall with no bounces left
            }

            if (!keepDisc) {
                discIter.remove();
            }
        }

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive())
                continue;

            processEnemyAI(enemy);
            enemy.move();
        }
    }

    // Helper to check if a specific disc hit any enemy
    private boolean checkDiscHitEnemy(Disc d) {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive())
                continue;
            if (d.x == enemy.getX() && d.y == enemy.getY()) {
                handleDamage(enemy, 999.0); // Insta-kill
                ui.showStatusMessage("TARGET ELIMINATED");
                return true;
            }
        }
        // Optional: Destroy disc on trail hit?
        if (map.isTrail(d.x, d.y)) {
            return true;
        }
        return false;
    }

    // --- AI LOGIC HUB ---
    private void processEnemyAI(Enemy enemy) {
        String name = enemy.getName();

        // Scaling Intelligence: Bot prediction improves with Stages
        int predictionDepth = 5 + (currentStage / 2);

        if (name.contains("Koura")) {
            // Koura Evolves: Random -> Chase -> Intercept
            if (currentStage >= 6)
                makeInterceptMove(enemy, predictionDepth);
            else if (currentStage >= 3)
                makeChaseMove(enemy);
            else
                makeRandomMove(enemy);
        } else if (name.contains("Sark")) {
            // Sark Evolves: Chase -> Intercept
            if (currentStage >= 4)
                makeInterceptMove(enemy, predictionDepth);
            else
                makeChaseMove(enemy);
        } else if (name.contains("Rinzler")) {
            // Rinzler gets smarter every stage
            makeInterceptMove(enemy, predictionDepth + 2);
        } else if (name.contains("Clu")) {
            // Clu is max intelligence
            makeInterceptMove(enemy, predictionDepth + 5);
        } else {
            makeSafeMove(enemy);
        }
    }

    private void makeRandomMove(Enemy enemy) {
        if (random.nextInt(100) < 20) {
            pickRandomSafeDirection(enemy);
        } else {
            if (!isMoveSafe(enemy, enemy.getDirection())) {
                pickRandomSafeDirection(enemy);
            }
        }
    }

    private void makeChaseMove(Enemy enemy) {
        int dx = player.getX() - enemy.getX();
        int dy = player.getY() - enemy.getY();
        String preferredDir;
        if (Math.abs(dx) > Math.abs(dy)) {
            preferredDir = (dx > 0) ? "RIGHT" : "LEFT";
        } else {
            preferredDir = (dy > 0) ? "DOWN" : "UP";
        }

        if (isMoveSafe(enemy, preferredDir)) {
            enemy.setDirection(preferredDir);
        } else {
            pickRandomSafeDirection(enemy);
        }
    }

    private void makeInterceptMove(Enemy enemy, int predictionSteps) {
        int targetX = player.getX();
        int targetY = player.getY();
        String pDir = player.getDirection();
        if (pDir.equals("UP"))
            targetY -= predictionSteps;
        if (pDir.equals("DOWN"))
            targetY += predictionSteps;
        if (pDir.equals("LEFT"))
            targetX -= predictionSteps;
        if (pDir.equals("RIGHT"))
            targetX += predictionSteps;

        int dx = targetX - enemy.getX();
        int dy = targetY - enemy.getY();

        String bestDir = (Math.abs(dx) > Math.abs(dy)) ? ((dx > 0) ? "RIGHT" : "LEFT") : ((dy > 0) ? "DOWN" : "UP");

        if (isMoveSafe(enemy, bestDir)) {
            enemy.setDirection(bestDir);
        } else {
            pickRandomSafeDirection(enemy);
        }
    }

    private boolean isMoveSafe(Enemy enemy, String direction) {
        int nextX = enemy.getX();
        int nextY = enemy.getY();

        if (direction.equals("UP"))
            nextY--;
        if (direction.equals("DOWN"))
            nextY++;
        if (direction.equals("LEFT"))
            nextX--;
        if (direction.equals("RIGHT"))
            nextX++;

        // Basic Safety Check
        if (map.isWall(nextX, nextY) || map.isTrail(nextX, nextY)) {
            return false;
        }

        // Advanced Safety (Unlock at Stage 2+): Check for Dead Ends
        if (currentStage >= 2) {
            boolean hasExit = false;
            // Check neighbors of the NEXT tile
            if (!map.isWall(nextX, nextY - 1) && !map.isTrail(nextX, nextY - 1))
                hasExit = true; // Up
            else if (!map.isWall(nextX, nextY + 1) && !map.isTrail(nextX, nextY + 1))
                hasExit = true; // Down
            else if (!map.isWall(nextX - 1, nextY) && !map.isTrail(nextX - 1, nextY))
                hasExit = true; // Left
            else if (!map.isWall(nextX + 1, nextY) && !map.isTrail(nextX + 1, nextY))
                hasExit = true; // Right

            if (!hasExit)
                return false; // It's a trap! Don't go there.
        }

        return true;
    }

    private void pickRandomSafeDirection(Enemy enemy) {
        String[] dirsArray = { "UP", "DOWN", "LEFT", "RIGHT" };
        List<String> dirs = Arrays.asList(dirsArray);
        Collections.shuffle(dirs);
        for (String tryDir : dirs) {
            if (isMoveSafe(enemy, tryDir)) {
                enemy.setDirection(tryDir);
                return;
            }
        }
    }

    private void makeSafeMove(Enemy enemy) {
        if (!isMoveSafe(enemy, enemy.getDirection())) {
            pickRandomSafeDirection(enemy);
        }
    }

    // --- STEP 2: COLLISIONS ---
    private void checkCollisions() {
        // 1. Wall Collisions (Reverse Direction)
        if (map.isWall(player.getX(), player.getY())) {
            handleDamage(player, 0.5);
            bounceCharacter(player);
        }

        // 2. Trail Collisions
        if (map.isTrail(player.getX(), player.getY())) {
            handleDamage(player, 0.5);
        }

        // 4. Enemy Logic
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive())
                continue;

            // A. Head-on Collisions
            if (player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                handleDamage(player, 1.0);
                handleDamage(enemy, 999.0);
            }

            // B. Enemy Hitting Walls
            if (map.isWall(enemy.getX(), enemy.getY())) {
                bounceCharacter(enemy);
            }

            // C. Enemy Hitting Trails
            if (map.isTrail(enemy.getX(), enemy.getY())) {
                handleDamage(enemy, 999.0);
            }
        }
    }

    private void bounceCharacter(GameCharacter c) {
        String dir = c.getDirection();
        int x = c.getX();
        int y = c.getY();

        if (x < 0 || x >= 40) {
            if (x < 0)
                c.setX(0);
            else
                c.setX(39);
            if (random.nextBoolean())
                c.setDirection("UP");
            else
                c.setDirection("DOWN");
        } else if (y < 0 || y >= 40) {
            if (y < 0)
                c.setY(0);
            else
                c.setY(39);
            if (random.nextBoolean())
                c.setDirection("LEFT");
            else
                c.setDirection("RIGHT");
        } else {
            if (dir.equals("LEFT") || dir.equals("RIGHT")) {
                if (random.nextBoolean())
                    c.setDirection("UP");
                else
                    c.setDirection("DOWN");
            } else {
                if (random.nextBoolean())
                    c.setDirection("LEFT");
                else
                    c.setDirection("RIGHT");
            }
        }
    }

    private void handleDamage(GameCharacter character, double damage) {
        if (character instanceof Enemy) {
            damage = 999.0;
        }

        character.reduceLives(damage);
        ui.showStatusMessage(character.getName() + " damaged.");

        if (character.getLives() <= 0) {
            if (character instanceof Player) {
                gameOver();
            } else if (character instanceof Enemy) {
                character.setAlive(false);

                int xpGain = calculateEnemyXP((Enemy) character);
                int oldLevel = player.getLevel();
                player.addXP(xpGain);

                ui.showStatusMessage(character.getName() + " Derezzed! +" + xpGain + " XP");
                if (player.getLevel() > oldLevel) {
                    ui.showStatusMessage("SYSTEM UPGRADE! Level " + player.getLevel());
                }
            }
        }
    }

    private int calculateEnemyXP(Enemy e) {
        String name = e.getName();
        if (name.contains("Koura"))
            return 10;
        if (name.contains("Sark"))
            return 20;
        if (name.contains("Rinzler"))
            return 50;
        if (name.contains("Clu"))
            return 100;
        return 10;
    }

    // --- STEP 3: GAME STATE ---
    private void checkGameState() {
        boolean allEnemiesDead = true;
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                allEnemiesDead = false;
                break;
            }
        }

        if (allEnemiesDead) {
            handleStageWin();
        }
    }

    private void gameOver() {
        isRunning = false;
        gameLoop.stop();
        ui.showGameOverScreen(player.getXP());
    }

    // --- STORY MANAGER ---
    // Made static to prevent inner class logic issues
    static class StoryManager {
        private Map<Integer, StoryData> stories = new HashMap<>();

        public StoryManager() {
            if (!loadStoriesFromFile()) {
                loadHardcodedStories();
            }
        }

        private boolean loadStoriesFromFile() {
            try (BufferedReader br = new BufferedReader(new FileReader("story_data.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length >= 3) {
                        stories.put(Integer.parseInt(parts[0]), new StoryData(parts[1], parts[2]));
                    }
                }
                return !stories.isEmpty();
            } catch (IOException e) {
                return false;
            }
        }

        private void loadHardcodedStories() {
            stories.put(1, new StoryData("THE REBOOT",
                    "Ten years later... Unbeknownst to Kevin, CLU 2.0 has rebooted. Corrupted programs are hunting you. Survive the ambush!"));
            stories.put(3, new StoryData("GRID DESTABILIZED",
                    "CLU captured two ISOs: Quorra and Miche. They are corrupted. Defeat the corrupted programs to purify the code!"));
            stories.put(5, new StoryData("SARK'S AMBUSH",
                    "Sark has ambushed Kevin and Tron! Kevin reveals he is secretly building a new CLU fragment. Defeat Sark's minions."));
            stories.put(7, new StoryData("THE TRAP",
                    "Tron located CLU, but it was a trap. Sark threatens the team. You must defeat Sark once and for all!"));
            stories.put(9, new StoryData("ENTER RINZLER",
                    "A mysterious jet draws a trail, causing Kevin to crash. A warrior named Rinzler steps out. Survive!"));
            stories.put(12, new StoryData("FINAL BATTLE",
                    "Kevin has stabilized the Grid. Enraged, CLU challenges you to a final battle. Defeat him to return to the real world!"));
        }

        public boolean hasStory(int stage) {
            return stories.containsKey(stage);
        }

        public StoryData getStory(int stage) {
            return stories.get(stage);
        }

        static class StoryData {
            String title;
            String text;

            public StoryData(String t, String txt) {
                title = t;
                text = txt;
            }
        }
    }

    // --- INTERNAL CLASS FOR PROJECTILES ---
    // Made static to act as a proper standalone helper
    static class Disc {
        int x, y;
        String direction;
        boolean fromPlayer;
        int bouncesLeft = 3;

        public Disc(int x, int y, String dir, boolean playerOwner) {
            this.x = x;
            this.y = y;
            this.direction = dir;
            this.fromPlayer = playerOwner;
        }

        public boolean moveAndBounce(Arena map) {
            int nextX = x;
            int nextY = y;

            if (direction.equals("UP"))
                nextY--;
            else if (direction.equals("DOWN"))
                nextY++;
            else if (direction.equals("LEFT"))
                nextX--;
            else if (direction.equals("RIGHT"))
                nextX++;

            if (map.isWall(nextX, nextY)) {
                if (bouncesLeft > 0) {
                    bouncesLeft--;
                    if (direction.equals("UP"))
                        direction = "DOWN";
                    else if (direction.equals("DOWN"))
                        direction = "UP";
                    else if (direction.equals("LEFT"))
                        direction = "RIGHT";
                    else if (direction.equals("RIGHT"))
                        direction = "LEFT";
                    return true;
                } else {
                    return false;
                }
            } else {
                x = nextX;
                y = nextY;
                return true;
            }
        }
    }

    // =================================================================================
    // MOCK CLASSES
    // =================================================================================

    public static void main(String[] args) {
        MockArena mockMap = new MockArena();
        MockPlayer mockPlayer = new MockPlayer();
        ArrayList<Enemy> mockEnemies = new ArrayList<>();

        mockEnemies.add(new MockEnemy("Sark (Chase)", 35, 35));
        mockEnemies.add(new MockEnemy("Koura (Random)", 5, 35));

        mockMap.setCharacters(mockPlayer, mockEnemies);

        MockUI mockUI = new MockUI(mockPlayer, mockEnemies);
        GameLogicController controller = new GameLogicController(mockMap, mockPlayer, mockEnemies, mockUI);

        mockUI.setActiveDiscs(controller.activeDiscs);
        mockUI.setController(controller);
        mockUI.setPlayerReference(mockPlayer);

        controller.startGame();
    }

    // Interfaces
    interface Arena {
        boolean isWall(int x, int y);

        boolean isTrail(int x, int y);
    }

    interface GameCharacter {
        int getX();

        int getY();

        void setX(int x);

        void setY(int y);

        void move();

        void reduceLives(double d);

        double getLives();

        String getName();

        void setAlive(boolean b);

        boolean isAlive();

        String getDirection();

        void setDirection(String d);

        void reset();
    }

    interface Player extends GameCharacter {
        void addXP(int xp);

        int getXP();

        int getLevel();

        int getNextLevelXP();
    }

    interface Enemy extends GameCharacter {
    }

    interface GameUI {
        void repaint();

        void showStatusMessage(String s);

        void showGameOverScreen(int score);

        void showStageWinScreen(int stage, int bonusXP);

        void showStoryScreen(GameLogicController.StoryManager.StoryData story);
    }

    // 1. Mock Arena
    static class MockArena implements Arena {
        private Player p;
        private ArrayList<Enemy> e;

        public void setCharacters(Player p, ArrayList<Enemy> e) {
            this.p = p;
            this.e = e;
        }

        public boolean isWall(int x, int y) {
            return x < 0 || x >= 40 || y < 0 || y >= 40;
        }

        public boolean isTrail(int x, int y) {
            if (p instanceof MockCharacter)
                for (Point pt : ((MockCharacter) p).trail)
                    if (pt.x == x && pt.y == y)
                        return true;
            for (Enemy en : e)
                if (en.isAlive() && en instanceof MockCharacter)
                    for (Point pt : ((MockCharacter) en).trail)
                        if (pt.x == x && pt.y == y)
                            return true;
            return false;
        }
    }

    // 2. Mock Character Base
    static abstract class MockCharacter implements GameCharacter {
        int x, y;
        double maxLives = 3.0;
        double lives = maxLives;
        boolean alive = true;
        String direction = "RIGHT";
        public List<Point> trail = new ArrayList<>();

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public double getLives() {
            return lives;
        }

        public void reduceLives(double d) {
            lives -= d;
        }

        public boolean isAlive() {
            return alive;
        }

        public void setAlive(boolean b) {
            alive = b;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String d) {
            this.direction = d;
        }

        public void reset() {
            lives = maxLives;
            alive = true;
            trail.clear();
            direction = "RIGHT";
        }

        public void move() {
            trail.add(new Point(x, y));
            if (trail.size() > 100)
                trail.remove(0);
            if (direction.equals("UP"))
                y--;
            else if (direction.equals("DOWN"))
                y++;
            else if (direction.equals("LEFT"))
                x--;
            else if (direction.equals("RIGHT"))
                x++;
        }
    }

    // 3. Mock Player
    static class MockPlayer extends MockCharacter implements Player {
        int xp = 0;
        int level = 1;
        int nextLevelThreshold = 5;

        public MockPlayer() {
            x = 20;
            y = 20;
        }

        public String getName() {
            return "Player";
        }

        public void addXP(int amount) {
            this.xp += amount;
            while (this.xp >= nextLevelThreshold) {
                this.xp -= nextLevelThreshold;
                level++;
                nextLevelThreshold = level * 5;
            }
        }

        public int getXP() {
            return xp;
        }

        public int getLevel() {
            return level;
        }

        public int getNextLevelXP() {
            return nextLevelThreshold;
        }
    }

    // 4. Mock Enemy
    static class MockEnemy extends MockCharacter implements Enemy {
        String name;

        public MockEnemy(String name, int startX, int startY) {
            this.name = name;
            this.x = startX;
            this.y = startY;
        }

        public String getName() {
            return name;
        }
    }

    // 5. Mock UI
    static class MockUI extends JFrame implements GameUI {
        Player playerRef;
        ArrayList<Enemy> enemiesRef;
        ArrayList<Disc> discsRef;
        GameCanvas canvas;
        GameLogicController controller;
        JPanel stageMenuPanel;
        JLabel stageInfoLabel;
        JPanel storyPanel;
        JLabel storyTitle, storyText;
        JPanel controlPanel;

        public MockUI(Player p, ArrayList<Enemy> e) {
            this.playerRef = p;
            this.enemiesRef = e;
            this.canvas = new GameCanvas();
            this.setTitle("TRON: LIGHT CYCLE ARENA");
            this.setLayout(new BorderLayout());
            this.add(canvas, BorderLayout.CENTER);

            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(new Color(20, 20, 30));
            JButton restartBtn = createNeonButton("REBOOT SYSTEM");
            JButton exitBtn = createNeonButton("EXIT GRID");
            restartBtn.addActionListener(ev -> {
                if (controller != null)
                    controller.restartGame();
            });
            exitBtn.addActionListener(ev -> {
                if (controller != null)
                    controller.exitGame();
            });
            controlPanel.add(restartBtn);
            controlPanel.add(exitBtn);
            this.add(controlPanel, BorderLayout.SOUTH);

            stageMenuPanel = new JPanel(new BorderLayout());
            stageMenuPanel.setBackground(new Color(0, 20, 40, 220));
            stageInfoLabel = new JLabel("SECTOR CLEARED", SwingConstants.CENTER);
            stageInfoLabel.setForeground(new Color(100, 255, 255));
            stageInfoLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
            JButton continueBtn = createNeonButton("PROCEED TO NEXT SECTOR");
            continueBtn.addActionListener(ev -> {
                hideStageMenu();
                if (controller != null)
                    controller.continueToNextStage();
            });
            stageMenuPanel.add(stageInfoLabel, BorderLayout.CENTER);
            stageMenuPanel.add(continueBtn, BorderLayout.SOUTH);
            stageMenuPanel.setVisible(false);

            storyPanel = new JPanel(new BorderLayout());
            storyPanel.setBackground(new Color(10, 10, 20));
            storyTitle = new JLabel("CHAPTER TITLE", SwingConstants.CENTER);
            storyTitle.setForeground(Color.ORANGE);
            storyTitle.setFont(new Font("Monospaced", Font.BOLD, 28));
            storyText = new JLabel("Story Text Here...", SwingConstants.CENTER);
            storyText.setForeground(Color.WHITE);
            storyText.setFont(new Font("SansSerif", Font.PLAIN, 16));
            JButton startStageBtn = createNeonButton("INITIATE SEQUENCE");
            storyPanel.add(storyTitle, BorderLayout.NORTH);
            storyPanel.add(storyText, BorderLayout.CENTER);
            storyPanel.add(startStageBtn, BorderLayout.SOUTH);
            storyPanel.setVisible(false);

            JPanel overlayContainer = new JPanel(new BorderLayout());
            overlayContainer.setOpaque(false);
            overlayContainer.add(stageMenuPanel, BorderLayout.NORTH);
            overlayContainer.add(storyPanel, BorderLayout.CENTER);
            this.setGlassPane(overlayContainer);
            overlayContainer.setVisible(true);

            this.setSize(420, 520);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setVisible(true);

            canvas.setFocusable(true);
            canvas.requestFocusInWindow();
            canvas.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    String currentDir = playerRef.getDirection();
                    if (e.getKeyCode() == KeyEvent.VK_W && !currentDir.equals("DOWN"))
                        playerRef.setDirection("UP");
                    if (e.getKeyCode() == KeyEvent.VK_S && !currentDir.equals("UP"))
                        playerRef.setDirection("DOWN");
                    if (e.getKeyCode() == KeyEvent.VK_A && !currentDir.equals("RIGHT"))
                        playerRef.setDirection("LEFT");
                    if (e.getKeyCode() == KeyEvent.VK_D && !currentDir.equals("LEFT"))
                        playerRef.setDirection("RIGHT");
                    if (e.getKeyCode() == KeyEvent.VK_F && controller != null)
                        controller.attemptFireWeapon();
                }
            });
            canvas.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    canvas.requestFocusInWindow();
                }
            });
        }

        public void setActiveDiscs(ArrayList<Disc> d) {
            this.discsRef = d;
        }

        private JButton createNeonButton(String text) {
            JButton btn = new JButton(text);
            btn.setFocusable(false);
            btn.setBackground(Color.BLACK);
            btn.setForeground(Color.CYAN);
            btn.setFont(new Font("Monospaced", Font.BOLD, 14));
            btn.setBorderPainted(false);
            return btn;
        }

        public void setController(GameLogicController c) {
            this.controller = c;
        }

        public void setPlayerReference(Player p) {
            this.playerRef = p;
        }

        public void showStatusMessage(String s) {
            System.out.println("SYSTEM: " + s);
        }

        public void showGameOverScreen(int score) {
            stageInfoLabel.setText("<html><center>DERESOLUTION<br>FINAL XP: " + score + "</center></html>");
            stageInfoLabel.setForeground(Color.RED);
            stageMenuPanel.setVisible(true);
            this.getGlassPane().setVisible(true);
            this.revalidate();
        }

        public void showStageWinScreen(int stage, int bonusXP) {
            stageInfoLabel
                    .setText("<html><center>SECTOR " + stage + " SECURED<br>BONUS: " + bonusXP + " XP</center></html>");
            stageInfoLabel.setForeground(Color.CYAN);
            stageMenuPanel.setVisible(true);
            this.getGlassPane().setVisible(true);
            this.revalidate();
        }

        public void showStoryScreen(GameLogicController.StoryManager.StoryData story) {
            storyTitle.setText(story.title);
            storyText.setText("<html><center><div style='width:250px;'>" + story.text + "</div></center></html>");
            JButton btn = (JButton) storyPanel.getComponent(2);
            for (ActionListener al : btn.getActionListeners())
                btn.removeActionListener(al);
            btn.addActionListener(ev -> {
                hideStoryScreen();
                if (controller != null)
                    controller.startStageLogic(controller.currentStage);
            });
            storyPanel.setVisible(true);
            this.getGlassPane().setVisible(true);
            this.revalidate();
        }

        public void hideStageMenu() {
            stageMenuPanel.setVisible(false);
        }

        public void hideStoryScreen() {
            storyPanel.setVisible(false);
        }

        class GameCanvas extends JPanel {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 10, 25));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 60, 80));
                for (int i = 0; i <= 400; i += 40) {
                    g2.drawLine(i, 0, i, 400);
                    g2.drawLine(0, i, 400, i);
                }
                drawGlowingTrail(g2, playerRef, new Color(0, 255, 255), new Color(0, 100, 150));
                for (Enemy en : enemiesRef) {
                    if (!en.isAlive())
                        continue;
                    Color mainColor = Color.MAGENTA;
                    Color glowColor = new Color(100, 0, 100);
                    if (en.getName().contains("Sark")) {
                        mainColor = new Color(255, 50, 50);
                        glowColor = new Color(100, 0, 0);
                    } else if (en.getName().contains("Koura")) {
                        mainColor = new Color(255, 255, 0);
                        glowColor = new Color(100, 100, 0);
                    } else if (en.getName().contains("Clu")) {
                        mainColor = new Color(255, 140, 0);
                        glowColor = new Color(120, 60, 0);
                    }
                    drawGlowingTrail(g2, en, mainColor, glowColor);
                }
                if (discsRef != null) {
                    g2.setColor(Color.YELLOW);
                    for (Disc d : discsRef) {
                        g2.fillOval(d.x * 10 + 2, d.y * 10 + 2, 6, 6);
                        g2.setColor(new Color(255, 255, 0, 100));
                        g2.fillOval(d.x * 10, d.y * 10, 10, 10);
                        g2.setColor(Color.YELLOW);
                    }
                }
                drawHUD(g2);
            }

            private void drawGlowingTrail(Graphics2D g2, GameCharacter c, Color core, Color glow) {
                if (!(c instanceof MockCharacter))
                    return;
                MockCharacter mc = (MockCharacter) c;
                g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 100));
                for (Point p : mc.trail)
                    g2.fillRect(p.x * 10 - 2, p.y * 10 - 2, 14, 14);
                g2.setColor(core);
                for (Point p : mc.trail)
                    g2.fillRect(p.x * 10, p.y * 10, 10, 10);
                g2.setColor(Color.WHITE);
                g2.fillRect(mc.getX() * 10, mc.getY() * 10, 10, 10);
            }

            private void drawHUD(Graphics2D g2) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(5, 5, 120, 90, 15, 15);
                g2.setColor(Color.CYAN);
                g2.drawRoundRect(5, 5, 120, 90, 15, 15);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.setColor(Color.WHITE);
                g2.drawString("STAGE: " + (controller != null ? controller.currentStage : 1), 15, 20);
                g2.drawString("LEVEL: " + playerRef.getLevel(), 15, 35);
                g2.setColor(new Color(100, 255, 100));
                g2.drawString("XP   : " + playerRef.getXP() + "/" + playerRef.getNextLevelXP(), 15, 50);
                long timeLeft = 0;
                if (controller != null) {
                    long elapsed = System.currentTimeMillis() - controller.lastFireTime;
                    if (elapsed < controller.FIRE_COOLDOWN)
                        timeLeft = controller.FIRE_COOLDOWN - elapsed;
                }
                g2.setColor(Color.ORANGE);
                if (timeLeft <= 0)
                    g2.drawString("DISC : READY [F]", 15, 65);
                else
                    g2.drawString("DISC : " + (timeLeft / 1000.0) + "s", 15, 65);
                g2.setColor(Color.GRAY);
                g2.fillRect(135, 10, 100, 10);
                double hpPercent = playerRef.getLives() / ((MockCharacter) playerRef).maxLives;
                if (hpPercent > 0.6)
                    g2.setColor(Color.GREEN);
                else if (hpPercent > 0.3)
                    g2.setColor(Color.YELLOW);
                else
                    g2.setColor(Color.RED);
                g2.fillRect(135, 10, (int) (100 * hpPercent), 10);
                g2.setColor(Color.WHITE);
                g2.drawRect(135, 10, 100, 10);
                g2.drawString("HP", 140 + 100 + 5, 20);
            }
        }
    }
}