package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * setMox(<value>)
 * 
 * @author Vigilant
 *
 */
public class SetMoxCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SetMoxCommand(String input) {
		super(input);
		
		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}

		// we can't parse if it's an wildcard/choice/etc but we can still store it as "valid"
		if (Utils.isInteger(subparts[1]) )
		{
			params.put(1, Integer.parseInt(subparts[1]));
		}
		else if ( isUncertain(subparts[1]))
		{
			params.put(1, subparts[1]);
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
		pc.setVar("{MOX}",""+getIntParam(1));
		
		return "";
	}
	
	public String toString()
	{
		return "Set character's MOX to " + subparts[1];
	}

}
