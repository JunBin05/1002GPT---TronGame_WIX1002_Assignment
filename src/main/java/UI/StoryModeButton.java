package UI;

public class StoryModeButton extends IconButton {

    public StoryModeButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Story Mode Button Clicked!"));
    }
}