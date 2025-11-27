package designenemies;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // Load all enemies from enemies.txt
        List<Enemy> allEnemies = EnemyLoader.loadEnemies("enemies.txt");

        // Check if any enemies were successfully loaded
        if (allEnemies.isEmpty()) {
            System.out.println("No enemies loaded!");
            return;
        }
       
        // Print all loaded enemy types to confirm
        System.out.println("=== ENEMIES LOADED FROM FILE ===");
        allEnemies.forEach(e -> System.out.println(e.name));

        // Spawn 7 random enemies for this match
        System.out.println("\n=== SPAWNING 7 ENEMIES ===");

        Random r = new Random();

        for (int i = 0; i < 7; i++) {
            //Pick a random enemy from the loaded list
            Enemy base = allEnemies.get(r.nextInt(allEnemies.size()));
            
            // Null check: If somehow base is null, retry this iteration
            if (base == null) {
            i--; // decrement i to try again
            continue; // skip this iteration
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

            // Assign a random position for arena 40x40
            spawn.spawnRandom(40, 40);

            //Print the spawned enemy and its position
            System.out.println(spawn);
        }
    }
}
