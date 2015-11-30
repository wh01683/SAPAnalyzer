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

    /**
     * Creates a new insert text field using the SQL type code.
     * @param colType SQL Integer class code.
     */
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

    /**
     * Returns a cast object using the class the field was created with.
     * @return Returns the cast object.
     */
    public Object getObject() {
        return fieldType.cast(super.getText());
    }

    /**
     * Obtains the integer contained in the field's text using the static Integer parseInt function.
     * @return Returns the integer contained in the field's text.
     */
    public Integer getInt() {
        try {
            return Integer.parseInt(super.getText());
        } catch (NumberFormatException n) {
            System.out.printf("Error parsing integer from InsertTextField %s.", this.getName());
            return null;
        }
    }

    /**
     * Obtains the double contained in the field's text using the static Double parseDouble function.
     * @return Returns the double contained in the field's text.
     */
    public Double getDouble() {
        try {
            return Double.parseDouble(super.getText());
        } catch (NumberFormatException n) {
            System.out.printf("Error parsing double from InsertTextField %.", this.getName());
            return null;
        }
    }


    /**
     * Obtains a single character from the field's text. Contains error checking to
     * verify field does not have more than one character.
     * @return Returns a single Character located at the field's index 0.
     */
    public Character getChar() {
        if (super.getText().length() > 1) {
            System.out.printf("Attempted to get char of length %d from InsertTextField %s.", super.getText().length(), this.getName());
            return null;
        } else {
            return super.getText().charAt(0);
        }
    }
}
