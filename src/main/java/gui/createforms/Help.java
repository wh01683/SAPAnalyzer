package gui.createforms;

import gui.DatabaseTableModel;

import javax.swing.*;

/**
 * Author: William Robert Howerton III
 * Created: 10/18/2015
 */
public class Help extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlCategories;
    private JPanel pnlUnits;
    private JTable tblUnits;
    private JTable tblCategories;

    private DatabaseTableModel categoriesModel;
    private DatabaseTableModel unitsModel;

    public Help() {
        this.setContentPane(pnlMain);

        tblCategories.setModel(new DatabaseTableModel("select * from partcategory"));
        tblUnits.setModel(new DatabaseTableModel("select * from units"));
        this.pack();
    }
}
