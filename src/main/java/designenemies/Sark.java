package designenemies;

public class Sark extends Enemy { 
    public Sark(boolean isBoss) {
        super("Sark", "Red", isBoss?"Hard":"Easy", 100, "Fast", "High", 
              isBoss?"Smart":"Basic", "The Commander", isBoss);
        this.imageBaseName = "sark";
    }
}