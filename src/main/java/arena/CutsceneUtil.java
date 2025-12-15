package arena;

import UI.GamePanel;
import javax.swing.*;
import java.io.File;

public class CutsceneUtil {
    /**
     * Checks if a cutscene file exists for the given chapter, stage, and suffix ("a" or "b").
     * Skips files with "random" in the name.
     */
    public static boolean cutsceneExists(int chapter, int stage, String suffix) {
        String[] candidates = {
            String.format("cutscene/c%dlevel%d%s.txt", chapter, stage, suffix),
            String.format("cutscene/c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random")) continue;
            File file = new File(path);
            if (file.exists() && file.length() > 0) return true;
        }
        return false;
    }

    /**
     * Shows the cutscene using the GamePanel's cutscene system, if the file exists and is not random.
     */
    public static void showCutsceneIfExists(int chapter, int stage, String suffix, JFrame parent, GamePanel panel) {
        String[] candidates = {
            String.format("cutscene/c%dlevel%d%s.txt", chapter, stage, suffix),
            String.format("cutscene/c%dlevel%d.txt", chapter, stage)
        };
        for (String path : candidates) {
            if (path.contains("random")) continue;
            File file = new File(path);
            if (file.exists() && file.length() > 0) {
                panel.cutscene.startScene(path);
                // Wait for cutscene to finish (blocking or via callback in real game)
                // For now, just return after starting
                return;
            }
        }
    }
}
