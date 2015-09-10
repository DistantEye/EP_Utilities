package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Skill;

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
		else if (Skill.isSkill(subparts[1]) && subparts[2].length() > 0)
		{
			this.subpartsToParams();
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect (Skill does not exist or specialization blank) " + input);
		}
	}

}
