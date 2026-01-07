package characters;

public class Kevin extends Character {

    public Kevin() {
        super();
        CharacterLoader.loadInto(this, "Kevin");
    }

    @Override
    public void levelUp() {
        super.levelUp(); // Refills discs, increments level
        
        // Kevin gains handling slowly so it reaches cap around level 90, small speed gain
        // Handling increment unchanged; speed increased to 0.007 per level per request
        handling = Math.min(1.0, handling + 0.00337);
        speed = Math.min(1.0, speed + 0.007);

        // Kevin gets +1 disc capacity every 10 levels (in addition to base growth)
        if (level % 10 == 0) {
            this.discCapacity = Math.min(this.discCapacity + 1, XPSystem.TronRules.MAX_DISCS);
            this.currentDiscCount = this.discCapacity;
            System.out.println(">> Kevin bonus: disc capacity increased to " + this.discCapacity);
        }
    }
}