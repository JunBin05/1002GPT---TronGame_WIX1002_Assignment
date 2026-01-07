package UI;

public class BackButton extends IconButton {

    public BackButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Back Button Clicked!"));
    }
}