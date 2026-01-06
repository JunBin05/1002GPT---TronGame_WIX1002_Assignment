package UI;

public class QuestionButton extends IconButton {

    public QuestionButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("QNA Button Clicked!"));
    }
}