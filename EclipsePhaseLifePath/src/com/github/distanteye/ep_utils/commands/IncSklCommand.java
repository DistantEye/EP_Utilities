package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * incSkl(<skill>,<value>)
 * incSkl(<skill>,<value>,<conditional>)
 * 
 * @author Vigilant
 *
 */
public class IncSklCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public IncSklCommand(String input) {
		super(input);
		
		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
		// resolve conditionals in both cases
		if (subparts.length == 4)
		{	
			this.cond = ConditionalStatement.getConditional(subparts[3],this);
			params.set(3, cond);
		}
		
		// does the Skill exist? or is at least a wildcard value?
		if (!Skill.isSkill(subparts[1]) && !isUncertain(subparts[1]))
		{
			throw new IllegalArgumentException("Poorly formated effect, skill does not exist : " + subparts[1]);	
		}
		
		
		params.set(1, subparts[1]);
		
		// check for integer or wildcard value
		if ( Utils.isInteger(subparts[2]) )
		{
			params.set(2, Integer.parseInt(subparts[2]));
		}
		else if (isUncertain(subparts[2]))
		{
			params.set(2, subparts[2]);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
		}
		
	
	}
	
	public String toString()
	{
		
		String result =  "Add " + subparts[2] + " from Skill " + subparts[1] + " on character";
		
		if (cond != null)
		{
			result += "if true (" + cond.toString() + ")";
		}
		
		return result;
	}

}
