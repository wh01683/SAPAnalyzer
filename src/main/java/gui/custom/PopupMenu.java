package gui.custom;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Class used for the popup menu seen in the SAP Analyzer main gui form.
 */
public class PopupMenu extends JPopupMenu {
    JPopupMenu menu = new JPopupMenu("Popup");

    class MyLabel extends JLabel {

        class PopupTriggerListener extends MouseAdapter {
            public void mousePressed(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    menu.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }

            public void mouseReleased(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    menu.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }

            public void mouseClicked(MouseEvent ev) {
            }
        }
    }


    public PopupMenu() {
    }
}