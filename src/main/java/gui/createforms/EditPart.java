package gui.createforms;

import db.DBIO;
import db.DBInfo;
import gui.custom.DBRow;
import gui.custom.InsertTextField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private JComboBox cbBOMSelect;
    private JTree treeBOMHierarchy;
    private JCheckBox chkAddPart;
    private JComboBox cbLevelsFilter;
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
    private InsertTextField fldBomStep;
    private InsertTextField fldBomQty;
    private InsertTextField fldBomHrlyCost;
    private InsertTextField fldHrEst;
    private InsertTextField fldPhase;
    private InsertTextField fldRevision;
    private InsertTextField fldProcType;
    private InsertTextField fldRefDes;

    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Hashtable<String, Object> suppNameToPk = new Hashtable<String, Object>(10);
    private DBIO dbio = new DBIO();
    boolean editable = false;

    public EditPart(boolean viewing) {

        //<editor-fold desc="Constructor">
        setEditAll(!viewing, pnlGeneral, pnlAdvanced, pnlStock, pnlEditBOM, pnlEditPart);
        editable = !viewing;

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

        fillCbBoxes();
        this.pack();
        //</editor-fold>

    }


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

    private void fillCbBoxes() {
        cbUnits.removeAllItems();
        cbSuppliers.removeAllItems();
        cbPartCategory.removeAllItems();
        cbPartPlantID.removeAllItems();

        ArrayList<String> units = DBInfo.getUnitOfMeasure();
        ArrayList<String> cats = DBInfo.getPartCategories();
        ArrayList<Object> plantId = DBInfo.getTabToPkVals().get("PLANTS");
        fillSuppHash();
        try {
            for (String s : units) {
                cbUnits.addItem(s);
            }
            for (String s : cats) {
                cbPartCategory.addItem(s);
            }
            for (Object o : plantId) {
                cbPartPlantID.addItem(o);
            }
            for (String s : suppNameToPk.keySet()) {
                cbSuppliers.addItem(s);
            }
        } catch (NullPointerException n) {
            System.out.printf("Load information from database first!\n");
            n.printStackTrace();
        }
    }

    private void insertRows() {
        ArrayList<String> queries = new ArrayList<String>(rowStack.size());
        DBIO.alterConstraints(false, "PART", "BOM", "STOCKDETAIL", "PART_SUPPLIER");
        for (DBRow row : rowStack) {
            queries.add(row.getInsertQuery());
        }
        //DBIO.executeQuery(queries);
        DBIO.alterConstraints(true, "PART", "BOM", "STOCKDETAIL", "PART_SUPPLIER");

    }
    private void makePartRow() {

        Integer partid = fldPartId.getInt();
        String desc = fldPartDesc.getText();
        Object plantID = cbPartPlantID.getSelectedItem();
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

        DBRow temp = new DBRow("PART", partid, desc, plantID, name, phase, revision, procType, refDes, notes, tcost,
                cost, wast, catid, unit);
        rowStack.push(temp);

    }

    private void makeStockRow() {
        Integer partId = fldPartId.getInt();
        Integer qtyonhand = fldQtyOnHand.getInt();
        Integer allocQty = fldAllocQty.getInt();
        Integer availQty = fldAvailQty.getInt();
        Integer reorder = fldReorderLvl.getInt();
        Integer leadTime = fldLeadTime.getInt();
        Integer supplierid = Integer.parseInt(suppNameToPk.get(cbSuppliers.getSelectedItem()).toString());

        DBRow temp = new DBRow("STOCKDETAIL", partId, qtyonhand, allocQty, availQty, reorder, leadTime, supplierid);
        rowStack.push(temp);
    }

    private void makePartSupplierRow() {
        Integer partId = fldPartId.getInt();
        Object supplierid = Integer.parseInt(suppNameToPk.get(cbSuppliers.getSelectedItem()).toString());
        DBRow temp = new DBRow("PART_SUPPLIER", partId, supplierid);
        rowStack.add(temp);
    }

    public void fillFields() throws NullPointerException {
        ArrayList<Object> partResults = DBIO.getMultiObResults(
                "select * from part where partid = " + fldPartId.getInt()).get(0);


        fldPartName.setText(partResults.get(4).toString());
        fldPartDesc.setText(partResults.get(2).toString());
        fldPhase.setText(partResults.get(5).toString());
        fldRevision.setText(partResults.get(6).toString());
        fldProcType.setText(partResults.get(7).toString());
        fldRefDes.setText(partResults.get(8).toString());
        txtarPartNotes.setText(partResults.get(9).toString());
        fldTransCost.setText(partResults.get(10).toString());
        fldPartCost.setText(partResults.get(11).toString());
        fldWaste.setText(partResults.get(12).toString());


        cbUnits.removeAllItems();
        cbPartCategory.removeAllItems();
        cbPartPlantID.removeAllItems();

        cbUnits.addItem(partResults.get(14));
        cbPartPlantID.addItem(partResults.get(3));
        cbPartCategory.addItem(partResults.get(13));

        cbUnits.setEnabled(false);
        cbPartCategory.setEnabled(false);
        cbPartPlantID.setEnabled(false);


        ArrayList<Object> stockResults = DBIO.getMultiObResults(
                "select * from stockdetail where partid = " + fldPartId.getInt()).get(0);

        fldQtyOnHand.setText(stockResults.get(2).toString());
        fldAllocQty.setText(stockResults.get(3).toString());
        fldAvailQty.setText(stockResults.get(4).toString());
        fldReorderLvl.setText(stockResults.get(5).toString());
        fldLeadTime.setText(stockResults.get(6).toString());
        cbSuppliers.removeAllItems();
        cbSuppliers.addItem(stockResults.get(7).toString());
        cbSuppliers.setEnabled(false);
    }

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


    private void fillSuppHash() {
        ArrayList<ArrayList<Object>> queryResults = DBIO.getMultiObResults("select * from supplier");
        for (ArrayList<Object> outerArr : queryResults) {
            suppNameToPk.put(outerArr.get(2).toString(), outerArr.get(1));
        }
    }

    public void setFldPartID(String newPartId) {
        this.fldPartId.setText(newPartId);
    }

}
