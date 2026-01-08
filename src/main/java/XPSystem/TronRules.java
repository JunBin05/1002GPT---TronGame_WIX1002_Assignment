package XPSystem;

public class TronRules {
    // 1. CONSTANTS
    private static final double BASE_XP = 100.0;
    private static final double EXPONENT = 1.5; // eased further: faster early progression
    public static final int MAX_LEVEL = 99;
    public static final int MAX_DISCS = 10;

    // Stage-clear XP tuning (tweakable)
    private static final double BASE_STAGE_XP = 300.0; // base XP for stage clear (raised so C1S1 grants level)
    private static final double STAGE_MULTIPLIER_PER_STAGE = 0.12; // +12% per stage
    private static final double CHAPTER_MULTIPLIER_PER_CHAPTER = 0.28; // +28% per chapter (increased)

    // Progressive run scaling: when enabled, scale stage XP so that a single full run
    // (C1S1 -> C5S4) will reach TARGET_END_LEVEL (e.g., 99) gradually.
    private static final boolean PROGRESSIVE_SCALING_ENABLED = true;
    private static final int PROGRESS_TARGET_LEVEL = 99;
    private static Double progressionScaleCache = null; // computed lazily to avoid startup cost

    // 3. BASE XP CALCULATION
    public static long getTotalXpForLevel(int level) {
        if (level <= 1)
            return 0;
        return (long) (BASE_XP * Math.pow(level, EXPONENT));
    }

    /**
     * Stage-clear XP helper (stage-only XP mode). Calculates XP based on chapter & stage
     * and ignores the level "gap" curve so XP cannot be farmed by repeated kills.
     */
    private static double computeUnscaledStageXp(int chapter, int stage) {
        double stageMult = 1.0 + Math.max(0, stage - 1) * STAGE_MULTIPLIER_PER_STAGE;
        double chapterMult = 1.0 + Math.max(0, chapter - 1) * CHAPTER_MULTIPLIER_PER_CHAPTER;
        return BASE_STAGE_XP * stageMult * chapterMult;
    }

    private static double computeProgressionScale() {
        if (!PROGRESSIVE_SCALING_ENABLED) return 1.0;
        if (progressionScaleCache != null) return progressionScaleCache;

        // Stages per chapter: 1->3, 2->6, 3->5, 4->5, 5->4
        int[] stagesPerChapter = {0, 3, 6, 5, 5, 4};
        double totalUnscaled = 0.0;
        for (int c = 1; c <= 5; c++) {
            for (int s = 1; s <= stagesPerChapter[c]; s++) {
                totalUnscaled += computeUnscaledStageXp(c, s);
            }
        }
        double target = (double) getTotalXpForLevel(PROGRESS_TARGET_LEVEL);
        if (totalUnscaled <= 0.0) { progressionScaleCache = 1.0; return progressionScaleCache; }
        progressionScaleCache = target / totalUnscaled;
        System.out.println(String.format("[TronRules] Progression scaling: target=%,.0f totalUnscaled=%,.0f scale=%.4f", target, totalUnscaled, progressionScaleCache));
        return progressionScaleCache;
    }

    // -------- Replay diminishing returns configuration --------
    public static final double STAGE_REPLAY_DECAY_BASE = 0.87; // per-level decay multiplier
    public static final double STAGE_REPLAY_MIN_FACTOR = 0.05; // floor multiplier when very high level

    // --- FIXED PER-STAGE XP TABLE ---
    private static final int[] STAGES_PER_CHAPTER = {0, 3, 6, 5, 5, 4};
    private static final long[][] FIXED_STAGE_XP = new long[6][]; // index by chapter, stage (1-based)

