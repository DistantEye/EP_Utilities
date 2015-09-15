/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.BaseCharacter;

/**
 * Handles access control for a variable in the character store
 * 
 * @author Vigilant
 */
public class CharVarWrapper extends CharAccessWrapper<String> {
	protected String varName;
	
	/**
	 * 
	 * @param aChar Valid BaseCharacter (or subclass)
	 * @param varName String name of the variable in the character
	 */
	public CharVarWrapper(BaseCharacter aChar, String varName)
	{
		this.aChar = aChar;
		this.varName = varName;		
	}
	
	@Override
	public String getValue() {
		return aChar.getVarSF(varName);
	}

	@Override
	public void setValue(String item) {
		aChar.setVar(varName, item);
	}

	@Override
	public boolean isInt() {
		return false;
	}

	public String getVarName() {
		return varName;
	}

	
}
