import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Validates that a number has been entered
 * @author Vigilant
 *
 */
public class NumericValidator extends InputVerifier {	
	
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
		JTextField temp = (JTextField)input;
		
		return Utils.isInteger(temp.getText());
	}
}
