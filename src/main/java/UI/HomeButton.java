package UI;

public class HomeButton extends IconButton {

    public HomeButton(String imagePath, MainFrame mainFrame, String username) {
        super(imagePath);
        addActionListener(e -> System.out.println("Home Button Clicked!"));
    }
}