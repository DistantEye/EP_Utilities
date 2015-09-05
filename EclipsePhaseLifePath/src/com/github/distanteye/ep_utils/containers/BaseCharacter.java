package com.github.distanteye.ep_utils.containers;

import java.util.HashMap;

import com.github.distanteye.ep_utils.core.Utils;
import com.github.distanteye.ep_utils.wrappers.AccessWrapper;
import com.github.distanteye.ep_utils.wrappers.IntWrapper;

/**
 * Most basic character representation that has simply a name, age, and a generalized variable store
 * 
 * @author Vigilant
 */
public class BaseCharacter {
	
	private HashMap<String,AccessWrapper<String>> otherVars;
	private String name;
	private int age;

	/**
	 * @param name Character name
	 */
	public BaseCharacter(String name) 
	{
		this.name = name;
		otherVars = new HashMap<String,AccessWrapper<String>>();
		age = -1; // placeholder
	}
	
	public String toString()
	{
		String result = this.name + "(" + this.age + ")";
		
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
		return age;
	}

	public void setAge(int age) 
	{
		this.age = age;
	}
	
	/**
	 * Most generalized variable store. Sets a key,value pair for storage by the character
	 * @param name Name of the variable
	 * @param val Value of the variable
	 */
	public void setVar(String name, AccessWrapper<String> val)
	{
		// if no variable at all, just add it in
		if (!hasVar(name))
		{
			this.otherVars.put(name, val);
		}
		// if variable, same type, replace the underlying value
		else if (getVar(name).equals(val))
		{	
			getVar(name).setValue(val.getValue());
		}
		// if variable, different type, remove old, add new
		else
		{
			this.otherVars.remove(name);
			this.otherVars.put(name, val);
		}
	}
	
	/**
	 * Increments key,value pair for variable stored by the character
	 * will throw error if the variable passed or the value passed is not a number.
	 * Will create the variable if it doesn't exist, set to 0 (before val is added)
	 * 
	 * @param name name of variable
	 * @param val Integer value
	 */
	public void incVar(String name, int val)
	{
		if (!this.hasVar(name))
		{
			this.setVar(name, new IntWrapper(val));
		}
		
		AccessWrapper<String> temp = getVar(name);
		
		if (temp.isInt())
		{
			int value = Integer.parseInt(temp.getValue()); // unfortunately because of limitations of the parent class we have to work it like this
			temp.setValue(""+value+val);
		}
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
			if (Utils.isInteger(this.getVarVal(name)))
			{
				return Integer.parseInt(this.getVarVal(name));
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
	public String getVarValSF(String name)
	{
		if (hasVar(name))
		{
			return getVarVal(name);
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
	public AccessWrapper<String> getVar(String name)
	{
		if (this.otherVars.containsKey(name))
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
		return this.otherVars.containsKey(name) && this.getVarVal(name).length() != 0;
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
			AccessWrapper<String> temp = this.otherVars.remove(name);
			
			if (temp==null)
			{
				return null;
			}
			else
			{
				return temp.getValue();
			}
		}
		else
		{
			throw new IllegalArgumentException("getVar(" + name + "): No such variable exists in character: " + this.getName());
		}
	}
	
	/**
	 * Shortcut method for getVar(name).getValue() 
	 * Will return the underlying value of a variable of that name if it exists
	 * @param name String key for the variable
	 * @return String value stored for the variable
	 */
	public String getVarVal(String name)
	{
		return getVar(name).getValue();
	}
}
