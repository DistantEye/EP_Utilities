/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Conditional with syntax :
 * ?skillIsType(skill,type)  (skill is name of skill, type is a type you want it to be, like Technical
 * 
 * $0,$1,$2,$3, etc when inside conditionals references the subparams of the effect containing the conditional, so 
 * incSkl(<skill>,<number>,<conditional>) leads to $0 accessing incSkl, $1 accessing <skill> and so on
 * 
 * || and && are partially supported
 * 
 * replacing ? with ! is for boolean not. so !hasTrait;trait => not having that trait
 * 
 * @author Vigilant
 *
 */
public class SkillIsTypeConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public SkillIsTypeConditional(String input, Command parent) {
		super(input, parent);

		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Invalidly formatted condition " + input + ")");
		}
			
		if (!Skill.isSkill(subparts[1]))
		{
			throw new IllegalArgumentException("Skill : " + subparts[1] + " does not exist!");
		}
		
		
		params.set(1, subparts[1]);
		params.set(2, subparts[2]);
	}

	@Override
	public boolean resolve(CharacterEnvironment env) {
		return Skill.hasCategory(subparts[1], subparts[2]);
	}

	public String toString()
	{
		return "Skill : " + subparts[1] + " has category : " + subparts[2]; 
	}

}
