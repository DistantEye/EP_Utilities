/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.BaseCharacter;

/**
 * Handles access control for a variable in the character store
 * Note : It has to reference a variable that already exists, or it will throw an error
 * 
 * @author Vigilant
 */
public class CharVarWrapper extends AccessWrapper<String> {
	BaseCharacter aChar;
	String varName;
	
	/**
	 * 
	 * @param aChar
	 * @param varName
	 */
	public CharVarWrapper(BaseCharacter aChar, String varName)
	{
		if (!aChar.hasVar(varName))
		{
			throw new IllegalArgumentException("Invalid varName(" + varName +"), variable does not exist in character");
		}
		
		this.aChar = aChar;
		this.varName = varName;		
	}
	
	@Override
	public String getValue() {
		return aChar.getVar(varName);
	}

	@Override
	public void setValue(String item) {
		aChar.setVar(varName, item);
	}

	@Override
	public boolean isInt() {
		return false;
	}

}
