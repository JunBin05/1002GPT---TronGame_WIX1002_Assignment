package UI;

public class RightButton extends IconButton {

    public RightButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Right Button Clicked!"));
    }
}