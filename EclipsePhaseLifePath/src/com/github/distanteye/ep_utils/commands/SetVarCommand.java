package com.github.distanteye.ep_utils.commands;

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
		else if (!Utils.isInteger(subparts[2]))
		{
			throw new IllegalArgumentException("Poorly formated effect " + input + ", " + subparts[2] + " is not a number");
		}
		else if (!( subparts[1].length() > 0 && subparts[2].length() > 0))
		{
			throw new IllegalArgumentException("Poorly formated effect (params blank) " + input);
		}
		
		
		params.put(1, subparts[1]);
		params.put(2, Integer.parseInt(subparts[2]));
	}

	public String toString()
	{
		return "Set character variable(" + subparts[1] + ") to value: " + subparts[2];
	}
}
