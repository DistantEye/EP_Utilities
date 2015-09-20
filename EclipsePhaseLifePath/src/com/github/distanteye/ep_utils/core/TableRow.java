package com.github.distanteye.ep_utils.core;

import com.github.distanteye.ep_utils.commands.Command;

/**
 * TableRow encapsulates a single row of many in a particular Table object.
 * It sets it's own hashCode and as well as getCopy and other support methods.
 * 
 * This class is returned as a return value from Table, so needs visibility outside of it
 * 
 * @author Vigilant
 *
 */
public class TableRow {
	private int lowRange;
	private int highRange;
	private String description;
	private String effects;
	private int hash = -1;
	private String parentName; // needed to avoid cyclical printing
	
	
	
	/**
	 * @param lowRange
	 * @param highRange
	 * @param description
	 * @param effects
	 */
	public TableRow(int lowRange, int highRange, String description,
			String effects, String parentName) {
		super();
		this.lowRange = lowRange;
		this.highRange = highRange;
		this.description = description;
		this.effects = effects;
		this.setHash();
	}
	
	/**
	 * Returns clone of this object with no modifications
	 * @return Clone of this object
	 */
	public TableRow getCopy()
	{
		TableRow temp = new TableRow(this.lowRange,this.highRange,this.description, this.effects, this.parentName);
		return temp;
	}
	
	/**
	 * Returns clone of this object, replacing the effects string according to the parameters passed
	 * @param search String to search for
	 * @param replace String to replace with
	 * @return
	 */
	public TableRow getCopy(String search, String replace)
	{
		TableRow temp = new TableRow(this.lowRange,this.highRange,this.description, this.effects.replace(search, replace),this.parentName);
		return temp;
	}
	
	private void setHash()
	{
		String temp = String.valueOf(lowRange) + "" + String.valueOf(highRange);
		this.hash = temp.hashCode();
	}
	
	public int hashCode()
	{
		if (this.hash == -1)
		{
			this.setHash();
		}
		
		return this.hash;
	}
	
	/**
	 * Returns a String containing the lowRange, highRange, and Row effects converted to String
	 */
	public String toString()
	{
		String lowRange = String.valueOf(this.lowRange);
		String highRange = String.valueOf(this.highRange);
		
		// we have to do a bit more special printing to avoid Cyclical toString with a TableRow rolling it's parent
		return "" + padLeft(lowRange,2) + "-" + padLeft(highRange,2) + " " + effectsToString();
	}
	
	public String effectsToString()
	{
		// we have to do a bit more special printing to avoid Cyclical toString with a TableRow rolling it's parent
		String[] effects = Utils.splitCommands(this.effects, ";");
		String effectsString = "";
		boolean first = true;
		
		for (int i = 0; i < effects.length; i++)
		{
			String part = "";
			if (effects[i].startsWith("rollTable") && Utils.returnStringInParen(effects[i]).equalsIgnoreCase(parentName))
			{
				// avoids printing any nesting of rollTable/runTable
				part = "Reroll table";
			}
			else
			{
				part = Command.effectsToString(effects[i]);
			}
			
			if (first)
			{
				effectsString = part;
			}
			else
			{
				effectsString += "; " + part;
			}
		}
		
		return effectsString;
	}
	
	/**
	 * Returns a String containing the lowRange, highRange, and Row description
	 */
	public String toStringDescription()
	{
		String lowRange = String.valueOf(this.lowRange);
		String highRange = String.valueOf(this.highRange);
		
		return "" + padLeft(lowRange,2) + "-" + padLeft(highRange,2) + " " + this.description;
	}
	
	public boolean isMatch(int roll)
	{
		return roll >= this.lowRange && roll <= this.highRange;
	}
	
	public int getLowRange() {
		return lowRange;
	}

	public void setLowRange(int lowRange) {
		this.lowRange = lowRange;
		this.setHash();
	}

	public int getHighRange() {
		return highRange;
	}

	public void setHighRange(int highRange) {
		this.highRange = highRange;
		this.setHash();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEffects() {
		return effects;
	}

	public void setEffects(String effects) {
		this.effects = effects;
	}
	
	private static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);  
	}
	
}
