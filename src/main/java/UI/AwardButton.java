package UI;

public class AwardButton extends IconButton {

    public AwardButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Award Button Clicked!"));
    }
}