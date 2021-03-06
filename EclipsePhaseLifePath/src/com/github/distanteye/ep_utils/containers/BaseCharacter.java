package com.github.distanteye.ep_utils.containers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Most basic character representation that has simply a name, age, and a generalized variable store
 * 
 * @author Vigilant
 */
public class BaseCharacter {
	
	private HashMap<String,String> otherVars;
	private String name;

	/**
	 * @param name Character name
	 */
	public BaseCharacter(String name) 
	{
		this.name = name;
		otherVars = new HashMap<String,String>();
		otherVars.put("_age", ""+(-1));
	}
	
	public String toString()
	{
		String result = this.name + "(" + this.getAge() + ")";
		
		return result;
	}
	
	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public int getAge() 
	{
		return this.getVarInt("_age");
	}

	public void setAge(int age) 
	{
		otherVars.put("_age", ""+age);
	}
	
	/**
	 * Most generalized variable store. Sets a key,value pair for storage by the character
	 * @param name Name of the variable
	 * @param val Value of the variable
	 */
	public void setVar(String name, String val)
	{		
		this.otherVars.put(name, val);
		
		// messing with bonus variables triggers calc
		if (name.startsWith("bonus"))
		{
			this.calc();
		}
	}
	
	/**
	 * Overload for incVar that takes an integer second parameter (for convenience)
	 * @param name name of variable
	 * @param val Integer value
	 */
	public void incVar(String name, int val)
	{
		this.incVar(name, String.valueOf(val));
	}
	
	/**
	 * Most generalized variable store. Increments key,value pair for storage by the character
	 * will throw error if the variable passed or the value passed is not a number.
	 * Will create the variable if it doesn't exist, set to 0 (before val is added)
	 * 
	 * 
	 * @param name Name of the variable (must be numeric holding variable)
	 * @param val Value of the variable (must be numeric value in string)
	 */
	public void incVar(String name, String val)
	{
		if (!this.hasVar(name))
		{
			this.setVar(name, String.valueOf(0));
		}
		
		String var = this.getVar(name);
		
		if (!Utils.isInteger(var))
		{
			throw new IllegalArgumentException("incVar(" + name + "," + val +"): variable value " + var + " is not a number!");
		}
		if (!Utils.isInteger(val))
		{
			throw new IllegalArgumentException("incVar(" + name + "," + val +"): " + val + " is not a number!");
		}
		
		// otherwise proceed with increment
		int newVal = Integer.parseInt(var) + Integer.parseInt(val);
		this.setVar(name, String.valueOf(newVal));
	}
	
	/**
	 * Skeleton method for telling the character to recalculate derived values
	 * Changing any variable that starts with "bonus" will trigger this
	 */
	public void calc()
	{
		
	}
	
	/**
	 * Retrieves a variable from the general store. Does not throw error if it doesn't exist, returns 0 instead
	 * Will still throw error if the variable exists but is not a number
	 * @param name Name of variable to search for
	 * @return The matching value for name, or 0 if it doesn't exist. 
	 */
	public int getVarInt(String name)
	{
		if (this.hasVar(name))
		{
			if (Utils.isInteger(this.getVar(name)))
			{
				return Integer.parseInt(this.otherVars.get(name));
			}
			else
			{
				throw new IllegalArgumentException(name + " is a non-integer variable and can't be returned by getVarInt");
			}
		}
		else
		{
			return 0;
		}
	}	
	
	/**
	 * Retrieves a variable from the general store. 
	 * This version returns "" instead of throwing an error if no such variable exists
	 * @param name Name of variable to search for
	 * @return The matching value for name, or "" if none exists
	 */
	public String getVarSF(String name)
	{
		if (hasVar(name))
		{
			return getVar(name);
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Retrieves a variable from the general store
	 * @param name Name of variable to search for
	 * @return The matching value for name
	 * @throws IllegalArgumentException if no such variable exists
	 */
	public String getVar(String name)
	{
		if (this.hasVar(name))
		{
			return this.otherVars.get(name);
		}
		else
		{
			throw new IllegalArgumentException("getVar(" + name + "): No such variable exists in character: " + this.getName());
		}
	}
	
	/**
	 * Returns whether a variable exists in the store (or from the character special store)
	 * 
	 * Will return false if the variable exists but has no value (length 0 string)
	 * @param name Name/Key to search for
	 * @return True if exists with a non "" value, false otherwise
	 */
	public boolean hasVar(String name)
	{
		return this.otherVars.containsKey(name) && this.otherVars.get(name).length() != 0;
	}
	
	/**
	 * Removes a variable from the store and returns it, if it exists
	 * @param name Name/Key to remove
	 * @return The variable removed
	 * @throws IllegalArgumentException if nothing with that key exists
	 */
	public String removeVar(String name)
	{
		if (this.otherVars.containsKey(name))
		{
			String result = this.otherVars.remove(name);
			
			// messing with bonus variables triggers calc
			if (name.startsWith("bonus"))
			{
				this.calc();
			}
			
			return result;
		}
		else
		{
			throw new IllegalArgumentException("getVar(" + name + "): No such variable exists in character: " + this.getName());
		}
	}
	
	/**
	 * Converts the chracter's data to XML, valid for storing/saving
	 * @return Returns a String containing an XML representation of all the character's data
	 */
	public String getXML()
	{
		String result = "<Character>\n";
		
		result += this.getInnerXML();
		
		result += "</Character>";
		
		return result;
	}
	
	/**
	 * Collects the character's data into a set of XML tags, not enclosed in a greater tag,
	 * this less subclasses redefine saving while still being able to draw off the superclass
	 * @return Returns a String containing the character's vital data in a list of XML tags
	 */
	protected String getInnerXML()
	{
		StringWriter result = new StringWriter();
		Element root = new Element("Character");
		Document doc = new Document(root);
		
		doc.getRootElement().addContent(new Element("charName").setText( this.name ));
		
		Element elemOtherVars = new Element("otherVars");
		
		for (String key : otherVars.keySet())
		{
			elemOtherVars.addContent(new Element(key).setText(  otherVars.get(key)  ));
		}
		
		
		doc.getRootElement().addContent(elemOtherVars);
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setEncoding("UTF-8").setOmitEncoding(false));
		
		
		try {
			xmlOut.outputElementContent(root, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}
	
	/**
	 * Discards the character's current data and replaces it with the data encoded into the xml string passed
	 * @param xml a validly formatted XML string as returned by getXML()
	 */
	public void loadXML(String xml)
	{
		this.setToDefaults();
		
		Document document = Utils.getXMLDoc(xml);
		
		Element root = document.getRootElement();
		
		Utils.verifyTag(root, "Character");
		Utils.verifyChildren(root, new String[]{"charName","otherVars"});
		
		this.name = root.getChildText("charName");
		Element elemOtherVars = root.getChild("otherVars");
		
		for (Element e : elemOtherVars.getChildren())
		{
			otherVars.put(e.getName(), e.getText());
		}
		
	}
	
	/**
	 * Discards character's current data and sets everything to default values
	 */
	public void setToDefaults()
	{
		this.name = "";
		otherVars = new HashMap<String,String>();
		setAge(-1); // placeholder
	}
	
}