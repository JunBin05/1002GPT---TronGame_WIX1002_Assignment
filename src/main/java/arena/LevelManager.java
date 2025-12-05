package arena;

import designenemies.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import characters.Character;

public class LevelManager {

    private static Random rand = new Random();

    public static List<Character> loadStage(int chapter, int stage, char[][] grid) {
        List<Character> enemies = new ArrayList<>();
        LevelConfig config = getLevelConfig(chapter, stage);

        if (config != null) {
            // 1. SPAWN MINIONS
            for (String type : config.enemyTypes.keySet()) {
                int count = config.enemyTypes.get(type);
                for (int i = 0; i < count; i++) {
                    // Random Logic for "Mix" types
                    String finalType = type;
                    if (type.equals("Random_Clu_Sark")) {
                        finalType = rand.nextBoolean() ? "Clu" : "Sark";
                    }
                    else if (type.equals("Random_All")) {
                        int r = rand.nextInt(3);
                        if(r==0) finalType="Clu"; else if(r==1) finalType="Sark"; else finalType="Koura";
                    }

                    // Create Minion (isBoss = false)
                    Enemy enemy = createEnemy(finalType, false); 
                    if (enemy != null) {
                        enemy.setArenaGrid(grid);
                        enemy.spawnRandom(40, 40);
                        enemies.add(enemy);
                    }
                }
            }

            // 2. SPAWN BOSS
            if (config.hasBoss) {
                // Create Boss (isBoss = true)
                Enemy boss = createEnemy(config.bossName, true); 
                if (boss != null) {
                    boss.setArenaGrid(grid);
                    boss.spawnRandom(40, 40);
                    enemies.add(boss);
                }
            }
        }
        return enemies;
    }

    private static Enemy createEnemy(String type, boolean isBoss) {
        return switch (type) {
            case "Clu" -> new Clu(isBoss);
            case "Sark" -> new Sark(isBoss);
            case "Rinzler" -> new Rinzler(isBoss);
            case "Koura" -> new Koura(isBoss);
            default -> new Koura(false); 
        };
    }

    private static LevelConfig getLevelConfig(int chapter, int stage) {
        LevelConfig config = new LevelConfig();

        // --- CHAPTER 1: All Clu Minions ---
        if (chapter == 1) {
            if (stage == 1) config.addEnemyType("Clu", 2); // Tutorial
            else if (stage == 2) config.addEnemyType("Clu", 4);
            else if (stage == 3) config.addEnemyType("Clu", 4);
        }
        
        // --- CHAPTER 2: Clu & Sark Mix ---
        else if (chapter == 2) {
            if (stage <= 4) {
                // "Randomly generate" Clu & Sark (Total 3)
                // We use a special key "Random_Clu_Sark" handled in loadStage
                config.addEnemyType("Random_Clu_Sark", 3);
            } 
            else if (stage == 5) {
                config.addEnemyType("Clu", 3); // 3 Minions
                config.hasBoss = true; config.bossName = "Koura"; // Koura Boss
            } 
            else if (stage == 6) {
                config.addEnemyType("Clu", 3); 
                config.hasBoss = true; config.bossName = "Sark"; // Sark Boss
            }
        }
        
        // --- CHAPTER 3: Clu, Sark, Koura ---
        else if (chapter == 3) {
            if (stage == 1) { config.addEnemyType("Random_All", 7); } // 7 Mixed Minions
            else if (stage == 2) { config.addEnemyType("Random_All", 2); config.hasBoss = true; config.bossName = "Sark"; }
            else if (stage == 3) { config.addEnemyType("Clu", 7); }
            else if (stage == 4) { config.addEnemyType("Sark", 3); }
            else if (stage == 5) { config.addEnemyType("Koura", 2); config.hasBoss = true; config.bossName = "Clu"; }
        }

        // --- CHAPTER 4 ---
        else if (chapter == 4) {
            if (stage == 1) { config.addEnemyType("Sark", 2); config.hasBoss = true; config.bossName = "Rinzler"; }
            else if (stage == 2) { config.addEnemyType("Koura", 5); }
            else if (stage == 3) { config.addEnemyType("Clu", 5); }
            else if (stage == 4) { config.addEnemyType("Rinzler", 3); } // Rinzler Minions
            else if (stage == 5) { config.addEnemyType("Sark", 2); }
        }

        // --- CHAPTER 5 ---
        else if (chapter == 5) {
            if (stage == 1) { config.addEnemyType("Rinzler", 6); }
            else if (stage == 2) { config.addEnemyType("Clu", 3); }
            else if (stage == 3) { config.addEnemyType("Sark", 7); }
            else if (stage == 4) { config.addEnemyType("Rinzler", 2); config.hasBoss = true; config.bossName = "Clu"; }
        }

        return config;
    }
}