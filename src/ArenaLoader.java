import javax.swing.*;
import java.awt.*;

public class ArenaLoader {

    public static Arena loadArena(int choice) {
        return switch (choice) {
            case 1 -> new ArenaOne();
            case 2 -> new ArenaTwo();
            case 3 -> new ArenaThree();
            case 4 -> new RandomArena();
            default -> new ArenaOne();
        };
    }

    public static void showArenaWindow(Arena arena) {
        JFrame frame = new JFrame("Tron Arena Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 850);
        frame.setLayout(new GridLayout(40, 40));
        
        // --- 1. Scaling Setup ---
        // We calculate the size to be 1.5 times the actual cell size (850 / 40 ≈ 21 pixels).
        final double SCALE_MULTIPLIER = 2; 
        final int TRUE_CELL_SIZE = frame.getWidth() / 40; 
        final int CELL_SIZE = (int) (TRUE_CELL_SIZE * SCALE_MULTIPLIER); // Approx 31 pixels
        // ------------------------

        // --- 2. Define Base Path and Load/Scale Image Icons ---
        // NOTE: Path needs to be correct for your environment.
        String baseDir = "C:\\Users\\User\\Documents\\UM Sem 1 Coding\\1002GPT---TronGame_WIX1002_Assignment\\data\\";

        // Load original images
        ImageIcon originalObstacleIcon = new ImageIcon(baseDir + "obstacle.png");
        ImageIcon originalSpeedRampIcon = new ImageIcon(baseDir + "Speed.png");

        ImageIcon obstacleIcon = null;
        ImageIcon speedRampIcon = null;

        // Scale the Obstacle Image
        if (originalObstacleIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image scaledObstacleImage = originalObstacleIcon.getImage().getScaledInstance(
                CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH); 
            obstacleIcon = new ImageIcon(scaledObstacleImage);
        }
        
        // Scale the Speed Ramp Image
        if (originalSpeedRampIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image scaledSpeedRampImage = originalSpeedRampIcon.getImage().getScaledInstance(
                CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH); 
            speedRampIcon = new ImageIcon(scaledSpeedRampImage);
        }
        // --------------------------------------------------

        char[][] grid = arena.getGrid();

        for (int r = 0; r < 40; r++) {
            for (int c = 0; c < 40; c++) {

                JLabel cell = new JLabel();
                cell.setOpaque(true);
                cell.setHorizontalAlignment(JLabel.CENTER); 
                cell.setVerticalAlignment(JLabel.CENTER);
                cell.setIcon(null); 

                switch (grid[r][c]) {
                    case '#': // WALL
                        cell.setBackground(new Color(0, 120, 255));
                        break;

                    case 'O': // OBSTACLE
                        cell.setBackground(Color.BLACK);
                        if (obstacleIcon != null) {
                            cell.setIcon(obstacleIcon);
                        } else {
                            // Fallback to color
                            cell.setBackground(new Color(255, 140, 0));
                        }
                        break;

                    case 'S': // SPEED RAMP
                        cell.setBackground(Color.BLACK);
                        if (speedRampIcon != null) {
                            cell.setIcon(speedRampIcon);
                        } else {
                            // Fallback to color
                            cell.setBackground(new Color(0, 255, 255));
                        }
                        break;

                    default:  // EMPTY
                        cell.setBackground(Color.BLACK);
                        break;
                }

                frame.add(cell);
            }
        }

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Arena arena = loadArena(4);
        showArenaWindow(arena);
    }
}