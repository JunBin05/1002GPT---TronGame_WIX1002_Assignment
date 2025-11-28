import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

// This class is the "Brain" of the game.
public class GameLogicController implements ActionListener {

    // References to your teammates' work
    // NOTE: Ensure Arena, Player, Enemy, and GameUI classes exist in your project
    // folder!
    private Arena map;
    private Player player;
    private ArrayList<Enemy> enemies;
    private GameUI ui;

    // Game State
    private boolean isRunning = false;
    private Timer gameLoop;
    private int currentLevel = 1;
    private Random random = new Random();

    // Constructor: Connects everything together
    public GameLogicController(Arena map, Player player, ArrayList<Enemy> enemies, GameUI ui) {
        this.map = map;
        this.player = player;
        this.enemies = enemies;
        this.ui = ui;

        // Set game speed (e.g., 50ms delay = 20 frames per second)
        this.gameLoop = new Timer(50, this);
    }

    public void startGame() {
        isRunning = true;
        gameLoop.start();
    }

    // --- THE MAIN LOOP (Runs every tick) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isRunning)
            return;

        updatePositions();
        checkCollisions();
        checkGameState();

        // Tell UI to redraw everything after logic updates
        ui.repaint();
    }

    // --- STEP 1: MOVEMENT & AI ---
    private void updatePositions() {
        // Move Player
        player.move();

        // Move Enemies (Using central AI Logic)
        for (Enemy enemy : enemies) {
            processEnemyAI(enemy); // DECIDE direction
            enemy.move(); // APPLY movement
        }
    }

    // --- AI LOGIC HUB ---
    private void processEnemyAI(Enemy enemy) {
        String name = enemy.getName();

        if (name.contains("Koura")) {
            // EASY: Erratic, Random
            makeRandomMove(enemy);
        } else if (name.contains("Sark")) {
            // MEDIUM: Predictable, Chases Player
            makeChaseMove(enemy);
        } else if (name.contains("Rinzler")) {
            // HARD: Silent Hunter (Predicts where player will be)
            makeInterceptMove(enemy, 5); // Look 5 steps ahead
        } else if (name.contains("Clu")) {
            // IMPOSSIBLE: Aggressive, Cuts off player
            makeInterceptMove(enemy, 10); // Look 10 steps ahead
        } else {
            makeSafeMove(enemy); // Default fallback
        }
    }

    // AI BEHAVIOR 1: Random (Koura)
    private void makeRandomMove(Enemy enemy) {
        // 20% chance to change direction randomly
        if (random.nextInt(100) < 20) {
            pickRandomSafeDirection(enemy);
        } else {
            // Otherwise keep going straight if safe
            if (!isMoveSafe(enemy, enemy.getDirection())) {
                pickRandomSafeDirection(enemy);
            }
        }
    }

    // AI BEHAVIOR 2: Chase (Sark)
    private void makeChaseMove(Enemy enemy) {
        // Try to reduce distance to player
        int dx = player.getX() - enemy.getX();
        int dy = player.getY() - enemy.getY();

        // Prefer moving along the larger distance axis
        String preferredDir;
        if (Math.abs(dx) > Math.abs(dy)) {
            preferredDir = (dx > 0) ? "RIGHT" : "LEFT";
        } else {
            preferredDir = (dy > 0) ? "DOWN" : "UP";
        }

        // Try preferred, if blocked, try random safe
        if (isMoveSafe(enemy, preferredDir)) {
            enemy.setDirection(preferredDir);
        } else {
            pickRandomSafeDirection(enemy);
        }
    }

    // AI BEHAVIOR 3: Intercept (Rinzler/Clu)
    private void makeInterceptMove(Enemy enemy, int predictionSteps) {
        // Predict where player is GOING, not where they ARE
        int targetX = player.getX();
        int targetY = player.getY();

        // Simple prediction based on player current direction
        String pDir = player.getDirection();
        if (pDir.equals("UP"))
            targetY -= predictionSteps;
        if (pDir.equals("DOWN"))
            targetY += predictionSteps;
        if (pDir.equals("LEFT"))
            targetX -= predictionSteps;
        if (pDir.equals("RIGHT"))
            targetX += predictionSteps;

        // Navigate towards predicted spot
        int dx = targetX - enemy.getX();
        int dy = targetY - enemy.getY();

        String bestDir = (Math.abs(dx) > Math.abs(dy)) ? ((dx > 0) ? "RIGHT" : "LEFT") : ((dy > 0) ? "DOWN" : "UP");

        if (isMoveSafe(enemy, bestDir)) {
            enemy.setDirection(bestDir);
        } else {
            // If primary path blocked, try to "Cut off" by choosing perpendicular safe path
            pickRandomSafeDirection(enemy);
        }
    }

    // Helper: Check if a move results in immediate death
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

        // Check Walls and Trails
        return !map.isWall(nextX, nextY) && !map.isTrail(nextX, nextY);
    }

    // Helper: Find any valid move to survive
    private void pickRandomSafeDirection(Enemy enemy) {
        String[] dirsArray = { "UP", "DOWN", "LEFT", "RIGHT" };
        List<String> dirs = Arrays.asList(dirsArray);

        // Shuffle the list to try directions in random order WITHOUT repetition
        Collections.shuffle(dirs);

        for (String tryDir : dirs) {
            if (isMoveSafe(enemy, tryDir)) {
                enemy.setDirection(tryDir);
                return;
            }
        }
    }

    // Default fallback if a specific strategy fails
    private void makeSafeMove(Enemy enemy) {
        if (!isMoveSafe(enemy, enemy.getDirection())) {
            pickRandomSafeDirection(enemy);
        }
    }

    // --- STEP 2: COLLISIONS ---
    private void checkCollisions() {
        // 1. Check Wall Collisions
        if (map.isWall(player.getX(), player.getY())) {
            handleDamage(player, 0.5);
            resetPositions();
        }

        // 2. Check Trail (Jetwall) Collisions
        if (map.isTrail(player.getX(), player.getY())) {
            handleDamage(player, 0.5);
        }

        // 3. Check Head-on Collisions with Enemies
        for (Enemy enemy : enemies) {
            if (player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                handleDamage(player, 1.0);
                handleDamage(enemy, 1.0);
            }
        }
    }

    // Helper to handle damage and check for death
    private void handleDamage(GameCharacter character, double damage) {
        character.reduceLives(damage);
        ui.showStatusMessage(character.getName() + " took " + damage + " damage!");

        if (character.getLives() <= 0) {
            if (character instanceof Player) {
                gameOver();
            } else if (character instanceof Enemy) {
                character.setAlive(false);
                player.addXP(100);
                ui.showStatusMessage("Enemy Derezzed! +100 XP");
            }
        }
    }

    // --- STEP 3: GAME STATE ---
    private void checkGameState() {
        enemies.removeIf(e -> !e.isAlive());

        if (enemies.isEmpty()) {
            levelUp();
        }
    }

    private void levelUp() {
        gameLoop.stop();
        currentLevel++;
        player.addXP(500);
        ui.showStatusMessage("Level " + currentLevel + " Complete!");
        startGame();
    }

    private void gameOver() {
        isRunning = false;
        gameLoop.stop();
        ui.showGameOverScreen(player.getXP());
    }

    private void resetPositions() {
        player.setX(50);
        player.setY(50);
    }
}