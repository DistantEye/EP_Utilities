package com.github.distanteye.ep_utils.ui.validators;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Validates any code for a specified class (the class must have a static method Exists(String name)
 * @author Vigilant
 *
 */
public class ExistsValidator extends InputVerifier {
	private Class<?> c;
	private Method m;
	
	public ExistsValidator(String name)
	{
		try {
			this.c = Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("No such class(" + name + ")");
		}
		
		Class<?>[] cArg = new Class[1];
        cArg[0] = String.class;
        
		try {
			m = c.getMethod("exists",cArg);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Class(" + name + ") lacks exists method!");
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Could not access exists method for Class(" + name + ")!");
		}
	}
	
	@Override
	public boolean shouldYieldFocus(JComponent input)
	{
		if (((JTextField)input).getText().length() == 0)
		{
			return true; // don't leave users stuck if the field is blank
		}
		else
		{
			if (super.shouldYieldFocus(input))
			{
				return true;
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Please enter a valid entry for this field (or blank)", "Invalid Field", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
	}

	@Override
	public boolean verify(JComponent input) {
		JTextField field = (JTextField)input;
		
		try {
			return (boolean) m.invoke(null, field.getText());
		} catch (IllegalAccessException e1) {
			throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
		} catch (IllegalArgumentException e1) {
			throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
		} catch (InvocationTargetException e1) {
			throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
		}
	}
}
