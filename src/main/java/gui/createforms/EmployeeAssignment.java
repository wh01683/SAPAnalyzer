package gui.createforms;

import db.DBIO;
import db.DBInfo;
import db.Utility;
import gui.custom.DBRow;
import gui.custom.DatabaseTableModel;
import gui.custom.InsertTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Author: William Robert Howerton III
 * Created: 11/29/2015
 */
public class EmployeeAssignment extends JFrame {
    private JPanel pnlMain;
    private JScrollPane paneEmpHistory;
    private JTable tblHistory;
    private JPanel pnlFields;
    private JComboBox cbEmployee;
    private InsertTextField fldHrEst;
    private JButton btnAssign;
    private JPanel pnlEmpStats;
    private JPanel pnlTable;
    private JTextField fldWage;
    private JTextField fldLastName;
    private JTextField fldFirstName;
    private JTextField fldPlant;
    private JTextField fldParentPart;
    private JTextField fldChildPart;

    private Hashtable<String, Object> empNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<Object, String> empPkToName;

    private static Object parentKey, childKey;

    /**
     * Creates a new EmployeeAssignment form with a given parent part ID and child part ID. The part IDs refer to a
     * specific BOM and will stay the same through the life of this form and are used to grab information
     * regarding the BOM.
     *
     * @param parentPartID ParentPartID of the BOM in question.
     * @param childPartID  ChildPartID of the BOM in question.
     */
    public EmployeeAssignment(Object parentPartID, Object childPartID) {
        empPkToName = DBInfo.getEmpPkToName();
        empNameToPk = Utility.flipPkHash(empPkToName);
        fldChildPart.setText(childPartID.toString());
        fldParentPart.setText(parentPartID.toString());
        parentKey = parentPartID;
        childKey = childPartID;

        this.setContentPane(pnlMain);

        cbEmployee.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    Object empID = empNameToPk.get(cbEmployee.getSelectedItem().toString());
                    fillFieldDetails(empID);
                }
            }
        });

        btnAssign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object empID = empNameToPk.get(cbEmployee.getSelectedItem().toString());
                assignEmployee(empID);
            }
        });

        fillCbBoxes();
        this.pack();

    }

    /**
     * Fills combo boxes for the form.
     */
    private void fillCbBoxes() {
        if (empPkToName == null) {
            cbEmployee.addItem("");
        } else {
            Enumeration e = empPkToName.keys();
            while (e.hasMoreElements()) {
                String empName = empPkToName.get(e.nextElement());
                cbEmployee.addItem(empName);
            }
        }
    }

    /**
     * Helper method to help fill text fields with details when different objects are selected.
     *
     * @param empID Employee id used to fetch details.
     */
    private void fillFieldDetails(Object empID) {

        ArrayList<Object> empTableResults = null;

        try {
            empTableResults =
                    DBIO.getMultiObResults("select * from " +
                            "employees e join staff s " +
                            "on e.employeeid = s.employeeid " +
                            "where e.employeeid = " + empID.toString()).get(0);

            fldLastName.setText(empTableResults.get(1).toString());
            fldFirstName.setText(empTableResults.get(2).toString());
            fldPlant.setText(empTableResults.get(3).toString());
            fldWage.setText(empTableResults.get(4).toString());

            tblHistory.setModel(
                    new DatabaseTableModel(
                            "select parentpartid, childpartid, hrlycost, hrest, plantid" +
                                    " from plant_bom where employeeid = " + empID));
        } catch (NullPointerException n) {
            JOptionPane.showMessageDialog(null, "Employee selected must be staff member!");
        } catch (IndexOutOfBoundsException i) {
            System.out.printf("No employees associated with this BOM.");
        }
    }


    /**
     * Assigns a a plant's employee to the BOM and commits it to the database.
     *
     * @param empID Employee to assign.
     */
    private void assignEmployee(Object empID) {
        ArrayList<Object> empTableResults =
                DBIO.getMultiObResults("select * from " +
                        "employees e join staff s " +
                        "on e.employeeid = s.employeeid " +
                        "where e.employeeid = " + empID.toString()).get(0);

        Double hrEst = fldHrEst.getDouble();
        Double wage = Double.parseDouble(fldWage.getText());
        Object plantId = empTableResults.get(3);
        Double hrlyCost = wage * hrEst;


        DBRow plant_bomRow = new DBRow("PLANT_BOM", plantId, parentKey, childKey, hrlyCost, hrEst, empID);

        try {
            DBIO.executeWithoutReturn(plant_bomRow.getInsertQuery());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error encountered when inserting Plant_BOM entry.");
            e.printStackTrace();
        }
    }

}
