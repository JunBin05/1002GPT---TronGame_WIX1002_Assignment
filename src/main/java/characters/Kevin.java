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
    }

    @Override
    protected int computeDiscBonus() {
        return super.computeDiscBonus() + (this.level / 10);
    }
}