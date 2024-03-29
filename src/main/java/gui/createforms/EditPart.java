package gui.createforms;

import db.DBIO;
import db.DBInfo;
import db.Utility;
import gui.custom.DBRow;
import gui.custom.InsertTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.Stack;

/**
 * Author: William Robert Howerton III
 * Created: 10/17/2015
 */

public class EditPart extends JFrame {

    //<editor-fold desc="GUI Fields">

    private JPanel pnlMain;
    private JButton btnFindPart;
    private JTabbedPane tbPart;
    private JPanel pnlEditPart;
    private JPanel pnlEditBOM;
    private JButton btnEdit;
    private JPanel pnlGeneral;
    private JPanel pnlStock;
    private JPanel pnlAdvanced;
    private JComboBox cbUnits;
    private JComboBox cbPartCategory;
    private JTextArea txtarPartNotes;
    private JComboBox cbPartPlantID;
    private JButton btnAddNew;
    private JButton btnClear;
    private JButton btnUndo;
    private JButton btnCommit;
    private JButton btnPreview;
    private JComboBox cbSuppliers;
    private InsertTextField fldPartId;
    private InsertTextField fldPartDesc;
    private InsertTextField fldPartCost;
    private InsertTextField fldQtyOnHand;
    private InsertTextField fldAllocQty;
    private InsertTextField fldAvailQty;
    private InsertTextField fldLeadTime;
    private InsertTextField fldPartName;

    private InsertTextField fldWaste;
    private InsertTextField fldTransCost;
    private InsertTextField fldReorderLvl;
    private InsertTextField fldPhase;
    private InsertTextField fldRevision;
    private InsertTextField fldProcType;
    private InsertTextField fldRefDes;
    private JCheckBox chkStockDetail;
    private JButton btnShowPartList;
    private JMenuBar menuBar;
    //</editor-fold>

    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Hashtable<String, Object> suppNameToPk = new Hashtable<String, Object>(10);


    boolean editable = false;

    /**
     * Used to create an EditPart form that looks up a part from the database automatically on creation.
     *
     * @param partID Part ID (primary key) of part to look up.
     */
    public EditPart(Object partID) {
        this(true);
        fldPartId.setText(partID.toString());
        fillFields();
    }