    static {
        // Initialize with values computed from the existing formula, then scale to ensure guarantees
        double scale = computeProgressionScale();
        long total = 0;
        for (int c = 1; c <= 5; c++) {
            int stages = STAGES_PER_CHAPTER[c];
            FIXED_STAGE_XP[c] = new long[stages + 1];
            for (int s = 1; s <= stages; s++) {
                double base = computeUnscaledStageXp(c, s);
                long val = (long) Math.max(0, Math.round(base * scale));
                FIXED_STAGE_XP[c][s] = val;
                total += val;
            }
        }

        long requiredTotal = getTotalXpForLevel(MAX_LEVEL);
        if (total < requiredTotal) {
            // Scale uniformly so full run reaches at least requiredTotal
            double factor = (double) requiredTotal / (double) Math.max(1, total);
            long newTotal = 0;
            for (int c = 1; c <= 5; c++) {
                for (int s = 1; s <= STAGES_PER_CHAPTER[c]; s++) {
                    FIXED_STAGE_XP[c][s] = (long) Math.max(0, Math.round(FIXED_STAGE_XP[c][s] * factor));
                    newTotal += FIXED_STAGE_XP[c][s];
                }
            }
            total = newTotal;
        }

        // Ensure final stage (C5S4) can be replayed (with decay) to reach the target
        int finalC = 5, finalS = 4;
        double decay = STAGE_REPLAY_DECAY_BASE;
        double obtainableFromFinal = (double) FIXED_STAGE_XP[finalC][finalS] / (1.0 - decay);
        if (obtainableFromFinal < (double) requiredTotal) {
            long neededBase = (long) Math.ceil((double) requiredTotal * (1.0 - decay));
            if (neededBase > FIXED_STAGE_XP[finalC][finalS]) {
                FIXED_STAGE_XP[finalC][finalS] = neededBase;
                // Recompute total (not strictly necessary but keep for debug)
                long newTotal = 0;
                for (int c = 1; c <= 5; c++) for (int s = 1; s <= STAGES_PER_CHAPTER[c]; s++) newTotal += FIXED_STAGE_XP[c][s];
                total = newTotal;
            }
        }

        System.out.println(String.format("[TronRules] Fixed XP table initialized. Full-run total=%,d required=%,d, C5S4=%d (replay cap %.0f)", total, requiredTotal, FIXED_STAGE_XP[5][4], FIXED_STAGE_XP[5][4] / (1.0 - decay)));
    }

    public static long calculateStageClearXp(int chapter, int stage) {
        if (chapter < 1 || chapter > 5) return 0;
        if (stage < 1 || stage > STAGES_PER_CHAPTER[chapter]) return 0;
        return FIXED_STAGE_XP[chapter][stage];
    }

    /**
     * Compute the expected level a fresh player would have after completing up to
     * the specified chapter/stage using the current stage XP schedule and scaling.
     */
    public static int getExpectedLevelForStage(int chapter, int stage) {
        // Stages per chapter: 1->3, 2->6, 3->5, 4->5, 5->4
        int[] stagesPerChapter = {0, 3, 6, 5, 5, 4};
        long cumulative = 0;
        for (int c = 1; c <= chapter; c++) {
            int maxS = (c == chapter) ? stage : stagesPerChapter[c];
            for (int s = 1; s <= maxS; s++) {
                cumulative += calculateStageClearXp(c, s);
            }
        }
        int level = 1;
        while (level < MAX_LEVEL && cumulative >= getTotalXpForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    /**
     * Returns number of stages in the given chapter (1..5). Returns 0 if invalid.
     */
    public static int getStagesForChapter(int chapter) {
        if (chapter < 1 || chapter > 5) return 0;
        int[] stagesPerChapter = {0, 3, 6, 5, 5, 4};
        return stagesPerChapter[chapter];
    }

    /**
     * Calculate the diminishing multiplier for replaying a stage based on how far
     * above the stage's expected level the player's current level is.
     * Returns a value in (MIN_FACTOR..1.0]
     */
    public static double calculateReplayMultiplier(int playerLevel, int chapter, int stage) {
        int expected = getExpectedLevelForStage(chapter, stage);
        int diff = playerLevel - expected;
        if (diff <= 0) return 1.0;
        double mult = Math.pow(STAGE_REPLAY_DECAY_BASE, diff);
        if (mult < STAGE_REPLAY_MIN_FACTOR) mult = STAGE_REPLAY_MIN_FACTOR;
        return mult;
    }
}