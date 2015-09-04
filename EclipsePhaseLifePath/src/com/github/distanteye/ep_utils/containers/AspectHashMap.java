/**
 * 
 */
package com.github.distanteye.ep_utils.containers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * a HashMap of Character Aspects. These can be Sleights, Rep, Skills, Traits, etc
 * While no methods are required to be implemented by T, certain things improve function, like a good toString,
 * which will be used when this class uses toString to print a joined String of the collection
 * 
 * This class bolts on a few common code functions to all Character Aspects, the toString mainly but also 
 * getting a random value from the collection, and other small helper methods.
 * 
 * It is possible to turn objects of this class into a psuedo immutable state with setImmutable(), where new keys can't be added,
 * and existing keys can't be removed.
 * 
 * Have no intentions of using this class with serialization
 * @author Vigilant
 *
 */
@SuppressWarnings("serial")
public class AspectHashMap<T> extends HashMap<String,T> {
	String separator;
	boolean includeKey;
	boolean mutable; // if false, put and remove will fail, except when put modifies an existing key
	
	/**
	 * Creates the class, specifying behaviors for the toString method to use, which does a somewhat customized
	 * joinString of this collection
	 * 
	 * @param seperator If joined to a String, the string that separates the values
	 * @param includeKey Whether this set needs to include the key in String representations, or whether 
	 *                   the Object value from the HashMap is sufficient (ints for instance will need the key)
	 */
	public AspectHashMap(String seperator, boolean includeKey) {
		super();
		this.separator = seperator;		
		this.includeKey = includeKey;
		this.mutable = true; 
	}

	/**
	 * Returns a single String equal to a join of the collection. Exact results of the join
	 * depend on the separator and includeKey value set for this class, as well as the toString implementation
	 * of the underlying object
	 * @return String a single String representation of the collection as per the above
	 */
	public String toString()
	{
		if (size() == 0)
		{
			return "";
		}
		
		boolean first = true;
		String result = "";
		
		for (String key : keySet())
		{
			String itemStr = "";
			
			if (!includeKey)
			{
				itemStr = get(key).toString();
			}
			else
			{
				itemStr = key + " " + get(key).toString();
			}
			
			if (first)
			{
				result += itemStr;
				first = false;
			}
			else
			{
				result += separator + itemStr;
			}
		}
		
		return result;
	}
	
	/**
	 * Sets mutable to false, causing operations that would add or remove keys to fail.
	 * This is not reversible from outside of the class.
	 */
	public void setImmutable()
	{
		this.mutable = false;
	}
	
	/**
	 * Case insensitive version of containsKey
	 * @param key to search for
	 * @return True if key exists, false otherwise
	 */
	public boolean containsKeyIgnoreCase(String name)
	{
		for (String key : keySet())
		{
			if (key.equalsIgnoreCase(name))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns whether HashMap has a key that at least starts with the name provided
	 * @param name Partial name to search for
	 * @return True/False as appropriate
	 */
	public boolean containsKeyPartial(String name)
	{
		for (String key : keySet())
		{
			if (key.startsWith(name))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public T getRand(SecureRandom rng)
	{
		int idx = rng.nextInt(size());
		return (new ArrayList<T>(this.values()).get(idx)); // most Type safe way to do
	}
	
	/**
	 * Functions like superclass put except if mutable is false will not allow new 
	 * keys to be added
	 */
	public T put(String key, T value)
	{
		if (mutable || containsKey(key))
		{
			return super.put(key, value);
		}
		else
		{
			throw new IllegalStateException("AspectHashMap is not currently mutable and cannot accept changes to the number of keys");
		}
	}
	
	/**
	 * Functions like superclass putAll except will only function when mutable is true
	 */
	public void putAll(Map<? extends String,? extends T> m)
	{
		if (mutable)
		{
			super.putAll(m);
		}
		else
		{
			throw new IllegalStateException("AspectHashMap is not currently mutable and cannot accept changes to the number of keys");
		}
	}
	
	/**
	 * Functions like superclass remove except will only function when mutable is true
	 */
	public T remove(String key)
	{
		if (mutable)
		{
			return super.remove(key);
		}
		else
		{
			throw new IllegalStateException("AspectHashMap is not currently mutable and cannot accept changes to the number of keys");
		}
	}
	
}
