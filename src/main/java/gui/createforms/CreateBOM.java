package gui.createforms;

import db.DBInfo;
import gui.DBRow;

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
    private JTextField fldBOMStep;
    private JTextField fldBOMQty;
    private JTextField fldBOMHrlyCost;
    private JTextField fldBOMHrEst;
    private JTree treeBOMHierarchy;
    private JComboBox cbParentPartID;
    private JTextField fldEmpAssigned;
    private JButton btnAdd;
    private JButton undoButton;
    private JButton btnCommit;
    private JButton btnClearFields;
    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Stack<DefaultMutableTreeNode> nodeStack = new Stack<DefaultMutableTreeNode>();

    public CreateBOM() {

        fillCbBoxes();
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
    }

    private void fillCbBoxes() {

        ArrayList<Object> itemIDs = DBInfo.getTabToPkVals().get("PART");
        for (Object i : itemIDs) {
            cbChildPartID.addItem(i);
            cbParentPartID.addItem(i);
        }
    }

    private void makeRowFromFields() {

        StringBuilder nodeString = new StringBuilder();
        Integer childKey = (Integer) cbChildPartID.getSelectedItem();
        Integer parentKey = (Integer) cbParentPartID.getSelectedItem();
        Integer step = Integer.parseInt(fldBOMStep.getText());
        Integer qty = Integer.parseInt(fldBOMQty.getText());
        Integer hrlyCost = Integer.parseInt(fldBOMHrlyCost.getText());
        Integer hrEst = Integer.parseInt(fldBOMHrEst.getText());
        Integer empID = Integer.parseInt(fldEmpAssigned.getText());

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
            if (components[i].getClass().getName().toString().equals("javax.swing.JTextField")) {
                JTextField temp = (JTextField) components[i];
                temp.setText("");
            }
        }
    }
}
