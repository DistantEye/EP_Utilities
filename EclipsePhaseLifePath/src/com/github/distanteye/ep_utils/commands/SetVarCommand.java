package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * setVar(<name>,<value>)
 * 
 * @author Vigilant
 *
 */
public class SetVarCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SetVarCommand(String input) {
		super(input);

		if (subparts.length != 3 )
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (!( subparts[1].length() > 0 && subparts[2].length() > 0))
		{
			throw new IllegalArgumentException("Poorly formated effect (params blank) " + input);
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
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		env.getPC().setVar(getStrParam(1),getStrParam(2));
		
		return "";
	}

	public String toString()
	{
		return "Set character variable(" + subparts[1] + ") to value: " + subparts[2];
	}
}
