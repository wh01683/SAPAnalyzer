package gui.custom;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class FormValidation {

    private static Border originalBorder;

    public static void storeDefaultBorder(Border border) {
        originalBorder = border;
    }

    public static boolean validateField(Method parseMethod, JTextComponent field, String message) {
        try {
            boolean result = true;
            // User did not enter anything into the field.
            if (field.getText().equals("")) {
                setFieldError(field);
                JOptionPane.showMessageDialog(null, "This field is required.");
                return false;
            }
            if (parseMethod != null) {
                if (parseMethod.getReturnType().toString().equals("boolean")) {
                    System.out.println("is boolean value");
                    // Call the correct method to try and convert the input field string into the correct data type
                    result = Boolean.valueOf(parseMethod.invoke(null, field.getText()).toString());
                    if (!result) {
                        setFieldError(field);
                        JOptionPane.showMessageDialog(null, message);
                    }
                } else {
                    parseMethod.invoke(null, field.getText());
                }
            }
            if (result) {
                // Remove the red error border
                field.setBorder(originalBorder);
            }
            return result;
        } catch (IllegalAccessException e) {
            System.out.println("Error calling method: " + parseMethod);
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            setFieldError(field);
            JOptionPane.showMessageDialog(null, message);
            return false;
        }
    }

    /**
     * Validate the specified field using the give method. So for example to validate that a JTextField
     * called `field` contains an integer value you would call the validateField method like so:
     * <p>
     * try{
     * validateField(Integer.class.getMethod("parseInt", String.class), field);
     * } catch( NoSuchMethodException ex){}
     *
     * @param parseMethod the method to use to convert the field value to the correct data type. If the parseMethod is
     *                    null then it will just make sure that the field has a value.
     * @param field       the field to validate.
     * @return boolean true, if the field is valid, false otherwise.
     */
    public static boolean validateField(Method parseMethod, JTextComponent field) {
        return validateField(parseMethod, field, "Enter a valid value.");
    }

    /**
     * Helper method to add a default error border of red.
     *
     * @param component component to border in red.
     */
    public static void setFieldError(JComponent component) {
        setFieldError(component, Color.RED);
    }

    /**
     * Adds a border to the specified component to indicate that it has erred.
     *
     * @param component the component to add the border to
     */
    public static void setFieldError(JComponent component, Color color) {
        component.grabFocus();
        component.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED, color, color));
    }

}
