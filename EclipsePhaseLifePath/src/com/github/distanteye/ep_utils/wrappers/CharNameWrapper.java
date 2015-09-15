/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.BaseCharacter;

/**
 * Handles access control for getting/setting the Character's name
 * @author Vigilant
 *
 */
public class CharNameWrapper extends CharAccessWrapper<String> {
	
	/**
	 * 
	 * @param aChar Valid BaseCharacter (or subclass)
	 */
	public CharNameWrapper(BaseCharacter aChar)
	{		
		this.aChar = aChar;
	}

	@Override
	public String getValue() {
		return aChar.getName();
	}

	@Override
	public void setValue(String item) {
		aChar.setName(item);
	}

	@Override
	public boolean isInt() {
		return false;
	}
	
}
