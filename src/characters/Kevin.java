package characters;

public class Kevin extends Character {

    public Kevin() {
        super("Kevin", "White"); // Kevin is White [cite: 85]
        this.symbol = 'K'; // Use 'K' for visual identification before image is ready
        // Initial stats will be loaded from characters.txt later
    }

    @Override
    public void levelUp() {
        level++;
        // Kevin gains more handling precision and discsOwned per level[cite: 101].
        // Example implementation (customize as needed):
        handling += 0.5;
        speed += 0.1;
        
        // Add additional disc slot every 15 levels [cite: 96]
        if (level % 15 == 0) {
            discsOwned += 1;
        }
    }
}