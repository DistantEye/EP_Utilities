/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.containers.SkilledCharacter;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Handles access control for getting/setting one of the Character's skills
 * @author Vigilant
 *
 */
public class SkillWrapper extends AccessWrapper<String> {
	SkilledCharacter aChar;
	String skillName;
	
	/**
	 * 
	 * @param aChar Valid SkilledCharacter (or subclass)
	 * @param skillName name of a valid Skill aChar has, will throw error if no skill matches this name
	 */
	public SkillWrapper(SkilledCharacter aChar, String skillName)
	{		
		this.aChar = aChar;
		this.skillName = skillName;
		
		if (!aChar.hasSkill(skillName))
		{
			throw new IllegalArgumentException("Skill(" + skillName + ") does not exist in Character!");
		}
	}

	@Override
	public String getValue() {
		Skill temp = aChar.getSkill(skillName);
		return ""+aChar.getFinalSklVal(temp);
	}

	@Override
	public void setValue(String item) {
		if (Utils.isInteger(item))
		{
			throw new IllegalArgumentException("StatWrapper.setValue needs an integer value!");
		}
		
		int val = Integer.parseInt(item);
		
		aChar.getSkill(skillName).setValue(val);
	}

	@Override
	public boolean isInt() {
		return false;
	}
}
