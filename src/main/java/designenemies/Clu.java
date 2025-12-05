package designenemies;

public class Clu extends Enemy { 
    public Clu(boolean isBoss) {
        super("Clu", "Yellow", isBoss?"Extreme":"Medium", 100, "Very Fast", "Perfect", 
              isBoss?"Strategic":"Basic", "The Codified Likeness Utility", isBoss);
        this.imageBaseName = "clu";
    }
}