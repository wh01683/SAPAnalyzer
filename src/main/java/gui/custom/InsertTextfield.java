package gui.custom;

import db.Utility;

import javax.swing.*;

/**
 * Author: William Robert Howerton III
 * Created: 10/19/2015
 */
public class InsertTextField extends JTextField {


    private Class<?> fieldType = null;

    public InsertTextField() {
        super();
    }

    public InsertTextField(int colType) {
        super();
        this.fieldType = Utility.ConvertType(colType);
        this.setText("");

    }

    @Override
    public String getText() {
        return super.getText();
    }

    @Override
    public void setEditable(boolean b) {
        super.setEditable(b);
    }

    @Override
    public void setText(String t) {
        super.setText(t);
    }


    public Object getObject() {
        return fieldType.cast(super.getText());
    }

    public Integer getInt() {
        try {
            return Integer.parseInt(super.getText());
        } catch (NumberFormatException n) {
            System.out.printf("Error parsing integer from InsertTextField %s.", this.getName());
            return null;
        }
    }

    public Double getDouble() {
        try {
            return Double.parseDouble(super.getText());
        } catch (NumberFormatException n) {
            System.out.printf("Error parsing double from InsertTextField %.", this.getName());
            return null;
        }
    }


    public Character getChar() {
        if (super.getText().length() > 1) {
            System.out.printf("Attempted to get char of length %d from InsertTextField %s.", super.getText().length(), this.getName());
            return null;
        } else {
            return super.getText().charAt(0);
        }
    }
}
