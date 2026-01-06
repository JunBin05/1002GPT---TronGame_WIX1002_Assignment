package UI;

public class LeftButton extends IconButton {

    public LeftButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Left Button Clicked!"));
    }
}