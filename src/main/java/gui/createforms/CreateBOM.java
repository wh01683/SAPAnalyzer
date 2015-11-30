package gui.createforms;

import db.DBIO;
import db.DBInfo;
import db.Utility;
import gui.custom.DBRow;
import gui.custom.InsertTextField;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

/**
 * Author: William Robert Howerton III
 * Created: 10/17/2015
 */
public class CreateBOM extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlEditBOM;
    private JComboBox cbChildPartNames;
    private JTree treeBOMHierarchy;
    private JComboBox cbParentPartNames;
    private JButton btnAdd;
    private JButton undoButton;
    private JButton btnCommit;
    private JButton btnClearFields;
    private InsertTextField fldStep;
    private InsertTextField fldQty;
    private InsertTextField fldHrlyCost;
    private InsertTextField fldHrEst;
    private JButton btnPreview;
    private JComboBox cbEmployeeNames;
    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Stack<DefaultMutableTreeNode> nodeStack = new Stack<DefaultMutableTreeNode>();
    private Hashtable<Object, String> partPkToName;
    private Hashtable<String, Object> partNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<String, Object> empNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<Object, String> empPkToName;

    public CreateBOM(Object parentKey) {
        this();
        cbParentPartNames.setSelectedItem(partPkToName.get(parentKey));
    }
    public CreateBOM() {

        this.setContentPane(pnlMain);
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((rowStack != null)) {
                    rowStack.pop();
                } else {
                    JOptionPane.showMessageDialog(null, "No more nodes to delete.");
                }
            }
        });
        btnClearFields.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        fldHrlyCost.setEditable(false);
        partPkToName = DBInfo.getPartPkToName();
        partNameToPk = Utility.flipPkHash(partPkToName);

        empPkToName = DBInfo.getEmpPkToName();
        empNameToPk = Utility.flipPkHash(empPkToName);


        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeRowFromFields();
            }
        });
        btnCommit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertAllFromStack();
            }
        });
        btnPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buildPreview();
            }
        });
        fillCbBoxes();
        this.pack();

    }

    /**
     * Fills combo boxes in the form with information from hashtables in DBInfo.
     * Hashtables used to associate non-key entities with primary keys during insertion process
     */
    private void fillCbBoxes() {

        if (partPkToName == null) {
            cbChildPartNames.addItem("");
            cbParentPartNames.addItem("");
        } else {
            Enumeration e = partPkToName.keys();
            ArrayList<String> partNames = new ArrayList<String>(10);
            while (e.hasMoreElements()) {
                String partName = partPkToName.get(e.nextElement());
                partNames.add(partName);
            }

            for (String s : partNames) {
                cbChildPartNames.addItem(s);
                cbParentPartNames.addItem(s);
            }
        }
        if (empPkToName == null) {
            cbEmployeeNames.addItem("");
        } else {
            Enumeration e = empPkToName.keys();
            while (e.hasMoreElements()) {
                String empName = empPkToName.get(e.nextElement());
                cbEmployeeNames.addItem(empName);

            }
        }
    }


    /**
     * Creates BOM and Plant_BOM rows from the GUI fields.
     */
    private void makeRowFromFields() {

        StringBuilder nodeString = new StringBuilder();

        Object empID = empNameToPk.get(cbEmployeeNames.getSelectedItem().toString());
        Double assignedStaffWage = Double.parseDouble(
                DBIO.getMultiObResults("select wage from staff where employeeid = " + empID.toString())
                        .get(0).get(0).toString());
        Object plantId = DBIO.getMultiObResults("select e.plantid from employees e join staff s on s.employeeid = e.employeeid where s.employeeid = "
                + empID.toString()).get(0).get(0);
        Object childKey = partNameToPk.get(cbChildPartNames.getSelectedItem().toString());
        Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
        Integer step = fldStep.getInt();
        Integer qty = fldQty.getInt();
        Double hrEst = fldHrEst.getDouble();
        Double hrlyCost = assignedStaffWage * hrEst;

        DBRow bomRow = new DBRow("BOM", step, qty, parentKey, childKey);
        DBRow plant_bomRow = new DBRow("PLANT_BOM", plantId, parentKey, childKey, hrlyCost, hrEst, empID);
        rowStack.push(bomRow);
        rowStack.push(plant_bomRow);
    }

    /**
     * Clears all fields in pnlEditBOM
     */
    private void clearFields() {

        Component[] components = pnlEditBOM.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i].getClass().getName().toString().equals("gui.custom.InsertTextField")) {
                InsertTextField temp = (InsertTextField) components[i];
                temp.setText("");
            }
        }
    }

    /**
     * INSERTS and COMMITS all BOMs and Plant_BOM rows from the row stack and clears the stack.
     */
    private void insertAllFromStack() {
        for (DBRow row : rowStack) {
            try {
                DBIO.executeWithoutReturn(row.getInsertQuery());
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        rowStack.clear();
    }

    /**
     * Shows the current stack insert preview.
     */
    private void buildPreview() {

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
}
