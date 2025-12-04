package XPSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TronFullStory {

    // --- 1. THE RULES & MATH ---
    static class TronRules {
        static final double BASE_XP = 100.0;
        static final double EXPONENT = 2.2;
        static final int MAX_LEVEL = 99;
        static final int MAX_DISCS = 10;

        enum EnemyType {
            NONE, MINION, KOURA, SARK, RINZLER, CLU
        }

        enum StageType {
            TUTORIAL, NORMAL, BOSS_KILL, BOSS_SURVIVE, STORY_CLIMAX
        }

        static long getTotalXpForLevel(int level) {
            if (level <= 1)
                return 0;
            return (long) (BASE_XP * Math.pow(level, EXPONENT));
        }

        static int getDiscCount(int level) {
            return Math.min(1 + (level / 10), MAX_DISCS);
        }

        // Percentage based XP (Scales with Level)
        static long calculateEnemyXp(int currentLevel, EnemyType enemy) {
            long gap = getTotalXpForLevel(currentLevel + 1) - getTotalXpForLevel(currentLevel);
            double multiplier = 0;
            switch (enemy) {
                case MINION:
                    multiplier = 0.40;
                    break; // High reward for "Arcade" feel
                case KOURA:
                    multiplier = 3.00;
                    break; // +3 Levels
                case SARK:
                    multiplier = 4.00;
                    break;
                case RINZLER:
                    multiplier = 5.00;
                    break;
                case CLU:
                    multiplier = 8.00;
                    break;
            }
            return (long) (gap * multiplier);
        }

        static long calculateStageReward(int currentLevel, StageType type) {
            long gap = getTotalXpForLevel(currentLevel + 1) - getTotalXpForLevel(currentLevel);
            double multiplier = 0;
            switch (type) {
                case TUTORIAL:
                    multiplier = 2.0;
                    break;
                case NORMAL:
                    multiplier = 3.5;
                    break;
                case BOSS_KILL:
                    multiplier = 5.0;
                    break;
                case BOSS_SURVIVE:
                    multiplier = 6.0;
                    break;
                case STORY_CLIMAX:
                    multiplier = 8.0;
                    break; // Massive legacy dump
            }
            return (long) (gap * multiplier);
        }
    }

    // --- 2. THE PLAYER PROFILE ---
    static class CharacterProfile {
        String name;
        int level = 1;
        long currentXp = 0;
        int discCount = 1;

        CharacterProfile(String name) {
            this.name = name;
        }

        void addXp(long amount) {
            this.currentXp += amount;
            while (level < TronRules.MAX_LEVEL && currentXp >= TronRules.getTotalXpForLevel(level + 1)) {
                level++;
                discCount = TronRules.getDiscCount(level);
            }
        }
    }

    // --- 3. THE ENGINE ---
    static class GameEngine {
        Map<String, CharacterProfile> characters = new HashMap<>();
        CharacterProfile activeCharacter;
        boolean tronAlive = true;

        GameEngine() {
            characters.put("Kevin", new CharacterProfile("Kevin"));
            characters.put("Tron", new CharacterProfile("Tron"));
            activeCharacter = characters.get("Tron"); // Start as Tron
        }

        void processStage(StageConfig stage) {
            System.out.println("\n--- STARTING STAGE " + stage.id + " (" + activeCharacter.name + ") ---");

            // 1. KILL MINIONS
            for (int i = 0; i < stage.minionCount; i++) {
                long xp = TronRules.calculateEnemyXp(activeCharacter.level, TronRules.EnemyType.MINION);
                activeCharacter.addXp(xp);
            }
            if (stage.minionCount > 0)
                System.out.println("Defeated " + stage.minionCount + " Minions.");

            // 2. BOSS LOGIC
            if (stage.type == TronRules.StageType.BOSS_KILL && stage.bossType != TronRules.EnemyType.NONE) {
                long xp = TronRules.calculateEnemyXp(activeCharacter.level, stage.bossType);
                activeCharacter.addXp(xp);
                System.out.println("BOSS DEFEATED: " + stage.bossType);
            } else if (stage.type == TronRules.StageType.BOSS_SURVIVE
                    || stage.type == TronRules.StageType.STORY_CLIMAX) {
                System.out.println("BOSS SURVIVED/ESCAPED: " + stage.bossType + " (No Kill XP)");
            }

            // 3. STAGE CLEAR REWARD
            long stageXp = TronRules.calculateStageReward(activeCharacter.level, stage.type);
            activeCharacter.addXp(stageXp);
            System.out.println("Stage Clear Bonus Awarded.");

            // 4. PRINT STATUS
            System.out.printf("STATUS: %s is Level %d (Discs: %d)%n", activeCharacter.name, activeCharacter.level,
                    activeCharacter.discCount);

            // 5. CHECK FOR DEATH EVENT (Stage 13)
            if (stage.type == TronRules.StageType.STORY_CLIMAX) {
                triggerTronDeath();
            }
        }

        void triggerTronDeath() {
            System.out.println("\n#############################################");
            System.out.println("### EVENT: TRON SACRIFICES HIMSELF FOR KEVIN ###");
            System.out.println("#############################################");

            tronAlive = false;
            CharacterProfile tron = characters.get("Tron");
            CharacterProfile kevin = characters.get("Kevin");

            // INHERITANCE: Kevin takes Tron's exact XP to survive Stage 14
            long xpGap = tron.currentXp - kevin.currentXp;
            if (xpGap > 0) {
                kevin.addXp(xpGap);
                System.out.println("SYSTEM: Identity Disc Transfer Complete.");
                System.out.println("SYSTEM: Kevin inherited Level " + kevin.level);
            }

            activeCharacter = kevin; // FORCE SWITCH
        }
    }

    // --- 4. DATA CONFIG ---
    static class StageConfig {
        int id;
        TronRules.StageType type;
        int minionCount;
        TronRules.EnemyType bossType;

        StageConfig(int id, TronRules.StageType type, int minions, TronRules.EnemyType boss) {
            this.id = id;
            this.type = type;
            this.minionCount = minions;
            this.bossType = boss;
        }
    }

    // --- 5. RUN SIMULATION ---
    public static void main(String[] args) {
        GameEngine game = new GameEngine();
        List<StageConfig> story = new ArrayList<>();

        // --- CHAPTER 1 ---
        story.add(new StageConfig(1, TronRules.StageType.TUTORIAL, 2, TronRules.EnemyType.NONE));
        story.add(new StageConfig(2, TronRules.StageType.NORMAL, 4, TronRules.EnemyType.NONE));
        story.add(new StageConfig(3, TronRules.StageType.NORMAL, 4, TronRules.EnemyType.NONE));

        // --- CHAPTER 2 ---
        story.add(new StageConfig(4, TronRules.StageType.NORMAL, 3, TronRules.EnemyType.NONE));
        story.add(new StageConfig(5, TronRules.StageType.NORMAL, 3, TronRules.EnemyType.NONE));
        story.add(new StageConfig(6, TronRules.StageType.NORMAL, 3, TronRules.EnemyType.NONE));
        story.add(new StageConfig(7, TronRules.StageType.NORMAL, 3, TronRules.EnemyType.NONE));
        story.add(new StageConfig(8, TronRules.StageType.BOSS_KILL, 0, TronRules.EnemyType.KOURA));
        story.add(new StageConfig(9, TronRules.StageType.BOSS_SURVIVE, 0, TronRules.EnemyType.SARK));

        // --- CHAPTER 3 ---
        story.add(new StageConfig(10, TronRules.StageType.NORMAL, 7, TronRules.EnemyType.NONE));
        story.add(new StageConfig(11, TronRules.StageType.BOSS_KILL, 2, TronRules.EnemyType.SARK));
        story.add(new StageConfig(12, TronRules.StageType.NORMAL, 7, TronRules.EnemyType.NONE));
        // STAGE 13: THE TURNING POINT
        story.add(new StageConfig(13, TronRules.StageType.STORY_CLIMAX, 2, TronRules.EnemyType.CLU));

        // --- CHAPTER 4 (KEVIN TAKES OVER) ---
        story.add(new StageConfig(14, TronRules.StageType.BOSS_KILL, 2, TronRules.EnemyType.RINZLER));
        story.add(new StageConfig(15, TronRules.StageType.NORMAL, 5, TronRules.EnemyType.NONE));
        story.add(new StageConfig(16, TronRules.StageType.NORMAL, 5, TronRules.EnemyType.NONE));
        story.add(new StageConfig(17, TronRules.StageType.NORMAL, 3, TronRules.EnemyType.NONE));
        story.add(new StageConfig(18, TronRules.StageType.NORMAL, 2, TronRules.EnemyType.NONE));

        // --- CHAPTER 5 ---
        story.add(new StageConfig(19, TronRules.StageType.NORMAL, 6, TronRules.EnemyType.NONE));
        story.add(new StageConfig(20, TronRules.StageType.NORMAL, 3, TronRules.EnemyType.NONE));
        story.add(new StageConfig(21, TronRules.StageType.NORMAL, 7, TronRules.EnemyType.NONE));
        story.add(new StageConfig(22, TronRules.StageType.BOSS_KILL, 2, TronRules.EnemyType.CLU));

        // RUN LOOP
        for (StageConfig stage : story) {
            game.processStage(stage);
        }

        System.out.println("\n--- GAME OVER ---");
        System.out.println("Final Kevin Level: " + game.activeCharacter.level);
    }
}
