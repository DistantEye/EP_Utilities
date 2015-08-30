import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class GridBagUIPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8550646555771187030L;
	
	private HashMap<String,JComponent> mappedComponents;
	private GridBagConstraints cons;	
	private GridBagLayout layout;
	private ArrayList<JComponent> children;

	
	/**
	 * Creates a new UIPanel with a double buffer and a flow layout.
	 */
	public GridBagUIPanel() {
		super();
		init();
	}

	/**
	 * Create a new buffered UIPanel with the specified layout manager
	 * @param layout - the LayoutManager to use
	 */
	public GridBagUIPanel(LayoutManager layout) {
		super(layout);
		init();
	}

	/**
	 * Creates a new UIPanel with FlowLayout and the specified buffering strategy. If isDoubleBuffered is true, the UIPanel will use a double buffer.
	 * @param isDoubleBuffered - a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public GridBagUIPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		init();
	}

	/**
	 * 
	 * @param layout - the LayoutManager to use
	 * @param isDoubleBuffered - a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public GridBagUIPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		init();
	}
	
	/**
	 * Groups common parts of object initialization away from the separate constructors
	 */
	public void init()
	{
		this.mappedComponents = new HashMap<String,JComponent>();
		this.children = new ArrayList<JComponent>();
		this.cons = new GridBagConstraints();
		this.layout = new GridBagLayout();
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);        
        this.setLayout(layout);
        
        cons.ipadx = 5;
		cons.ipady = 5;
		cons.anchor = GridBagConstraints.NORTHWEST;
		cons.fill = GridBagConstraints.NONE;
		cons.weighty = 1.0;
		cons.weightx = 1.0;
		cons.gridheight = 1;
		cons.gridwidth = 1;
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
	 * using the labelText for a key
	 * 
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param labelText Label to display
	 * @param updateListen If not null, will add listener passed in to the text field
	 * @param cols number of cols for the text field
	 */
	public void addMappedTF(int x, int y, String labelText, int cols, DocumentListener updateListen)
	{
		addMappedTF(x,y,labelText,labelText,cols,updateListen);
	}
	
	/**
	 * Adds a JLabel JTextField pair (each weight 1 ), and adds the textField to the mappedComponents list
	 * using the labelText for a key. This version specifies a different String for the label and the map key
	 * 
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param labelText Label to display
	 * @param mapName String to use for the map key for the text field
	 * @param updateListen If not null, will add listener passed in to the text field
	 * @param cols number of cols for the text field
	 */
	public void addMappedTF(int x, int y, String labelText, String mapName, int cols, DocumentListener updateListen)
	{
		addLabel(x,y,labelText);
		this.mappedComponents.put(mapName, addTextF(x+1,y,cols,updateListen));
	}
	
	/**
	 * Adds a JLabel JTextField pair (each weight 1 ), and adds the textField to the mappedComponents list
	 * using the labelText for a key. The text field is not edittable
	 * 
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param labelText Label to display
	 * @param value The text to display in the field
	 * @param cols number of cols for the text field
	 * @param horiztonal Whether the pair is arranged horizontally (vertically if false)
	 */
	public void addMappedFixedTF(int x, int y, String labelText, String value, int cols, boolean horizontal)
	{
		int newX,newY;
		
		if (horizontal)
		{
			newX = x+1;
			newY = y;
		}
		else
		{
			newX = x;
			newY = y+1;
		}
		
		addLabel(x,y,labelText);
		JTextField temp = addTextF(newX,newY,value,cols,null);
		temp.setEditable(false);
		this.mappedComponents.put(labelText, temp);
	}
	
	/**
	 * Shorthand command to add label at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for label
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
		this.mappedComponents.put(text, temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Text Field at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param value the default value for the text field
	 * @param cols Number of Columns
	 * @param updateListen If not null, will add listener passed in to the text field
	 * @return The component created
	 */
	public JTextField addTextF(int x, int y, String value, int cols, DocumentListener updateListen)
	{
		cons.gridx = x;
		cons.gridy = y;
		JTextField temp = new JTextField(value, cols);
		
		// this prevents the common issue of the text fields turning into slits
		temp.setMinimumSize(temp.getPreferredSize());

		if (updateListen != null)
		{
			temp.getDocument().addDocumentListener(updateListen);
		}
		
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Text Field at coordinates with text. No value set in this version
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param cols Number of Columns
	 * @param updateListen If not null, will add listener passed in to the text field
	 * @return The component created
	 */
	public JTextField addTextF(int x, int y, int cols, DocumentListener updateListen)
	{
		return this.addTextF(x, y, "", cols, updateListen);
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
	
	/**
	 * Shorthand to add new components to the UI tab
	 * @param comp Component to add to UI
	 */
	public void addC(JComponent comp) {
		this.add(comp,cons);
		this.children.add(comp);
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * 
	 */
	public void addC(JComponent comp, int x, int y) {
		cons.gridx = x;
		cons.gridy = y;
		this.addC(comp);
		this.children.add(comp);
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords with height and width
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param gridHeight valid number or GridBagConstraints constant
	 * @param gridWidth valid number or GridBagConstraints constant
	 */
	public void addC(JComponent comp, int x, int y, int gridHeight, int gridWidth) {
		int oldHeight = cons.gridheight;
		int oldWidth = cons.gridwidth;
		cons.gridheight = gridHeight;
		cons.gridwidth = gridWidth;
		this.addC(comp,x,y);
		cons.gridheight = oldHeight;
		cons.gridwidth = oldWidth;
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords with height, width, and fill value
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param gridHeight valid number or GridBagConstraints constant
	 * @param gridWidth valid number or GridBagConstraints constant
	 * @param fill valid number or GridBagConstraints constant
	 */
	public void addC(JComponent comp, int x, int y, int gridHeight, int gridWidth, int fill) {
		int oldFill = cons.fill;
		cons.fill = fill;
		this.addC(comp,x,y,gridHeight,gridWidth);
		cons.fill = oldFill;
	}
	
	/**
	 * Returns whether either this Panel or any of it's children have the component in question
	 * @param name Name of the mapped component to look for
	 * @return true or false as appropriate
	 */
	public boolean hasComponent(String name)
	{
		if (this.mappedComponents.containsKey(name))
		{
			return true;
		}
		else
		{
			for (JComponent j : this.children)
			{
				if (j.getClass().getSimpleName().equalsIgnoreCase("GridBagUIPanel"))
				{
					GridBagUIPanel temp = (GridBagUIPanel)j;
					return temp.hasComponent(name);
				}
			}
			
			return false;
		}
	}
	
	/**
	 * Returns eitehr the mapped component of the given name, or null
	 * @param name Name of the mapped component to look for
	 * @return the JComponent if it exists, null otherwise
	 */
	public JComponent getComponent(String name)
	{
		if (this.mappedComponents.containsKey(name))
		{
			return this.mappedComponents.get(name);
		}
		else
		{
			for (JComponent j : this.children)
			{
				if (j.getClass().getSimpleName().equalsIgnoreCase("GridBagUIPanel"))
				{
					GridBagUIPanel temp = (GridBagUIPanel)j;
					if ( temp.hasComponent(name) )
					{
						return temp.getComponent(name);
					}
				}
			}
			
			return null;
		}
	}
	
	/**
	 * Helper method, searches mappedComponents for a JTextField with name, and returns it properly cast to JTextField
	 * 
	 * @param name Valid name 
	 * @return The JTextField of matching name, or null if no matching JTextField exists
	 */
	public JTextField getTextF(String name)
	{
		// does it exist?
		if (!this.hasComponent(name) )
		{
			return null;
		}
		else
		{
			
			JComponent temp = this.getComponent(name);
			
			// is it the right type?
			if ( !temp.getClass().getSimpleName().equalsIgnoreCase("jtextfield") )
			{
				return null;
			}
			else
			{
				return (JTextField)temp;
			}
			
		}
		
	}
	
	/**
	 * Returns the integer value stored in a mapped textfield, if applicable
	 * @param name Textfield to search for
	 * @return Either the appropriate int value or 0 if no valid integer bearing field could be found
	 */
	public int getTextFVal(String name)
	{
		JTextField temp = getTextF(name);		
		
		// does it exist and is it a number
		if (temp == null || !Utils.isInteger(temp.getText()))
		{
			return 0;
		}
		else
		{
			return Integer.parseInt(temp.getText());
		}
	}
	
	/**
	 * Attempts to retrieve a mapped text field with the matching name and change its text value to val
	 * 
	 * This function will fail silently if the field doesn't exist
	 * 
	 * @param name Name of the text field to look for
	 * @param val Value to setText to
	 */
	public void setTextF(String name, String val)
	{
		JTextField temp = getTextF(name);		
		
		// does it exist and is it a number
		if (temp == null)
		{
			return;
		}
		else
		{
			temp.setText(val);
		}
	}
	
	/**
	 * Attempts to retrieve a mapped text field with the matching name and change its text value to val
	 * This version takes an int value for convenience
	 * @param name Name of the text field to look for
	 * @param val int Value to setText to, will be converted to string
	 */
	public void setTextF(String name, int val)
	{
		this.setTextF(name, ""+val);
	}
	
	/**
	 * Attempts to retrieve a mapped button with the matching name and change its display text to val
	 * 
	 * This function will fail silently if the button doesn't exist
	 * 
	 * @param name Name of the button to look for
	 * @param val Value to setText to
	 */
	public void setButtonText(String name, String val)
	{
		JComponent temp = mappedComponents.get(name);		
		
		// does it exist and is it a number
		if (temp == null || !temp.getClass().getSimpleName().equalsIgnoreCase("JButton"))
		{
			return;
		}
		else
		{
			((JButton)temp).setText(val);
		}
	}
	
	/**
	 * Adds a component to this Panel's mappedComponet list
	 * @param name Name/key for the component
	 * @param comp Component to add
	 * @return the component added
	 */
	public JComponent put(String name, JComponent comp)
	{
		return this.mappedComponents.put(name, comp);
	}
}
