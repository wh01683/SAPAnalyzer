package gui.createforms;

import db.DBInfo;
import gui.custom.DBRow;
import gui.custom.InsertTextField;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Author: William Robert Howerton III
 * Created: 10/17/2015
 */
public class CreateBOM extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlEditBOM;
    private JComboBox cbChildPartID;
    private JTree treeBOMHierarchy;
    private JComboBox cbParentPartID;
    private JButton btnAdd;
    private JButton undoButton;
    private JButton btnCommit;
    private JButton btnClearFields;
    private InsertTextField fldStep;
    private InsertTextField fldQty;
    private InsertTextField fldHrlyCost;
    private InsertTextField fldHrEst;
    private InsertTextField fldEmpAssigned;
    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Stack<DefaultMutableTreeNode> nodeStack = new Stack<DefaultMutableTreeNode>();

    public CreateBOM() {

        this.setContentPane(pnlMain);
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((rowStack != null) && (nodeStack != null)) {
                    rowStack.pop();
                    nodeStack.pop();
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

        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeRowFromFields();
            }
        });
        fillCbBoxes();
        this.pack();

    }

    private void fillCbBoxes() {

        ArrayList<Object> itemIDs = DBInfo.getTabToPkVals().get("PART");
        if (itemIDs == null) {
            cbChildPartID.addItem("");
            cbParentPartID.addItem("");
        } else {
            for (Object i : itemIDs) {
                cbChildPartID.addItem(i);
                cbParentPartID.addItem(i);
            }
        }
    }

    private void makeRowFromFields() {

        StringBuilder nodeString = new StringBuilder();
        Integer childKey = (Integer) cbChildPartID.getSelectedItem();
        Integer parentKey = (Integer) cbParentPartID.getSelectedItem();
        Integer step = fldStep.getInt();
        Integer qty = fldQty.getInt();
        Double hrlyCost = fldHrlyCost.getDouble();
        Double hrEst = fldHrEst.getDouble();
        Integer empID = fldEmpAssigned.getInt();

        nodeString.append("Parent: ").append(parentKey).append("| Child: ").append(childKey).append("| step: ").append(step)
                .append("| qty: ").append(qty).append("| hrly cost: ").append(hrlyCost).append("| hr est: ").append(hrEst).append("| emp: ").append(empID)
                .append("\n");
        DBRow temp = new DBRow("BOM", step, qty, parentKey, childKey, hrlyCost, hrEst, empID);
        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(nodeString.toString());
        rowStack.push(temp);
        nodeStack.push(tempNode);

        //TODO: add temp node to tree
    }

    private void clearFields() {

        Component[] components = pnlEditBOM.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i].getClass().getName().toString().equals("gui.custom.InsertTextField")) {
                InsertTextField temp = (InsertTextField) components[i];
                temp.setText("");
            }
        }
    }
}
