/**
 * 
 */
package com.github.distanteye.ep_utils.containers;

import java.util.HashMap;

/**
 * Extension of AspectHashMap to better handle Stats
 * Overloads toString with a form that takes in a bonuses HashMap to print modified Stat values
 * 
 * No intention of implementing serialization for this class
 * @author Vigilant
 */
@SuppressWarnings("serial")
public class StatHashMap extends AspectHashMap<Stat> {

	
	public StatHashMap(String seperator, boolean includeKey) {
		super(seperator, includeKey);
	}
	
	/**
	 * Returns a single String equal to a join of the collection. Exact results of the join
	 * depend on the separator and includeKey value set for this class, as well as the toString implementation
	 * of the underlying object
	 * @param HaashMap of bonus values to consider (will add to Stat values as appropriate)
	 * @return String a single String representation of the collection as per the above
	 */
	public String toString(HashMap<String,Integer> bonuses)
	{
		if (size() == 0)
		{
			return "";
		}
		
		boolean first = true;
		String result = "";
		
		for (String key : getOrder())
		{
			int bonus = 0;
			if (bonuses.containsKey("bonus"+key))
			{
				bonus = bonuses.get("bonus"+key);
			}
			
			String itemStr = key + " " + (get(key).getValue()+bonus);
			
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

}
