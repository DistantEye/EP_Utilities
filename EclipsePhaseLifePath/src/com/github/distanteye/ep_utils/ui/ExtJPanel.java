/**
 * 
 */
package com.github.distanteye.ep_utils.ui;

import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.distanteye.ep_utils.core.Utils;
import com.github.distanteye.ep_utils.wrappers.MappedComponent;

/**
 * Basic extension of JPanel which adds helper methods to make adding other Swing objects.
 * to it easier. Has the ability to store and recall Mapped JComponents, to give key/name-based access
 * 
 * Most methods (when applicable) return the JComponent added/modified/created,
 * to support chaining
 * 
 * All JPanel constructors are preserved for consistency
 * 
 * No intention of currently implementing serialization for this
 * 
 * @author Vigilant
 */
@SuppressWarnings("serial")
public class ExtJPanel extends JPanel {
	private HashMap<String,MappedComponent> mappedComponents;
	protected GridBagConstraints cons;	
	private ArrayList<JComponent> children;
	
	private ArrayList<MappedComponent> orderedComponentList;
	
	/**
	 * Creates a new ExtJPanel with a double buffer and a flow layout.
	 */
	public ExtJPanel() {
		super();
	}

	/**
	 * Create a new buffered ExtJPanel with the specified layout manager
	 * @param layout the LayoutManager to use
	 */
	public ExtJPanel(LayoutManager layout) {
		super(layout);
	}

	/**
	 * Creates a new ExtJPanel with FlowLayout and the specified buffering strategy. If isDoubleBuffered is true, the JPanel will use a double buffer.
	 * @param isDoubleBuffered a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public ExtJPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	/**
	 * Creates a new ExtJPanel with the specified layout manager and buffering strategy
	 * @param layout the LayoutManager to use
	 * @param isDoubleBuffered a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public ExtJPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}
	
	
	protected void init()
	{
		this.children = new ArrayList<JComponent>();
		this.cons = new GridBagConstraints();
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		this.mappedComponents = new HashMap<String,MappedComponent>();
		orderedComponentList = new ArrayList<MappedComponent>();
		
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
	 * Shorthand to add new components to the UI tab
	 * @param comp Component to add to UI
	 * @return The component added
	 */
	public JComponent addC(JComponent comp) {
		this.add(comp,cons);
		this.children.add(comp);
		
		return comp;
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @return The component added
	 */
	public JComponent addC(JComponent comp, int x, int y) {
		cons.gridx = x;
		cons.gridy = y;
		this.addC(comp);
		this.children.add(comp);
		
		return comp;
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords with height and width
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param gridHeight valid number or GridBagConstraints constant
	 * @param gridWidth valid number or GridBagConstraints constant
	 * @return The component added
	 */
	public JComponent addC(JComponent comp, int x, int y, int gridHeight, int gridWidth) {
		int oldHeight = cons.gridheight;
		int oldWidth = cons.gridwidth;
		cons.gridheight = gridHeight;
		cons.gridwidth = gridWidth;
		this.addC(comp,x,y);
		cons.gridheight = oldHeight;
		cons.gridwidth = oldWidth;
		
		return comp;
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords with height, width, and fill value
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param gridHeight valid number or GridBagConstraints constant
	 * @param gridWidth valid number or GridBagConstraints constant
	 * @param fill valid number or GridBagConstraints constant
	 * @return The component added
	 */
	public JComponent addC(JComponent comp, int x, int y, int gridHeight, int gridWidth, int fill) {
		int oldFill = cons.fill;
		cons.fill = fill;
		this.addC(comp,x,y,gridHeight,gridWidth);
		cons.fill = oldFill;
		
		return comp;
	}

	public ArrayList<JComponent> getChildren() {
		return children;
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
			for (ExtJPanel j : this.getExtJPanelChildren())
			{
				j.hasComponent(name);
			}
			
			return false;
		}
	}
	
	/**
	 * Returns either the mapped component of the given name, or null
	 * @param name Name of the mapped component to look for
	 * @return the MappedComponent if it exists, null otherwise
	 */
	public MappedComponent getMappedComponent(String name)
	{
		if (this.mappedComponents.containsKey(name))
		{
			return this.mappedComponents.get(name);
		}
		else
		{
			for (ExtJPanel j : this.getExtJPanelChildren())
			{
				
				if ( j.hasComponent(name) )
				{
					return j.getMappedComponent(name);
				}				
			}
			
			return null;
		}
	}
	
	/**
	 * Shortcut method that gets the MappedComponent matching name, then returns its
	 * underlying JComponent
	 * @param name Name of the mapped component to look for
	 * @return The underlying JComponent for the MappedComponent named, or null if none found
	 */
	public JComponent getComponentVal(String name)
	{
		MappedComponent temp = getMappedComponent(name);
		
		if (temp == null)
		{
			return null;
		}
		else
		{
			return temp.getComp();
		}
	}
	
	/**
	 * Returns a list of keys for the the mappedComponents list
	 * Keys are returned rather than values since keys sometimes themselves can be needed information
	 * And themselves can be used to access the values
	 * @return ArrayList of Strings for the component keys
	 */
	public ArrayList<String> getMappedComponentKeys()
	{
		ArrayList<String> result = new ArrayList<String>();
		for (String key : mappedComponents.keySet())
		{
			result.add(key);
		}
		return result;
	}
	
	/**
	 * Calls up a MappedComponent and instructs it to update()
	 * @param name Name/key of MappedComponent
	 */
	public void updateComp(String name)
	{
		getMappedComponent(name).update();
	}
	
	/**
	 * Calls up all MappedComponents, in insertion order, and calls their update() method.
	 * Will only effect components owned by this 
	 * @param andChildren If true, will run this recursively on any children that are ExtJPanels
	 */
	public void updateAllComps(boolean andChildren)
	{
		for (MappedComponent m : orderedComponentList)
		{
			m.update();
		}
		
		if (andChildren)
		{
			for (ExtJPanel child : getExtJPanelChildren())
			{
				child.updateAllComps(true);
			}
		}
	}
	
	/**
	 * Searches through children list and returns only the ones that are ExtJPanels (or subclass)
	 * @return ArrayList of ExtJPanel children of this ExtJPanel
	 */
	protected ArrayList<ExtJPanel> getExtJPanelChildren()
	{
		ArrayList<ExtJPanel> result = new ArrayList<ExtJPanel>();
		
		for (JComponent child : children)
		{
			if (child instanceof ExtJPanel)
			{
				result.add((ExtJPanel)child);
			}
		}
		
		return result;
	}
	
	/**
	 * Adds a component to this Panel's mappedComponet list
	 * @param name Name/key for the component
	 * @param comp MappedComponent to add
	 * @return the component added
	 */
	public MappedComponent putMapped(String name, MappedComponent comp)
	{
		orderedComponentList.add(comp); // add reference to list so we have ordered result
		return this.mappedComponents.put(name, comp);
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
			
			JComponent temp = this.getComponentVal(name);
			
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
	public int getTextFIntVal(String name)
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
		JComponent temp = this.getComponentVal(name);		
		
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
	
}
