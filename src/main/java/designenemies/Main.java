package designenemies;

import java.util.*;
import arena.ArenaOne; // Assuming ArenaOne is the test map
import arena.Arena; // Import the base Arena class

public class Main {

    public static void main(String[] args) {
        // 1. Load Arena and get the grid
        Arena arena = new ArenaOne(); // Use ArenaOne for testing
        char[][] arenaGrid = arena.getGrid();
        
        // Load all enemies from enemies.txt
        List<Enemy> allEnemies = EnemyLoader.loadEnemies("data/enemies.txt");

        if (allEnemies.isEmpty()) {
            System.out.println("No enemies loaded!");
            return;
        }
       
        System.out.println("=== ENEMIES LOADED FROM FILE ===");
        allEnemies.forEach(e -> System.out.println(e.name));

        List<Enemy> spawnedEnemies = new ArrayList<>();

        // 2. Spawn 7 random enemies, set up their context
        System.out.println("\n=== SPAWNING 7 ENEMIES ===");

        Random r = new Random();

        for (int i = 0; i < 7; i++) {
            Enemy base = allEnemies.get(r.nextInt(allEnemies.size()));
            
            if (base == null) {
                i--; 
                continue;
            }

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
            spawn.currentDirection = designenemies.Direction.values()[r.nextInt(4)];
            
            // IMPORTANT: Give the enemy the arena context
            spawn.setArenaGrid(arenaGrid);
            
            spawnedEnemies.add(spawn);

            System.out.println(spawn + ", Initial Dir: " + spawn.currentDirection);
        }
        
        // 3. Simulation (5 steps of movement)
        System.out.println("\n=== SIMULATING 5 MOVES ===");
        
        for (int step = 1; step <= 5; step++) {
            System.out.println("\n--- STEP " + step + " ---");
            
            for (Enemy enemy : spawnedEnemies) {
                // Determine the next move using the AI logic
                Direction nextMove = enemy.decideMove();
                
                // Apply the move (updates x, y, and currentDirection)
                enemy.applyMove(nextMove);
                
                // Print the result
                System.out.println(enemy.name + " moved " + nextMove + " to " + enemy.toString());
            }
        }
    }
}