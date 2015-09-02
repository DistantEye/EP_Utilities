import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * While often DocumentListeners are just a class inside the UI, both code reuse
 * and some concerns about keeping code calls short led to this class being made on
 * the outside.
 * 
 * @author Vigilant
 *
 */
public class TextChangeListener implements DocumentListener {
	private UI parent;
	private JComponent field; // optional addition, set this to support InputVerifier checking 
							 // and other integrations
	
	public TextChangeListener(UI parent, JComponent field)
	{
		this.parent = parent;
		this.field = field;
	}
	
	public void callUpdateIfReady()
	{
		if (field==null)
		{
			// we can't do anything if field is null, just trigger update
			parent.update();
		}
		else
		{
			// we see if this class has a InputVerifier, if it does, we only call update
			// if it would return true
			InputVerifier validator = field.getInputVerifier();
			
			if (validator==null || validator.verify(field))
			{
				parent.update();
			}						
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		callUpdateIfReady();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		callUpdateIfReady();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		callUpdateIfReady();
	}

}
