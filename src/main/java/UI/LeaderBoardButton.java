package UI;

public class LeaderBoardButton extends IconButton {

    public LeaderBoardButton(String imagePath) {
        super(imagePath);
        addActionListener(e -> System.out.println("Leaderboard Button Clicked!"));
    }
}