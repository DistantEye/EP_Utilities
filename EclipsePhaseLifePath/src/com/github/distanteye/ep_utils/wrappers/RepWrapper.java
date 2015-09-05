/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.containers.Rep;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * @author Vigilant
 *
 */
public class RepWrapper extends AccessWrapper<String> {
	EpCharacter aChar;
	String repName;
	
	/**
	 * 
	 * @param aChar Valid BaseCharacter (or subclass)
	 * @param repName Name of rep to link to. Must be a valid rep type
	 */
	public RepWrapper(EpCharacter aChar, String repName)
	{		
		this.aChar = aChar;
		this.repName = repName;
		
		if (!Rep.exists(repName))
		{
			throw new IllegalArgumentException(repName + " is not a valid type of Rep!");
		}
	}

	@Override
	public String getValue() {
		return ""+aChar.reps().get(repName).getValue();
	}

	@Override
	public void setValue(String item) {
		if (Utils.isInteger(item))
		{
			throw new IllegalArgumentException("StatWrapper.setValue needs an integer value!");
		}
		
		int val = Integer.parseInt(item);
		aChar.reps().get(repName).setValue(val);
	}

	@Override
	public boolean isInt() {
		return true;
	}
}
