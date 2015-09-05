/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.containers.Morph;

/**
 * Handles access control for getting/setting the Character's morph
 * @author Vigilant
 *
 */
public class CharMorphWrapper extends AccessWrapper<String> {
	EpCharacter aChar;
	
	/**
	 * 
	 * @param aChar Valid BaseCharacter (or subclass)
	 */
	public CharMorphWrapper(EpCharacter aChar)
	{		
		this.aChar = aChar;
	}

	@Override
	public String getValue() {
		return aChar.getMorphName();
	}

	@Override
	public void setValue(String item) {
		aChar.setCurrentMorph(Morph.createMorph(item));
	}

	@Override
	public boolean isInt() {
		return false;
	}
}
