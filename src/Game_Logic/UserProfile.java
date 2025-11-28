public class UserProfile {
    public int totalXP = 0;
    public int currentChapter = 1;
    public int currentLevel = 1;

    // Inventory
    public boolean hasSpeedBoost = false;
    public boolean hasWallBreaker = false;

    // Unlocks
    public boolean isCharacter2Unlocked = false;

    public void addXP(int amount) {
        this.totalXP += amount;
        checkUnlocks();
    }

    private void checkUnlocks() {
        if (this.totalXP > 1000) {
            this.hasSpeedBoost = true;
            System.out.println("UNLOCKED: Speed Boost!");
        }
    }

    public void startLevel(int levelNumber) {
        // Reset positions first...

        switch (levelNumber) {
            case 1: // Chapter 1, Level 1
                DELAY = 60; // Slow speed
                enemyAI_Type = "STUPID";
                break;
            case 2: // Chapter 1, Level 2
                DELAY = 50; // Faster
                enemyAI_Type = "NORMAL";
                // Maybe add obstacles to the map[x][y] here
                break;
            case 3: // Chapter 1, Level 3
                DELAY = 40;
                break;
            case 4: // BOSS FIGHT
                DELAY = 30; // Very fast
                enemyAI_Type = "BOSS"; // Smarter AI logic
                // Make enemy color distinct
                break;
            case 5: // Chapter 2 Starts...
                break;
        }
    }
}