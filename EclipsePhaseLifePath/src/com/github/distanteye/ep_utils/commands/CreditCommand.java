package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * credit(<value>)
 * 
 * @author Vigilant
 *
 */
public class CreditCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public CreditCommand(String input) {
		super(input);


		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Invalidly formatted effect " + input + ")");
		}
		
		
		if (Utils.isInteger(subparts[1]))
		{
			params.put(1, Integer.parseInt(subparts[1]));
		}
		else if ( isUncertain(subparts[1]) )
		{
			params.put(1, subparts[1]);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");
		}
		
		
	}
	
	public String toString()
	{
		return "Add credits to character(" + subparts[1] + ")";
	}

}
