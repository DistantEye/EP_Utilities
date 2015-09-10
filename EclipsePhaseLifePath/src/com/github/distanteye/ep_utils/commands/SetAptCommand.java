package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Aptitude;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * setApt(<aptitudeName>,<value>)
 * 
 * @author Vigilant
 *
 */
public class SetAptCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SetAptCommand(String input) {
		super(input);
		
		
		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() > 0 )
		{
			if (! Utils.isInteger(subparts[2]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			
			if (! Aptitude.exists(subparts[1]))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a valid aptitude");
			}
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}

		params.put(1, subparts[1]);
		params.put(2, Integer.parseInt(subparts[2]));
	}
	
	public String toString()
	{
		String result = "Set APT: " + subparts[1] + " to " + subparts[2];
		
		
		return result;
	}

}
