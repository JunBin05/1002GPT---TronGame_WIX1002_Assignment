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
        
        // Uniform per-level increases (small): Tron focuses on speed and stability
        speed = Math.min(1.0, speed + 0.005); 
        handling = Math.min(1.0, handling + 0.004);
        
        // --- FIXED HEART LOGIC ---
        // Every 10 levels, increase MAXIMUM lives
        if (level % 10 == 0) {
            this.maxLives += 1.0; 
            this.lives = this.maxLives; // Heal to the new max immediately
            System.out.println(">> MAX LIVES INCREASED! New Max: " + this.maxLives);
        }
    }
}