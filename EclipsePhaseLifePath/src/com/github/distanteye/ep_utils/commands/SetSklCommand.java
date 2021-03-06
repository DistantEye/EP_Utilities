package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalBuilder;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
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
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SetSklCommand(String input) {
		super(input);

		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
		// does the Skill exist? or is at least a wildcard value?
		if (!Skill.isSkill(subparts[1]) && !isUncertain(subparts[1]))
		{
			throw new IllegalArgumentException("Poorly formated effect, skill does not exist : " + subparts[1]);	
		}


		params.put(1, subparts[1]);

		// check for integer or wildcard value
		if ( Utils.isInteger(subparts[2]) )
		{
			params.put(2, Integer.parseInt(subparts[2]));
		}
		else if (isUncertain(subparts[2]))
		{
			params.put(2, subparts[2]);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
		}
		
		if (subparts.length == 4)
		{	
			this.cond = ConditionalBuilder.getConditional(subparts[3], this);
			params.put(3,cond);
		}
	}
	
	public String run(CharacterEnvironment env)
	{	
		super.run(env);
		
		String name = getStrParam(1);
		int val = getIntParam(2);
		
		// executes the add, throwing error if the skill didn't exist
		if (! env.getPC().setSkill(name, val) )
		{
			throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + subparts[1]);
		}
		
		return "";
	}
	
	public String toString()
	{
		String result = "Set skl " + subparts[2] + " to " + subparts[1];
		
		if (subparts.length == 4)
		{
			result += ", Conditional must be true: " + cond.toString();
		}
		
		return result;
	}

}
