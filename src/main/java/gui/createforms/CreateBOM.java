package gui.createforms;

import db.DBIO;
import db.DBInfo;
import gui.custom.DBRow;
import gui.custom.InsertTextField;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private InsertTextField fldEmpAssigned;
    private JButton btnPreview;
    private JComboBox cbEmployeeNames;
    private Stack<DBRow> rowStack = new Stack<DBRow>();
    private Stack<DefaultMutableTreeNode> nodeStack = new Stack<DefaultMutableTreeNode>();
    private Hashtable<Object, String> partPkToName;
    private Hashtable<String, Object> partNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<String, Object> empNameToPk = new Hashtable<String, Object>(10);
    private Hashtable<Object, String> empPkToName;

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

        partPkToName = DBInfo.getPartPkToName();
        Enumeration e = partPkToName.keys();

        while (e.hasMoreElements()) {
            Object next = e.nextElement();
            partNameToPk.put(partPkToName.get(next), next);
        }
        empPkToName = DBInfo.getEmpPkToName();
        Enumeration p = empPkToName.keys();

        while (p.hasMoreElements()) {
            Object next = p.nextElement();
            empNameToPk.put(empPkToName.get(next), next);
        }
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

    private void fillCbBoxes() {

        if (partPkToName == null) {
            cbChildPartNames.addItem("");
            cbParentPartNames.addItem("");
        } else {
            Enumeration e = partPkToName.keys();

            while (e.hasMoreElements()) {
                String partName = partPkToName.get(e.nextElement());
                cbChildPartNames.addItem(partName);
                cbParentPartNames.addItem(partName);
                //TODO: Sort alphabetically
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

    private void makeRowFromFields() {

        StringBuilder nodeString = new StringBuilder();

        Object childKey = partNameToPk.get(cbChildPartNames.getSelectedItem().toString());
        Object parentKey = partNameToPk.get(cbParentPartNames.getSelectedItem().toString());
        Integer step = fldStep.getInt();
        Integer qty = fldQty.getInt();
        Double hrlyCost = fldHrlyCost.getDouble();
        Double hrEst = fldHrEst.getDouble();
        Object empID = empNameToPk.get(cbEmployeeNames.getSelectedItem().toString());

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
