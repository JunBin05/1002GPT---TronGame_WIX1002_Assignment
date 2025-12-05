package characters;

public class Kevin extends Character {

    public Kevin() {
        super("Kevin", "Orange"); 
        this.symbol = 'K';
        this.imageBaseName = "kevin"; 
    }

    @Override
    public void levelUp() {
        super.levelUp(); // Refills discs, increments level
        
        // Much slower speed increase - reduced from 0.03 to 0.015, handling from 0.08 to 0.03
        speed += 0.015;    
        handling += 0.03;  
        
        if (level % 10 == 0) {
            this.maxLives += 1.0; 
            this.lives = this.maxLives; // Heal to the new max immediately
            System.out.println(">> MAX LIVES INCREASED! New Max: " + this.maxLives);
        }
    }
}