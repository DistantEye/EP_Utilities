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
			if (Aptitude.exists(subparts[1]) || isUncertain(subparts[1]))
			{
				params.put(1, subparts[1]);
			}
			else
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a valid aptitude");
			}
			
			// we can't parse if it's an wildcard/choice/etc but we can still store it as "valid"
			if (Utils.isInteger(subparts[2]) )
			{
				params.put(2, Integer.parseInt(subparts[2]));
			}
			else if ( isUncertain(subparts[2]))
			{
				params.put(2, subparts[2]);
			}
			else
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}

	}
	
	public String toString()
	{
		String result = "Set APT: " + subparts[1] + " to " + subparts[2];
		
		
		return result;
	}

}
