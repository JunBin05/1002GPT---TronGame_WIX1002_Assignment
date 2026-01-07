package UI;

public class PlayArenaButton extends IconButton {

    public PlayArenaButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Play Arena Button Clicked! (Game Start immediately...)"));
    }
}