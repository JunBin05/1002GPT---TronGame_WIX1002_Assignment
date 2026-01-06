package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Toggle button for character selection (two states/images).
 */
public class CharacterSelectButton extends IconButton {

    private final Image offImage;
    private final Image onImage;
    private boolean selected = false;
    private int width = -1;
    private int height = -1;

    public CharacterSelectButton(String offPath, String onPath) {
        super(offPath);
        this.offImage = new ImageIcon(offPath).getImage();
        this.onImage  = new ImageIcon(onPath).getImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                refreshIcon();
            }
        });
    }

    private void refreshIcon() {
        Image img = selected ? onImage : offImage;
        setBaseImage(img); // resizes if a size was already set
        if (width > 0 && height > 0) {
            super.resizeIcon(width, height);
        }
    }

    @Override
    public void resizeIcon(int width, int height) {
        this.width = width;
        this.height = height;
        super.resizeIcon(width, height);
    }

    public void reset() {
        this.selected = false;
        refreshIcon();
    }

    public boolean isSelected() {
        return selected;
    }
}
