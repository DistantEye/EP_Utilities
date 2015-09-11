package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;
import com.github.distanteye.ep_utils.containers.Rep;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * rep(<type>,<value>)
 * rep(<type>,<value>,<conditional>)	(as with others, conditional must be true for the command to work)
 *
 * @author Vigilant
 *
 */
public class RepCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public RepCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
		
		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() > 0 )
		{
			
			if ( Rep.exists(subparts[1]) || isUncertain(subparts[1]))
			{
				params.put(1, subparts[1]);
			}
			else
			{
				throw new IllegalArgumentException("Poorly formatted effect Rep (" + subparts[1] + ") does not exist");
			}
			
			
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
				this.cond = ConditionalStatement.getConditional(subparts[3], this);
				params.put(3, cond);
			}
			
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}
	
	public String toString()
	{
		String result = "Add " + subparts[2] + " to Rep(" + subparts[1] +  ") for character";
		
		if (subparts.length == 4)
		{
			result += ", Conditional must be true: " + cond.toString();
		}
		
		return result;
	}

}
