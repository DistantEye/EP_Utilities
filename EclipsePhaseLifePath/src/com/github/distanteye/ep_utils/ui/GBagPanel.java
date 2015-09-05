package com.github.distanteye.ep_utils.ui;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * GridBag flavor of ExtJPanel to add many helper methods related to adding, 
 * tracking, returning components managed in a GridBagLayout
 * 
 * Most methods (when applicable) return the JComponent added/modified/created,
 * to support chaining
 * 
 * No intention of currently implementing serialization for this
 * 
 * @author Vigilant
 */
@SuppressWarnings("serial")
public class GBagPanel extends ExtJPanel {			
	
	/**
	 * Creates a new UIPanel with a double buffer and a flow layout.
	 */
	public GBagPanel() {
		super(new GridBagLayout());
		init();
	}

	/**
	 * Creates a new UIPanel with FlowLayout and the specified buffering strategy. If isDoubleBuffered is true, the UIPanel will use a double buffer.
	 * @param isDoubleBuffered - a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public GBagPanel(boolean isDoubleBuffered) {
		super(new GridBagLayout(),isDoubleBuffered);
		init();
	}

	
	/**
	 * Groups common parts of object initialization away from the separate constructors
	 */
	protected void init()
	{
		super.init();		
	}

	/**
	 * Inserts a spacer to signify the end of components for this row (effects layout so it doesn't center)
	 * @param x nonzero int
	 * @param y nonzero int
	 */
	public void endRow(int x, int y)
	{
		cons.weightx = 100.0;
		addLabel(x,y,"");
		cons.weightx = 1.0;
	}
	
	/**
	 * Inserts a spacer to signify the end of components vertically (effects layout so it doesn't center)
	 * @param x nonzero int
	 * @param y nonzero int
	 */
	public void endVertical(int x, int y)
	{ 
		cons.weighty = 100.0;
		addLabel(x,y,"");
		cons.weighty = 1.0;
	}
		
	/**
	 * Adds a JLabel JTextField pair (each weight 1 ), and adds the textField to the mappedComponents list
	 * using the labelText for a key.
	 * @param editState Whether the JTextField is editable or fixed
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param labelText Label to display
	 * @param mapName The key for the TextField in MappedComponents
	 * @param cols number of cols for the text field
	 * @param value The text to display in the field
	 * @param o Whether the label/textfield pair is oriented horizontally or vertically
	 * @param parentUIForTextChangeListener if not null, will attach a TextChangeListener to this TextField, which reports back to the UI passed
	 * 
	 * @return The component created
	 */
	public JTextField addMappedTF(EditState editState, int x, int y, String labelText, String mapName, int cols, String value, 
									Orientation o, UI parentUIForTextChangeListener)
	{
		int newX,newY;
		
		if (o == Orientation.HORIZONTAL)
		{
			newX = x+1;
			newY = y;
		}
		else
		{
			newX = x;
			newY = y+1;
		}
		
		if (editState == EditState.FIXED)
		{
			parentUIForTextChangeListener = null; // sanity check to prevent odd behavior
		}
		
		addLabel(x,y,labelText);
		JTextField temp = addTextF(newX,newY,value,cols,parentUIForTextChangeListener);
		
		if (editState == EditState.FIXED)
		{
			temp.setEditable(false);
		}
		this.putMapped(mapName, temp);
		
		return temp;
	}
	
	/**
	 * Shorthand command to add label at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for label
	 * @return The component created
	 */
	public JLabel addLabel(int x, int y, String text)
	{
		cons.gridx = x;
		cons.gridy = y;
		JLabel temp = new JLabel(text);
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Button at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for Button
	 * @return The component created
	 */
	public JButton addButton(int x, int y, String text)
	{
		cons.gridx = x;
		cons.gridy = y;
		JButton temp = new JButton(text);
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Button at coordinates with text add adds it to the mapped components list
	 * with key = text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for Button
	 * @return The component created
	 */
	public JButton addMappedButton(int x, int y, String text)
	{
		JButton temp = addButton(x,y,text);
		this.putMapped(text, temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Text Field at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param value the default value for the text field
	 * @param cols Number of Columns
	 * @param parentUIForTextChangeListener If not null, will add a TextChangeListener, which will trigger updates on this UI
	 * @return The component created
	 */
	public JTextField addTextF(int x, int y, String value, int cols, UI parentUIForTextChangeListener)
	{
		cons.gridx = x;
		cons.gridy = y;
		JTextField temp = new JTextField(value, cols);
		
		// this prevents the common issue of the text fields turning into slits
		temp.setMinimumSize(temp.getPreferredSize());		
		
		addC(temp);
		
		if (parentUIForTextChangeListener != null)
		{
			temp.getDocument().addDocumentListener(new TextChangeListener(parentUIForTextChangeListener,temp));			
		}
		
		return temp;
	}
	
	/**
	 * Shorthand command to add Text Area at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param rows Number of Rows
	 * @param cols Number of Columns
	 * @return The component created
	 */
	public JTextArea addTextArea(int x, int y, int rows, int cols)
	{
		cons.gridx = x;
		cons.gridy = y;
		JTextArea temp = new JTextArea(rows,cols);
		addC(temp);
		return temp;
	}
	
}
