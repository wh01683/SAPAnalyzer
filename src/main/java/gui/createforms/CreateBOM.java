package gui.createforms;

import db.DBIO;
import db.DBInfo;
import db.QueryStorage;
import db.Utility;
import gui.custom.DBRow;
import gui.custom.DatabaseTableModel;
import gui.custom.InsertTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
    private JButton btnAssignEmp;
    private JTable tblBOMInfo;
    private JPanel pnlBOMInfo;
    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Hashtable<Object, String> partPkToName;
    private Hashtable<String, Object> partNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<String, Object> empNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<Object, String> empPkToName;

    /**
     * Helper constructor to use alongside the popup menu in the SAP analyzer GUI.
     *
     * @param parentKey
     */
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

        fillCbBoxes();

        cbParentPartNames.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
                    tblBOMInfo.setModel(new DatabaseTableModel(QueryStorage.getPartList(parentKey)));
                    onPartSelect();
                }
            }
        });

        cbChildPartNames.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    onPartSelect();
                }
            }
        });

        cbEmployeeNames.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    fillEmployeeFields(empNameToPk.get(cbEmployeeNames.getSelectedItem()));
                }
            }
        });
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
        btnAssignEmp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new EmployeeAssignment(partNameToPk.get(cbParentPartNames.getSelectedItem().toString()),
                        partNameToPk.get(cbChildPartNames.getSelectedItem().toString())).setVisible(true);
            }
        });
        this.pack();

    }

    /**
     * Handles information updates when a new part is selected in the combo box.
     */
    private void onPartSelect() {
        Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
        Object childKey = partNameToPk.get(cbChildPartNames.getSelectedItem().toString());
        fillEmployeeCbBoxes(parentKey, childKey);

        try {
            ArrayList<Object> bomResults = DBIO.getMultiObResults(
                    "select qty, step from bom where parentpartid = " + parentKey + " and childpartid = " + childKey).get(0);
            fldStep.setText(bomResults.get(1).toString());
            fldQty.setText(bomResults.get(0).toString());
        } catch (IndexOutOfBoundsException i) {
            System.out.printf("No BOM results for parent key = %s and child key = %s.\n",
                    parentKey.toString(), childKey.toString());
        }
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

            Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
            Object childKey = partNameToPk.get(cbChildPartNames.getSelectedItem().toString());

            fillEmployeeCbBoxes(parentKey, childKey);

        }
    }


    /**
     * Creates BOM and Plant_BOM rows from the GUI fields.
     */
    private void makeRowFromFields() {

        Object childKey = partNameToPk.get(cbChildPartNames.getSelectedItem().toString());
        Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
        Integer step = fldStep.getInt();
        Integer qty = fldQty.getInt();

        DBRow bomRow = new DBRow("BOM", step, qty, parentKey, childKey);
        rowStack.push(bomRow);
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

    /**
     * This method fills the employee combo box with all employees assigned to a given BOM.
     *
     * @param parentPartId ParentPartID of the BOM
     * @param childPartId  ChildPartID of the BOM
     */
    private void fillEmployeeCbBoxes(Object parentPartId, Object childPartId) {

        cbEmployeeNames.removeAllItems();
        ArrayList<ArrayList<Object>> assignedEmployees = DBIO.getMultiObResults("select employeeid from plant_bom " +
                "where parentpartid = " + parentPartId +
                " and childpartid = " + childPartId);
        ArrayList<Object> employeeIDs = new ArrayList<Object>(10);

        for (ArrayList<Object> row : assignedEmployees) {
            employeeIDs.add(row.get(0));
        }

        for (Object o : employeeIDs) {
            cbEmployeeNames.addItem(empPkToName.get(o));
        }
    }

    /**
     * Helper method to fill all employee related information fields using a given employee ID primary key.
     *
     * @param employeeID EmployeeID primary key of the employee.
     */
    private void fillEmployeeFields(Object employeeID) {

        Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
        Object childKey = partNameToPk.get(cbChildPartNames.getSelectedItem().toString());

        ArrayList<Object> empStats = DBIO.getMultiObResults(
                "select * from plant_bom where parentpartid = " + parentKey +
                        " and childpartid = " + childKey + " and employeeid = " + employeeID).get(0);

        fldHrlyCost.setText(empStats.get(3).toString());
        fldHrEst.setText(empStats.get(4).toString());
    }
}
