package designenemies;

import java.awt.BorderLayout;   
import java.awt.Color;          // FIX: Added Color import
import java.awt.GridLayout;     
import java.util.*;
import javax.swing.JFrame; 
import javax.swing.JPanel; 
import javax.swing.ImageIcon;

// Imports from your other packages:
import arena.Arena; 
import arena.ArenaLoader; 
import characters.Direction; 
import characters.Character; 
import characters.Tron; 
import characters.Kevin;
import characters.CharacterLoader; 
import characters.CharacterData; 
import controller.GameController;

public class Main {

    public static void main(String[] args) {
        
        // --- 1. GAME SETUP (Load Arena, Icons, Frame) ---
        
        Color NEON_PURPLE_BG = new Color(15, 0, 40); 
        
        Arena arena = ArenaLoader.loadArena(1); 
        JFrame frame = new JFrame("Tron Light Cycle Arena");
        frame.getContentPane().setBackground(NEON_PURPLE_BG);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 850);
        frame.setLayout(new BorderLayout()); 

        Map<String, ImageIcon> icons = ArenaLoader.loadAllIcons(frame); // FIX: ArenaLoader visibility assumed fixed
        
        // --- 2. CHARACTER SETUP (Player and Enemies) ---
        
        List<Character> cycles = new ArrayList<>();
        
        // A. Load Player Character (Tron)
        CharacterData tronData = CharacterLoader.loadCharacterData("Tron");
        Tron playerTron = new Tron();
        if (tronData != null) playerTron.loadInitialAttributes(tronData);
        playerTron.r = 20; playerTron.c = 15; 
        playerTron.currentDirection = characters.Direction.EAST; // FIX: Use characters.Direction
        cycles.add(playerTron); 
        
        // B. Load Enemy Characters (using your existing logic)
        List<Enemy> allEnemies = EnemyLoader.loadEnemies("data/enemies.txt");
        if (allEnemies.isEmpty()) {
            System.out.println("No enemies loaded!");
            return;
        }
       
        System.out.println("=== ENEMIES LOADED FROM FILE ===");
        allEnemies.forEach(e -> System.out.println(e.name));

        Random r = new Random();
        int enemiesToSpawn = 7; 

        for (int i = 0; i < enemiesToSpawn; i++) {
            Enemy base = allEnemies.get(r.nextInt(allEnemies.size()));
            
            // Create a new enemy object based on its type
            Enemy spawn;
            if (base instanceof Clu) spawn = new Clu(new String[]{
                    base.name, base.color, base.difficulty, String.valueOf(base.xp),
                    base.speed, base.handling, base.intelligence, base.description});
            else if (base instanceof Rinzler) spawn = new Rinzler(new String[]{
                    base.name, base.color, base.difficulty, String.valueOf(base.xp),
                    base.speed, base.handling, base.intelligence, base.description});
            else if (base instanceof Sark) spawn = new Sark(new String[]{
                    base.name, base.color, base.difficulty, String.valueOf(base.xp),
                    base.speed, base.handling, base.intelligence, base.description});
            else spawn = new Koura(new String[]{
                    base.name, base.color, base.difficulty, String.valueOf(base.xp),
                    base.speed, base.handling, base.intelligence, base.description});

            // Assign a random position and direction
            spawn.spawnRandom(40, 40);
            spawn.currentDirection = characters.Direction.values()[r.nextInt(4)]; // FIX: Use characters.Direction
            
            // IMPORTANT: Give the enemy the arena context
            spawn.setArenaGrid(arena.getGrid());
            
            cycles.add(spawn); 
            System.out.println("Spawned: " + spawn + ", Initial Dir: " + spawn.currentDirection);
        }
        
        // --- 3. UI SETUP and Game Start ---
        
        JPanel arenaPanel = new JPanel(new GridLayout(40, 40)); 
        JPanel hudPanel = ArenaLoader.createHUDPanel(playerTron); 
        
        frame.add(hudPanel, BorderLayout.NORTH);
        frame.add(arenaPanel, BorderLayout.CENTER);
        
        frame.setVisible(true);

        // 4. SHOW MAIN MENU (StartGameMenu must be implemented and imported)
        // StartGameMenu.showMenu(frame); 
        
        // 5. START GAME LOOP
        System.out.println("Starting Game Simulation...");
        
        GameController controller = new GameController(frame, arena, cycles, icons, arenaPanel, hudPanel); 
        
        new Thread(controller).start();
    }
}