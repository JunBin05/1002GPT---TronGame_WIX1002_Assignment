package XPSystem;

public class TronRules {
    // 1. CONSTANTS
    private static final double BASE_XP = 100.0;
    private static final double EXPONENT = 2.2;
    public static final int MAX_LEVEL = 99;
    public static final int MAX_DISCS = 10;

    // 2. ENUMS
    public enum EnemyType {
        MINION, KOURA, SARK, RINZLER, CLU
    }

    public enum StageType {
        TUTORIAL, NORMAL, BOSS_KILL, BOSS_SURVIVE, STORY_CLIMAX
    }

    // 3. BASE XP CALCULATION
    public static long getTotalXpForLevel(int level) {
        if (level <= 1)
            return 0;
        return (long) (BASE_XP * Math.pow(level, EXPONENT));
    }

    public static int getDiscCount(int level) {
        return Math.min(1 + (level / 10), MAX_DISCS);
    }

    // 4. ENEMY XP (UPDATED FOR LEVEL 50 PACING)
    public static long calculateEnemyXp(int currentLevel, EnemyType enemy) {
        // Calculate the "Gap"
        long currentTotal = getTotalXpForLevel(currentLevel);
        long nextTotal = getTotalXpForLevel(currentLevel + 1);
        long gap = nextTotal - currentTotal;

        double multiplier = 0;
        switch (enemy) {
            case MINION:
                multiplier = 0.15;
                break; // 15% (Was 40%)
            case KOURA:
                multiplier = 1.00;
                break; // +1 Level (Was 3.0)
            case SARK:
                multiplier = 1.50;
                break; // +1.5 Levels (Was 4.0)
            case RINZLER:
                multiplier = 2.00;
                break; // +2 Levels (Was 5.0)
            case CLU:
                multiplier = 3.00;
                break; // +3 Levels (Was 8.0)
        }

        return (long) (gap * multiplier);
    }

    // 5. STAGE REWARD (UPDATED FOR LEVEL 50 PACING)
    public static long calculateStageReward(int currentLevel, StageType type) {
        long currentTotal = getTotalXpForLevel(currentLevel);
        long nextTotal = getTotalXpForLevel(currentLevel + 1);
        long gap = nextTotal - currentTotal;

        double multiplier = 0;
        switch (type) {
            case TUTORIAL:
                multiplier = 1.0;
                break; // Start Lvl 2 (Was 2.0)
            case NORMAL:
                multiplier = 1.2;
                break; // +1.2 Levels (Was 3.5)
            case BOSS_KILL:
                multiplier = 2.5;
                break; // +2.5 Levels (Was 5.0)
            case BOSS_SURVIVE:
                multiplier = 3.0;
                break; // +3.0 Levels (Was 6.0)
            case STORY_CLIMAX:
                multiplier = 4.0;
                break; // +4.0 Levels (Was 8.0)
        }

        return (long) (gap * multiplier);
    }
}