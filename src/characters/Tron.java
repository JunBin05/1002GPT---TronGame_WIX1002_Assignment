package characters;

public class Tron extends Character {

    public Tron() {
        super("Tron", "Blue"); // Tron is Blue [cite: 85]
        this.symbol = 'T'; // Use 'T' for visual identification before image is ready
        // Initial stats will be loaded from characters.txt later
    }

    @Override
    public void levelUp() {
        level++;
        // Tron gains more speed and stability per level[cite: 100].
        // Example implementation (customize as needed):
        speed += 0.5; 
        handling += 0.1; 
        
        // Add 1 life every 10 levels [cite: 95]
        if (level % 10 == 0) {
            lives += 1;
        }
    }
}