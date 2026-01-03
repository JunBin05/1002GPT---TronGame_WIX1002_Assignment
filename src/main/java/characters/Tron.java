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
    }
}