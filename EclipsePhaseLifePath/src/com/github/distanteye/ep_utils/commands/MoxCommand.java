package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * mox(<value>)			increments mox by value
 * 
 * @author Vigilant
 *
 */
public class MoxCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public MoxCommand(String input) {
		super(input);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}

		// check for integer or wildcard value
		if ( Utils.isInteger(subparts[1]) )
		{
			params.set(1, Integer.parseInt(subparts[1]));
		}
		else if (isUncertain(subparts[1]))
		{
			params.set(1, subparts[1]);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");
		}
		
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		EpCharacter pc = env.getPC();
		pc.incVar("{MOX}",getIntParam(1));
		
		return "";
	}
	
	public String toString()
	{
		return "Add " + subparts[1] + " to character's MOX";
	}

}
