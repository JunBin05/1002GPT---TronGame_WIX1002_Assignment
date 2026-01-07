package UI;

public class PlayButton extends IconButton {

    public PlayButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Play Button Clicked!"));
    }
}