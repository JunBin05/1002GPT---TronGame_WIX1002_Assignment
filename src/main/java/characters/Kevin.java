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
        
        // Kevin gains handling slowly so it reaches cap around level 90, small speed gain
        // Calculation: (1.0 - baseHandling) / (90 - 1) ≈ 0.00337 per level
        handling = Math.min(1.0, handling + 0.00337);
        speed = Math.min(1.0, speed + 0.003);

        // Kevin gets +1 disc capacity every 10 levels (in addition to base growth)
        if (level % 10 == 0) {
            this.discCapacity = Math.min(this.discCapacity + 1, XPSystem.TronRules.MAX_DISCS);
            this.currentDiscCount = this.discCapacity;
            System.out.println(">> Kevin bonus: disc capacity increased to " + this.discCapacity);
        }
        
        if (level % 10 == 0) {
            this.maxLives += 1.0; 
            this.lives = this.maxLives; // Heal to the new max immediately
            System.out.println(">> MAX LIVES INCREASED! New Max: " + this.maxLives);
        }
    }
}