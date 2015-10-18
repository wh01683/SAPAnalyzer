package gui.createforms;

import db.DBInfo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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
    private JTextField fldPartTCost;
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

}
