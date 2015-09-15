/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * MappedComponent is a Wrapper for JComponents when used in 
 * a mappedComponents list, typically via ExtJPanel. It adds on functionality
 * that lets a UI make a uniform update() call that is resolved into pulls or pushes
 * from arbitrary data sources or TextFields in the UI
 * 
 * The class is immutable (although the underlying values in the data referenced can change)
 * 
 * @author Vigilant
 *
 */
public class MappedComponent {
	private DataFlow df;
	private AccessWrapper<String> linkedData;
	private JComponent comp;
		
	
	/**
	 * @param df Valid enum for what direction data flows between comp and linkedData
	 * 				Use DataFlow.STATIC for non-updating components
	 * @param linkedData AccessWrapper for interacting with the backend/character-data. This can be null if df==STATIC
	 * @param comp the JComponent being wrapped. If df is not STATIC, JComponent must extend JTextComponent
	 */
	public MappedComponent(DataFlow df, AccessWrapper<String> linkedData, JComponent comp) {
		super();
		this.df = df;		
		this.comp = comp;
		
		if (df!=DataFlow.STATIC && !(comp instanceof JTextComponent || comp instanceof JButton))
		{
			throw new IllegalArgumentException("Non-static DataFlow components must be JTextComponents or JButtons for MappedComponent");
		}
		
		this.linkedData = linkedData;
	}

	public JComponent getComp() {
		return comp;
	}

	/**
	 * Cause any attached JTextComponents to refresh their displayed text via pulling from linkedData
	 */
	public void refresh()
	{
		// STATIC Components don't update or refresh
		if (df==DataFlow.STATIC)
		{
			return;
		}
		else
		{	
			setCompText(linkedData.getValue());
		}
	}

	public void update()
	{
		// STATIC Components don't update
		if (df==DataFlow.STATIC)
		{
			return;
		}
		
		if (df == DataFlow.PULL)
		{
			setCompText(linkedData.getValue());
		}
		else
		{
			// PUSH
			linkedData.setValue(getCompText());
		}
	}
	
	/**
	 * Resolves whether comp is JTextComponent or JButton and calls setText accordingly
	 * @param val Valid string to set on comp
	 */
	private void setCompText(String val)
	{
		if (comp instanceof JTextComponent)
		{
			((JTextComponent)comp).setText(val);
		}
		else if (comp instanceof JButton)
		{
			((JButton)comp).setText(val);
		}
		else
		{
			throw new IllegalStateException("Could not recognize what subclass JComponent comp is");
		}
	}
	
	/**
	 * Resolves whether comp is JTextComponent or JButton and calls getText accordingly
	 * @return Text contents of comp
	 */
	private String getCompText()
	{
		if (comp instanceof JTextComponent)
		{
			return ((JTextComponent)comp).getText();
		}
		else if (comp instanceof JButton)
		{
			return ((JButton)comp).getText();
		}
		else
		{
			throw new IllegalStateException("Could not recognize what subclass JComponent comp is");
		}
	}
	
	/**
	 * The nature of the DataFlow between comp and linkedData
	 * If STATIC, no behavior during updates
	 * If PULL, comp is setText to the result of linkedData.getValue()
	 * If PUSH, linkedData is set to comp.getText()
	 * @author Vigilant
	 *
	 */
	public enum DataFlow
	{
		PULL,PUSH,STATIC
	}
}
