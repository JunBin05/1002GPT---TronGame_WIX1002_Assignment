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
                    } else if (type.equals("Random_Clu_Koura")) {
                        finalType = rand.nextBoolean() ? "Clu" : "Koura";
                    } else if (type.equals("Random_Koura_Sark")) {
                        finalType = rand.nextBoolean() ? "Koura" : "Sark";
                    } else if (type.equals("Random_Except_Rinzler")) {
                        int r = rand.nextInt(3);
                        if (r == 0)
                            finalType = "Clu";
                        else if (r == 1)
                            finalType = "Sark";
                        else
                            finalType = "Koura";
                    } else if (type.equals("Random_Except_Clu")) {
                        int r = rand.nextInt(3);
                        if (r == 0)
                            finalType = "Rinzler";
                        else if (r == 1)
                            finalType = "Sark";
                        else
                            finalType = "Koura";
                    } else if (type.equals("Random_All")) {
                        int r = rand.nextInt(4);
                        if (r == 0)
                            finalType = "Clu";
                        else if (r == 1)
                            finalType = "Sark";
                        else if (r == 2)
                            finalType = "Rinzler";
                        else
                            finalType = "Koura";
                    }

                    // Create Minion (isBoss = false)
                    Enemy enemy = createEnemy(finalType, false);
                    if (enemy != null) {
                        // Use per-enemy, per-tier tuning from data file
                        EnemyLoader.EnemyStats stats = EnemyLoader.getStats(finalType, false);
                        int tier = (chapter <= 2) ? 0 : (chapter <= 4) ? 1 : 2;

                        double minionLives = (stats != null) ? stats.getTierHp(tier) : 1.0;
                        enemy.setMaxLives(minionLives);
                        enemy.setLives(minionLives);

                        double speed = (stats != null) ? stats.getTierSpeed(tier) : 0.33;
                        double handling = (stats != null) ? stats.getTierHandling(tier) : 0.70;
                        double aggression = (stats != null) ? stats.getTierAggression(tier) : 0.20;
                        int trail = (stats != null) ? stats.getTierTrail(tier) : 7;

                        final int PLAYER_BASE_DELAY = 200;
                        final int PLAYER_SPEED_MULTIPLIER = 100;
                        final int GLOBAL_MIN_DELAY = 50; // safety floor in ms
                        int delayMs = PLAYER_BASE_DELAY - (int) Math.round((speed - 1.0) * PLAYER_SPEED_MULTIPLIER);
                        if (delayMs < GLOBAL_MIN_DELAY)
                            delayMs = GLOBAL_MIN_DELAY; // clamp to global minimum

                        enemy.setSpeed(speed);
                        enemy.setHandling(handling);
                        enemy.setAggression(aggression);
                        enemy.setMoveDelayMs(delayMs); // new time-based scheduling
                        enemy.setTrailDuration(trail);

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
                    // Use per-enemy, per-tier tuning from data file
                    EnemyLoader.EnemyStats stats = EnemyLoader.getStats(config.bossName, true);
                    int tier = (chapter <= 2) ? 0 : (chapter <= 4) ? 1 : 2;

                    double bossLives = (stats != null) ? stats.getTierHp(tier) : 2.0;
                    boss.setMaxLives(bossLives);
                    boss.setLives(bossLives);

                    double speed = (stats != null) ? stats.getTierSpeed(tier) : 0.45;
                    double handling = (stats != null) ? stats.getTierHandling(tier) : 0.75;
                    double aggression = (stats != null) ? stats.getTierAggression(tier) : 0.6;
                    int trail = (stats != null) ? stats.getTierTrail(tier) : 10;

                    final int PLAYER_BASE_DELAY = 200;
                    final int PLAYER_SPEED_MULTIPLIER = 100;
                    final int GLOBAL_MIN_DELAY = 50; // safety floor in ms
                    int delayMs = PLAYER_BASE_DELAY - (int) Math.round((speed - 1.0) * PLAYER_SPEED_MULTIPLIER);
                    if (delayMs < GLOBAL_MIN_DELAY)
                        delayMs = GLOBAL_MIN_DELAY;

                    boss.setSpeed(speed);
                    boss.setHandling(handling);
                    boss.setAggression(aggression);
                    boss.setMoveDelayMs(delayMs);
                    boss.setTrailDuration(trail);

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

        // CHAPTER 1: All Clu Minions
        if (chapter == 1) {
            if (stage == 1)
                config.addEnemyType("Clu", 2); // Tutorial
            else if (stage == 2)
                config.addEnemyType("Clu", 4);
            else if (stage == 3)
                config.addEnemyType("Clu", 4);
        }

        // CHAPTER 2: Clu & Sark Mix
        else if (chapter == 2) {
            if (stage <= 4) {
                // "Randomly generate" Clu & Sark (Total 3)
                // We use a special key "Random_Clu_Sark" handled in loadStage
                config.addEnemyType("Random_Clu_Sark", 3);
            } else if (stage == 5) {
                config.addEnemyType("Random_Clu_Sark", 3); // 3 Minions
                config.hasBoss = true;
                config.bossName = "Koura"; // Koura Boss
            } else if (stage == 6) {
                config.addEnemyType("Random_Clu_Sark", 3);
                config.hasBoss = true;
                config.bossName = "Sark"; // Sark Boss
            }
        }

        // CHAPTER 3: Clu, Sark, Koura
        else if (chapter == 3) {
            if (stage == 1) {
                config.addEnemyType("Random_Except_Rinzler", 7);
            } // 7 Mixed Minions
            else if (stage == 2) {
                config.addEnemyType("Random_Clu_Koura", 2);
                config.hasBoss = true;
                config.bossName = "Sark";
            } else if (stage == 3) {
                config.addEnemyType("Random_Except_Rinzler", 7);
            } else if (stage == 4) {
                config.addEnemyType("Random_Except_Rinzler", 3);
            } else if (stage == 5) {
                config.addEnemyType("Random_Koura_Sark", 2);
                config.hasBoss = true;
                config.bossName = "Clu";
            }
        }

        // CHAPTER 4
        else if (chapter == 4) {
            if (stage == 1) {
                config.addEnemyType("Random_Except_Rinzler", 2);
                config.hasBoss = true;
                config.bossName = "Rinzler";
            } else if (stage == 2) {
                config.addEnemyType("Random_All", 5);
            } else if (stage == 3) {
                config.addEnemyType("Random_All", 5);
            } else if (stage == 4) {
                config.addEnemyType("Random_All", 3);
            } else if (stage == 5) {
                config.addEnemyType("Random_Except_Rinzler", 2);
                config.hasBoss = true;
                config.bossName = "Rinzler";
            }
        }

        // CHAPTER 5
        else if (chapter == 5) {
            if (stage == 1) {
                config.addEnemyType("Random_All", 6);
            } else if (stage == 2) {
                config.addEnemyType("Random_All", 3);
            } else if (stage == 3) {
                config.addEnemyType("Random_All", 7);
            } else if (stage == 4) {
                config.addEnemyType("Random_Except_Clu", 2);
                config.hasBoss = true;
                config.bossName = "Clu";
            }
        }

        return config;
    }
}