package com.github.distanteye.ep_utils.containers;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Container for Stats, a general class for encapsulating stats
 * Stats are, at minimum, a name and a non-negative integer value
 * @author Vigilant
 */
public class Stat {
	private String name;
	private int value;
	
	/**
	 * @param name Name of Stat type
	 * @param value Current value, should be non-negative
	 */
	public Stat(String name, int value) {
		super();
		this.name = name;
		this.setValue(value);
	}
	
	public String toString()
	{
		return this.name + " " + this.value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Sets Stat value to val
	 * @param val Valid non negative integer value
	 */
	public void setValue(int val) {
		if (val < 0)
		{
			throw new IllegalArgumentException("val must be non-negative integer");
		}
		else
		{
			this.value = val;
		}
	}
	
	public void addValue(int value) {
		setValue(getValue() + value);
	}
	
	public String toXML(int tab)
	{
		return Utils.tab(tab) + "<stat>\n"+
						Utils.tab(tab+1) + "<name>" + name + "</name>\n" +
						Utils.tab(tab+1) + "<value>" + value + "</value>\n" +
			Utils.tab(tab) + "</stat>\n";
	}
	
	public static Stat fromXML(String xml)
	{
		String statBlock = Utils.returnStringInTag("stat", xml, 0);
		String nameStr = Utils.returnStringInTag("name", statBlock, 0);
		String valueStr = Utils.returnStringInTag("value", statBlock, 0);
		
		int val = -1;
		if (Utils.isInteger(valueStr))
		{
			val = Integer.parseInt(valueStr);
		}
		else
		{
			throw new IllegalArgumentException("Value must be an integer.");
		}
		
		return new Stat(nameStr,val);
	}
}
