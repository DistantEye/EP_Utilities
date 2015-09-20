package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command of following syntax types:
 * SklSpec(<skill>,<specializationName>)
 * 
 * @author Vigilant
 *
 */
public class SklSpecCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SklSpecCommand(String input) {
		super(input);

		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if ((Skill.isSkill(subparts[1]) || isUncertain(subparts[1])) && subparts[2].length() > 0)
		{
			this.subpartsToParams();
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect (Skill does not exist or specialization blank) " + input);
		}
	}
	
	public String run(CharacterEnvironment env)
	{	
		super.run(env);
		
		String name = getStrParam(1);
		String val = getStrParam(2);
		
		// executes the add, throwing error if the skill didn't exist
		if (! env.getPC().addSkillSpec(name, val) )
		{
			throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + name);
		}
		
		return "";
	}
	
	public String toString()
	{
		return "Add specialization(" + subparts[2] + ") to skill " + subparts[1];
	}

}
