package UI;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class CutsceneManager {
    /**
     * Shows a cutscene dialog for the given chapter, stage, and suffix ("a" or "b").
     * Skips if the file does not exist or is empty.
     */
    public static void showCutscene(int chapter, int stage, String suffix, JFrame parent) {
        // Try cXlevelY[suffix].txt, then fallback to cXlevelY.txt if not found
        String fileA = String.format("cutscene/c%dlevel%d%s.txt", chapter, stage, suffix);
        String fileNoSuffix = String.format("cutscene/c%dlevel%d.txt", chapter, stage);
        File file = new File(fileA);
        System.out.println("[CutsceneManager] showCutscene called: chapter=" + chapter + ", stage=" + stage + ", suffix=" + suffix + ", fileA=" + fileA + ", fileNoSuffix=" + fileNoSuffix);
        if (!file.exists()) {
            System.out.println("[CutsceneManager] File does not exist: " + fileA + ". Trying fallback: " + fileNoSuffix);
            file = new File(fileNoSuffix);
            if (!file.exists()) {
                System.out.println("[CutsceneManager] Fallback file does not exist: " + fileNoSuffix);
                return;
            }
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line).append("\n");
        } catch (IOException e) {
            System.out.println("[CutsceneManager] IOException reading file: " + file.getPath());
            return;
        }
        if (content.length() == 0) {
            System.out.println("[CutsceneManager] File is empty: " + file.getPath());
            return;
        }
        System.out.println("[CutsceneManager] Showing cutscene dialog for file: " + file.getPath());
        JTextArea textArea = new JTextArea(content.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(parent, scrollPane, "Cutscene", JOptionPane.PLAIN_MESSAGE);
    }
}