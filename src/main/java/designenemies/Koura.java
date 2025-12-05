package designenemies;

public class Koura extends Enemy { 
    public Koura(boolean isBoss) {
        super("Koura", "Purple", isBoss?"Medium":"Easy", 50, "Normal", "Normal", 
              isBoss?"Standard":"Basic", "Standard Sentry", isBoss);
        this.imageBaseName = "koura";
    }
}