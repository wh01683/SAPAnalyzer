package gui.createforms;

import db.DBInfo;
import db.DatabaseIO;
import gui.DBRow;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Author: William Robert Howerton III
 * Created: 10/17/2015
 */
public class EditPart extends JFrame {
    private JPanel pnlMain;
    private JButton btnFindPart;
    private JTabbedPane tbPart;
    private JPanel pnlEditPart;
    private JPanel pnlEditBOM;
    private JButton btnEdit;
    private JPanel pnlGeneral;
    private JPanel pnlStock;
    private JPanel pnlAdvanced;
    private JTextField fldPartID;
    private JTextField fldPartDesc;
    private JComboBox cbUnits;
    private JTextField fldQtyOnHand;
    private JComboBox cbPartCategory;
    private JTextField fldPartName;
    private JTextField fldPartCost;
    private JTextField fldPartTransCost;
    private JTextField fldPartWaste;
    private JTextField fldAllocQty;
    private JTextField fldAvailQty;
    private JTextField fldMinReordLvl;
    private JTextField fldLeadTime;
    private JTextField fldPartPhase;
    private JTextField fldPartRev;
    private JTextField fldPartProcType;
    private JTextField fldPartRefDes;
    private JTextArea txtarPartNotes;
    private JComboBox cbBOMSelect;
    private JTextField fldBOMStep;
    private JTextField fldBOMQty;
    private JTextField fldBOMHrlyCost;
    private JTextField fldBOMHrEst;
    private JTree treeBOMHierarchy;
    private JCheckBox chkAddPart;
    private JComboBox cbLevelsFilter;
    private JTextField fldPartSupplier;
    private JComboBox cbPartPlantID;
    private JButton btnAddNew;
    private JButton btnClear;
    private JButton btnUndo;
    private JButton btnCommit;
    private JButton btnPreview;

    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private DatabaseIO dbio = new DatabaseIO();
    boolean editable = false;

    public EditPart() {
        chkAddPart.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (chkAddPart.isSelected()) {
                    btnFindPart.setEnabled(false);
                } else {
                    btnFindPart.setEnabled(true);
                }
            }
        });
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
                makeStockRow();
            }
        });
        btnUndo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rowStack.pop();
                rowStack.pop();
            }
        });
        btnPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayInsertTextPreview();
            }
        });

        fillCbBoxes();
        this.pack();
    }


    private void setEditAll(boolean editable, JPanel... panels) {

        for (int p = 0; p < panels.length; p++) {
            Component[] components = panels[p].getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i].getClass().getName().toString().equals("javax.swing.JTextField")) {
                    JTextField temp = (JTextField) components[i];
                    temp.setEditable(editable);
                    System.out.printf("Index: %d, Name: %s ", i, components[i].getName());
                }
            }
        }
    }

    private void fillCbBoxes() {
        ArrayList<String> units = DBInfo.getUnitOfMeasure();
        ArrayList<String> cats = DBInfo.getPartCategories();

        for (String s : units) {
            cbUnits.addItem(s);
        }

        for (String s : cats) {
            cbPartCategory.addItem(s);
        }
    }

    private void makePartRow() {

        Integer partid = Integer.parseInt(fldPartID.getText());
        String desc = fldPartDesc.getText();
        Integer plantID = (Integer) cbPartPlantID.getSelectedItem();
        String name = fldPartName.getText();
        String phase = fldPartPhase.getText();
        Character revision = fldPartRev.getText().charAt(0);
        String procType = fldPartProcType.getText();
        String refDes = fldPartRefDes.getText();
        String notes = txtarPartNotes.getText();
        Double tcost = Double.parseDouble(fldPartTransCost.getText());
        Double cost = Double.parseDouble(fldPartCost.getText());
        Double wast = Double.parseDouble(fldPartWaste.getText());
        String catid = (String) cbPartCategory.getSelectedItem();
        String unit = (String) cbUnits.getSelectedItem();

        DBRow temp = new DBRow("PART", partid, desc, plantID, name, phase, revision, procType, refDes, notes, tcost,
                cost, wast, catid, unit);
        rowStack.push(temp);

    }

    private void makeStockRow() {
        Integer partId = Integer.parseInt(fldPartID.getText());
        Integer qtyonhand = Integer.parseInt(fldQtyOnHand.getText());
        Integer allocQty = Integer.parseInt(fldAllocQty.getText());
        Integer availQty = Integer.parseInt(fldAvailQty.getText());
        Integer reorder = Integer.parseInt(fldMinReordLvl.getText());
        Integer leadTime = Integer.parseInt(fldLeadTime.getText());
        Integer supplierid = Integer.parseInt(fldPartSupplier.getText());

        DBRow temp = new DBRow("STOCKDETAIL", partId, qtyonhand, allocQty, availQty, reorder, leadTime, supplierid);
        rowStack.push(temp);
    }

    private void fillFields() {
        ArrayList<Object> partResults = dbio.getMultiObResults(
                "select * from part where partid = " + Integer.parseInt(fldPartID.getText())).get(0);

        fldPartName.setText(partResults.get(4).toString());
        fldPartDesc.setText(partResults.get(2).toString());
        fldPartPhase.setText(partResults.get(5).toString());
        fldPartRev.setText(partResults.get(6).toString());
        fldPartProcType.setText(partResults.get(7).toString());
        fldPartRefDes.setText(partResults.get(8).toString());
        txtarPartNotes.setText(partResults.get(9).toString());
        fldPartTransCost.setText(partResults.get(10).toString());
        fldPartCost.setText(partResults.get(11).toString());
        fldPartWaste.setText(partResults.get(12).toString());

        cbUnits.removeAllItems();
        cbPartCategory.removeAllItems();
        cbPartPlantID.removeAllItems();

        cbUnits.addItem(partResults.get(14));
        cbPartPlantID.addItem(partResults.get(3));
        cbPartCategory.addItem(partResults.get(13));

        cbUnits.setEnabled(false);
        cbPartCategory.setEnabled(false);
        cbPartPlantID.setEnabled(false);


        ArrayList<Object> stockResults = dbio.getMultiObResults(
                "select * from stockdetail where partid = " + Integer.parseInt(fldPartID.getText())).get(0);

        fldQtyOnHand.setText(stockResults.get(2).toString());
        fldAllocQty.setText(stockResults.get(3).toString());
        fldAvailQty.setText(stockResults.get(4).toString());
        fldMinReordLvl.setText(stockResults.get(5).toString());
        fldLeadTime.setText(stockResults.get(6).toString());
        fldPartSupplier.setText(stockResults.get(7).toString());

    }

    private void displayInsertTextPreview() {

        JTextArea textArea = new JTextArea("INSERT PREVIEW\n=================================================");

        for (DBRow row : rowStack) {
            textArea.append(row.getInsertQuery().concat("\n"));
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(null, scrollPane, "Insert Preview", JOptionPane.CLOSED_OPTION);
    }

    private void clearAll(JPanel... panels) {

        for (int p = 0; p < panels.length; p++) {
            Component[] components = panels[p].getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i].getClass().getName().toString().equals("javax.swing.JTextField")) {
                    JTextField temp = (JTextField) components[i];
                    temp.setText("");
                }
            }
        }
    }

}
