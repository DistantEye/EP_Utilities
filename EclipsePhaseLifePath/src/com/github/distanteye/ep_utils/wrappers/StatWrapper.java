/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.SkilledCharacter;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Handles access control for a Stat in a SkilledCharacter
 * Values passed to this class must be integer parse-able
 * @author Vigilant
 *
 */
public class StatWrapper extends AccessWrapper<String> {

	SkilledCharacter aChar;
	String statName;
	
	/**
	 * 
	 * @param aChar
	 * @param statName
	 */
	public StatWrapper(SkilledCharacter aChar, String statName)
	{
		this.aChar = aChar;
		this.statName = statName;		
	}
	
	@Override
	public String getValue() {
		return ""+aChar.stats().get(statName).getValue();
	}

	@Override
	public void setValue(String item) {
		if (Utils.isInteger(statName))
		{
			throw new IllegalArgumentException("StatWrapper.setValue needs an integer value!");
		}
		
		int val = Integer.parseInt(item);
		aChar.stats().get(statName).setValue(val);
	}

	@Override
	public boolean isInt() {
		return true;
	}

}
