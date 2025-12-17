package XPSystem;

public class CharacterProfile {
    private String name;
    private int level;
    private long currentXp;
    private int discCount;

    public CharacterProfile(String name) {
        this.name = name;
        this.level = 1;
        this.currentXp = 0;
        this.discCount = 1;
    }

    public void addXp(long amount) {
        this.currentXp += amount;

        // Check for Level Up (Using the While Loop logic)
        // We use TronRules.getTotalXpForLevel to check the threshold
        while (level < TronRules.MAX_LEVEL &&
                currentXp >= TronRules.getTotalXpForLevel(level + 1)) {

            level++;
            this.discCount = TronRules.getDiscCount(level);

            System.out.println(">>> LEVEL UP! " + name + " is now Version " + level + ".0! (Discs: " + discCount + ")");
        }
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public int getDiscs() {
        return discCount;
    }

    public long getXp() {
        return currentXp;
    }
}