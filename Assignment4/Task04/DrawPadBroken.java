import java.awt.*;
import java.awt.event.*;

/** Deliberately faulty version used to capture the compiler diagnostic. */
class DrawPadBroken extends Frame {
    DrawPadBroken() {
        addMouseListener(new MouseAdapter() {
            @Override public void mouseclicked(MouseEvent e) { }
        });
    }
}
