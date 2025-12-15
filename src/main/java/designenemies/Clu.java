package designenemies;

public class Clu extends Enemy { 
    
    public Clu(boolean isBoss) {
        // Just pass the name "Clu" and the isBoss flag.
        // The parent Enemy class will look up "Clu_Boss" or "Clu_Minion" in the text file.
        super("Clu", isBoss);
        
        // This ensures the image loader looks for "clu_NORTH.png", etc.
        this.imageBaseName = "clu";
    }
}