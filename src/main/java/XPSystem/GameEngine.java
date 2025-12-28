package XPSystem;

import java.util.HashMap;
import java.util.Map;

public class GameEngine {
    private Map<String, CharacterProfile> characters = new HashMap<>();
    private CharacterProfile activeCharacter;
    private boolean isEndlessMode = false;

    public GameEngine() {
        characters.put("Kevin", new CharacterProfile("Kevin"));
        characters.put("Tron", new CharacterProfile("Tron"));
        activeCharacter = characters.get("Tron"); // Default
    }

    public void setGameMode(String mode) {
        this.isEndlessMode = mode.equalsIgnoreCase("ENDLESS");
    }

    // --- UPDATED SWITCH LOGIC ---
    public void switchCharacter(String name) {
        // 1. Check if Tron is dead
        if (name.equalsIgnoreCase("Tron") && !GlobalState.isTronPlayable()) {
            System.out.println("[ACCESS DENIED] User TRON not found. (Status: DEREZZED)");
            return;
        }

        // 2. Allow Switch
        if (characters.containsKey(name)) {
            this.activeCharacter = characters.get(name);
            System.out.println("\n[SYSTEM] Switched Identity to: " + name.toUpperCase());
        } else {
            System.out.println("[ERROR] User not found.");
        }
    }

    public void stageCleared(int stageId, TronRules.StageType type) {
        if (stageId > GlobalState.maxStageCleared) {
            GlobalState.maxStageCleared = stageId;
        }

        // XP Logic First (So the active character gets the stage reward)
        if (!isEndlessMode) {
            long xp = TronRules.calculateStageReward(activeCharacter.getLevel(), type);
            activeCharacter.addXp(xp);
            System.out.println("Stage " + stageId + " Cleared. Reward: " + xp + " XP");
        }

        // --- STAGE 13 EVENT LOGIC (UPDATED) ---
        if (type == TronRules.StageType.STORY_CLIMAX) {
            System.out.println("\n#############################################");
            System.out.println("### CRITICAL EVENT: TRON SACRIFICES HIMSELF ###");
            System.out.println("#############################################");

            GlobalState.killTron();
            CharacterProfile tron = characters.get("Tron");
            CharacterProfile kevin = characters.get("Kevin");

            // LOGIC: Regardless of who played, Kevin must inherit the best stats
            // If Player played as Tron: Kevin inherits Tron's high stats.
            // If Player played as Kevin: Kevin keeps his stats (he is already high level).

            if (kevin.getXp() < tron.getXp()) {
                long difference = tron.getXp() - kevin.getXp();
                kevin.addXp(difference);
                System.out.println(
                        ">>> DATA SYNC: Kevin inherited Tron's System Data (Level " + kevin.getLevel() + ") <<<");
            }

            // Force switch to Kevin if not already
            this.activeCharacter = kevin;
            System.out.println("[SYSTEM] TRON is gone. Control locked to KEVIN.");
        }
    }

    public void enemyKilled(TronRules.EnemyType type) {
        // Per-kill XP disabled: no XP is granted when killing individual enemies.
        System.out.println(activeCharacter.getName() + " killed " + type + " (per-kill XP disabled)");
    }

    public CharacterProfile getActiveCharacter() {
        return activeCharacter;
    }
}