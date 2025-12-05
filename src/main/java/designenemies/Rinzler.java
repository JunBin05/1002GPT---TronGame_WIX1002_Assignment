package designenemies;

public class Rinzler extends Enemy { 
    public Rinzler(boolean isBoss) {
        super("Rinzler", "Orange", isBoss?"Hard":"Easy", 100, "Fast", "High", 
              isBoss?"Adaptive":"Basic", "Tron's corrupted form", isBoss);
        this.imageBaseName = "rinzler";
    }
}