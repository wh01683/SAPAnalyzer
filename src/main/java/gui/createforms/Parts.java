package gui.createforms;

import db.QueryStorage;
import gui.custom.DatabaseTableModel;

import javax.swing.*;

/**
 * Author: William Robert Howerton III
 * Created: 11/29/2015
 *
 */
public class Parts extends JFrame {
    private JPanel pnlMain;
    private JTable tblParts;

    /**
     * Constructs a table showing all parts and associated quantities used to produce the given part
     * @param partID Part ID primary key of the part to obtain parts list for.
     */
    public Parts(Object partID) {
        this.setContentPane(pnlMain);
        tblParts.setModel(new DatabaseTableModel(QueryStorage.getPartList(partID)));
        this.pack();
    }
}
