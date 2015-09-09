package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * setSkl(<skill>,<value>,<conditional>)		( the three parameter versions throw an error if the conditional isn't true )
 * 
 * @author Vigilant
 *
 */
public class SetSklCommand extends Command {

	/**
	* Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public SetSklCommand(String input) {
		super(input);

		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect, wrong parameter length" + origString);
		}
		else if (Skill.isSkill(subparts[1]) || DataProc.containsUncertainty(subparts[1]))
		{
			if (! Utils.isInteger(subparts[2]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			
			params.put(0, subparts[0]);
			params.put(1, subparts[1]);
			params.put(2, Integer.parseInt(subparts[2]));
			
			if (subparts.length == 4)
			{	
				params.put(3, ConditionalStatement.getConditional(subparts[3]));
			}
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect : skill does not exist " + origString);
		}
	}
	
	public String toString()
	{
		String result = "Set skl " + subparts[2] + " to " + subparts[1];
		
		if (subparts.length == 4)
		{
			result += ", Conditional must be true: " + subparts[3];
		}
		
		return result;
	}

}