    /**
     * Constructor for EditPart form.
     * @param viewing If true, user will be unable to edit many fields in the form.
     *                If false, user will be able to edit these fields and use combo boxes.
     */
    public EditPart(boolean viewing) {

        //<editor-fold desc="Constructor">

        setEditAll(!viewing, pnlGeneral, pnlAdvanced, pnlStock, pnlEditPart);
        editable = !viewing;

        this.setVisible(true);
        pnlMain.setVisible(true);
        this.setContentPane(pnlMain);

        btnEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean temp = !editable;
                editable = temp;
                setEditAll(editable, pnlGeneral, pnlAdvanced, pnlStock, pnlEditBOM, pnlEditPart);
            }
        });
        btnFindPart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillFields();
            }
        });
        btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAll(pnlMain, pnlGeneral, pnlAdvanced, pnlStock, pnlEditBOM, pnlEditPart);
            }
        });
        btnAddNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makePartRow();
                if (chkStockDetail.isSelected()) {
                    makeStockRow();
                }
                makePartSupplierRow();
            }
        });
        btnUndo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    rowStack.pop();
                    rowStack.pop();
                    rowStack.pop();
                } catch (EmptyStackException n) {
                    JOptionPane.showMessageDialog(null, "Row stack already empty.");
                }
            }
        });

        btnShowPartList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Parts(fldPartId.getText()).setVisible(true);
            }
        });
        btnPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayInsertTextPreview();
            }
        });
        btnCommit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertRows();
            }
        });

        suppNameToPk = Utility.flipPkHash(DBInfo.getSuppPkToName());
        createMenu();
        this.setJMenuBar(menuBar);
        fillCbBoxes();

        this.pack();

        //</editor-fold>

    }

    /**
     * Cycles through a given array of panels and sets all JTextAreas and all JTextFields found in the panels to
     * editable or non-editable, depending on whether the user is just viewing an item's information from the database
     * or attempting to add a new item. Will disable combo boxes.
     *
     * @param editable true sets all fields to editable
     * @param panels   panels containing fields to be altered.
     */
    private void setEditAll(boolean editable, JPanel... panels) {


        for (int p = 0; p < panels.length; p++) {
            Component[] components = panels[p].getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i].getClass().getName().toString().equals("gui.custom.InsertTextField")) {
                    InsertTextField temp = (InsertTextField) components[i];
                    temp.setEditable(editable);
                } else if (components[i].getClass().getName().toString().equals("javax.swing.JTextArea")) {
                    JTextArea temp = (JTextArea) components[i];
                    temp.setEditable(editable);
                } else if (components[i].getClass().getName().toString().equals("javax.swing.JComboBox")) {
                    JComboBox temp = (JComboBox) components[i];
                    temp.setEnabled(editable);
                    if (editable) {
                        fillCbBoxes();
                    } else {
                        cbUnits.removeAllItems();
                        cbSuppliers.removeAllItems();
                        cbPartCategory.removeAllItems();
                        cbPartPlantID.removeAllItems();
                    }
                }
            }
        }
    }

    /**
     * Method fills all combo boxes in the form with information from the database. Clears all CB boxes first
     * to avoid adding duplicates. Null exception will be caught if the information has not been loaded from the database yet.
     */
    private void fillCbBoxes() {
        cbUnits.removeAllItems();
        cbSuppliers.removeAllItems();
        cbPartCategory.removeAllItems();
        cbPartPlantID.removeAllItems();

        ArrayList<String> units = DBInfo.getUnitOfMeasure();
        ArrayList<String> cats = DBInfo.getPartCategories();
        try {
            for (String s : units) {
                cbUnits.addItem(s);
            }
            for (String s : cats) {
                cbPartCategory.addItem(s);
            }

            for (String s : suppNameToPk.keySet()) {
                cbSuppliers.addItem(s);
            }
        } catch (NullPointerException n) {
            System.out.printf("Load information from database first!\n");
            n.printStackTrace();
        }
    }

    /**
     * Inserts all rows in the stack. Queries are pre-formatted using the DBRow object's getInsertQuery method.
     */
    private void insertRows() {

        DBIO.alterConstraints(false, "PART", "BOM", "STOCKDETAIL", "PART_SUPPLIER");
        for (DBRow row : rowStack) {
            try {
                DBIO.executeWithoutReturn(row.getInsertQuery());
            } catch (java.sql.SQLException e) {
                JOptionPane.showMessageDialog(null, "Error encountered when inserting row \n" + row.toString() + "\n\n" +
                        "It is likely due to a duplicate key in the table.");
                e.printStackTrace();
            }
        }
        DBIO.alterConstraints(true, "PART", "BOM", "STOCKDETAIL", "PART_SUPPLIER");

        rowStack.clear();
    }

    /**
     * Creates a new DBRow compatible with PART table.
     */
    private void makePartRow() {

        if (!verifyPk()) {
            JOptionPane.showMessageDialog(null, "PK " + fldPartId.getInt() + " is already taken.");
            fldPartId.grabFocus();
        } else {
            Integer partid = fldPartId.getInt();
            String desc = fldPartDesc.getText();
            String name = fldPartName.getText();
            String phase = fldPhase.getText();
            Character revision = fldRevision.getChar();
            String procType = fldProcType.getText();
            String refDes = fldRefDes.getText();
            String notes = txtarPartNotes.getText();
            Double tcost = fldTransCost.getDouble();
            Double cost = fldPartCost.getDouble();
            Double wast = fldWaste.getDouble();
            String catid = (String) cbPartCategory.getSelectedItem();
            String unit = (String) cbUnits.getSelectedItem();

            DBRow temp = new DBRow("PART", partid, name, desc, phase, revision, procType,
                    cost, tcost, refDes, wast, notes, unit, catid);
            rowStack.push(temp);
        }
    }

    /**
     * Creates a new DBRow compatible with STOCKDETAIL table.
     */
    private void makeStockRow() {
        Integer partId = fldPartId.getInt();
        Integer qtyonhand = fldQtyOnHand.getInt();
        Integer allocQty = fldAllocQty.getInt();
        Integer availQty = fldAvailQty.getInt();
        Integer reorder = fldReorderLvl.getInt();
        Integer leadTime = fldLeadTime.getInt();

        DBRow temp = new DBRow("STOCKDETAIL", qtyonhand, allocQty, availQty, reorder, leadTime, partId);
        rowStack.push(temp);
    }

    /**
     * Creates a new DBRow compatible with PART_SUPPLIER table.
     */
    private void makePartSupplierRow() {
        Integer partId = fldPartId.getInt();
        Double delivCost = fldTransCost.getDouble();
        Double suppCost = fldPartCost.getDouble();
        Object supplierid = Integer.parseInt(suppNameToPk.get(cbSuppliers.getSelectedItem()).toString());
        DBRow temp = new DBRow("PART_SUPPLIER", supplierid, partId, delivCost, suppCost);
        rowStack.add(temp);
    }

    /**
     * Fills all text fields in the form with data from the database.
     */
    private void fillFields() {

        ArrayList<Object> partResults = null;

        try {
            partResults = DBIO.getMultiObResults(
                    "select * from part where partid = " + fldPartId.getInt()).get(0);
        } catch (IndexOutOfBoundsException i) {
            JOptionPane.showMessageDialog(null, "Part ID " + fldPartId.getInt() + " not found.");
            fldPartId.grabFocus();
            fldPartId.setText("");
        }

        try {
            fldPartName.setText(partResults.get(1).toString());
            fldPartDesc.setText(partResults.get(2).toString());
            fldPhase.setText(partResults.get(3).toString());
            fldRevision.setText(partResults.get(4).toString());
            fldProcType.setText(partResults.get(5).toString());
            fldRefDes.setText(partResults.get(8).toString());
            txtarPartNotes.setText(partResults.get(10).toString());
            fldTransCost.setText(partResults.get(8).toString());
            fldPartCost.setText(partResults.get(6).toString());
            fldWaste.setText(partResults.get(9).toString());


        cbUnits.removeAllItems();
        cbPartCategory.removeAllItems();
        cbPartPlantID.removeAllItems();

            cbUnits.addItem(partResults.get(11));
        cbPartCategory.addItem(partResults.get(12));

        cbUnits.setEnabled(false);
        cbPartCategory.setEnabled(false);
        cbPartPlantID.setEnabled(false);


        ArrayList<Object> stockResults = DBIO.getMultiObResults(
                "select * from stockdetail where partid = " + fldPartId.getInt()).get(0);

            try {
                fldQtyOnHand.setText(stockResults.get(0).toString());
                fldAllocQty.setText(stockResults.get(1).toString());
                fldAvailQty.setText(stockResults.get(2).toString());
                fldReorderLvl.setText(stockResults.get(3).toString());
                fldLeadTime.setText(stockResults.get(4).toString());
            } catch (IndexOutOfBoundsException noStockInfo) {
                fldQtyOnHand.setText("");
                fldAllocQty.setText("");
                fldAvailQty.setText("");
                fldReorderLvl.setText("");
                fldLeadTime.setText("");
            }
        } catch (NullPointerException n) {
            JOptionPane.showMessageDialog(null, "Must supply the part's ID number.");
            fldPartId.grabFocus();
        }

    }

    /**
     * Brings up preview of insert queries used for the proposed row additions.
     */
    private void displayInsertTextPreview() {

        JTextArea textArea = new JTextArea("INSERT PREVIEW\n=================================================\n");

        for (DBRow row : rowStack) {
            textArea.append(row.getInsertQuery().concat("\n"));
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(null, scrollPane, "Insert Preview", JOptionPane.CLOSED_OPTION);
    }

    /**
     * Creates the File and Help menus located at the top of the form.
     */
    private void createMenu() {


        menuBar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenu mnuHelp = new JMenu("Help");
        JMenuItem mnuItemUnitsNCats = new JMenuItem("Units & Categories");
        JMenuItem mnuLoad = new JMenu("Load");
        JMenuItem mnuItemNew = new JMenu("New");
        JMenuItem mnuItemBOM = new JMenuItem("BOM");
        JMenuItem mnuItemPart = new JMenuItem("Part");

        JMenuItem mnuItemLoadAll = new JMenuItem("All");
        mnuItemLoadAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DBInfo.showLoadScreen();
                    }
                });
            }
        });

        mnuItemPart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new EditPart(false).setVisible(true);

            }
        });

        mnuItemBOM.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CreateBOM().setVisible(true);
            }
        });

        mnuItemUnitsNCats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Help().setVisible(true);
            }
        });

        mnuItemNew.add(mnuItemBOM);
        mnuItemNew.add(mnuItemPart);
        mnuFile.add(mnuItemNew);
        mnuHelp.add(mnuItemUnitsNCats);
        mnuLoad.add(mnuItemLoadAll);
        mnuFile.add(mnuLoad);
        menuBar.add(mnuFile);
        menuBar.add(mnuHelp);
    }

    /**
     * Clears all JTextFields in an array of panels by setting text to ""
     *
     * @param panels
     */
    private void clearAll(JPanel... panels) {

        for (int p = 0; p < panels.length; p++) {
            Component[] components = panels[p].getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i].getClass().getName().toString().equals("gui.custom.InsertTextField")) {
                    InsertTextField temp = (InsertTextField) components[i];
                    temp.setText("");
                }
            }
        }
    }


    /**
     * Sets value contained in the primary key text field to a new value.
     * @param newPartId new PART primary key value
     */
    public void setFldPartID(String newPartId) {
        this.fldPartId.setText(newPartId);
    }

    /**
     * Verifies that the PK is not already taken by checking against primary key values loaded from the PART table.
     * @return True if PK is not taken, returns false if PK is already in table.
     */
    private boolean verifyPk() {
        return !DBInfo.getTabToPkVals().get("PART").contains(fldPartId.getInt());
    }


}
