package characters;

public class Tron extends Character {

    public Tron() {
        super("Tron", "Blue"); 
        this.symbol = 'T';
        this.imageBaseName = "tron"; 
    }

    @Override
    public void levelUp() {
        super.levelUp(); 
        
        // Much slower speed increase - only 0.02 per level instead of 0.05
        speed += 0.02; 
        handling += 0.02; 
        
        // --- FIXED HEART LOGIC ---
        // Every 10 levels, increase MAXIMUM lives
        if (level % 10 == 0) {
            this.maxLives += 1.0; 
            this.lives = this.maxLives; // Heal to the new max immediately
            System.out.println(">> MAX LIVES INCREASED! New Max: " + this.maxLives);
        }
    }
}