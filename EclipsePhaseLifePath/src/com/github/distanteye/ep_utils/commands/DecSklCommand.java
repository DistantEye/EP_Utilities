package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * decSkl(<skill>,<value/all>)					(decSkl all will set two variables {lastRemSkl} {lastRemSklVal}, equal to what was removed)
 * decSkl(<skill>,<value/all>,<conditional>)	' the three parameter versions throw an error if the conditional isn't true		
 * 
 * @author Vigilant
 *
 */
public class DecSklCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public DecSklCommand(String input) {
		super(input);

		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
		// resolve conditionals in both cases
		if (subparts.length == 4)
		{	
			this.cond = ConditionalStatement.getConditional(subparts[3],this);
			params.put(3, cond);
		}
		
		// does the Skill exist?
		if (Skill.isSkill(subparts[1]))
		{
			throw new IllegalArgumentException("Poorly formated effect, skill does not exist : " + subparts[1]);	
		}
		
		
		params.put(1, subparts[1]);
		
		// branch behavior based on whether is a number or an "all"
		if ( subparts[2].equalsIgnoreCase("all") ) 
		{
			params.put(2, subparts[2]);
		}
		else
		{
			if (! Utils.isInteger(subparts[2]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			else
			{
				params.put(2, Integer.parseInt(subparts[2]));
			}
		}

		
	}
	
	public String toString()
	{
		String amount = subparts[2];
		if (Utils.isInteger(amount))
		{
			amount = "" + Integer.parseInt(amount)*-1; // we reverse this because of way text is stated
		}
		
		String result =  "Remove " + amount + " from Skill " + subparts[1] + " on character";
		
		if (cond != null)
		{
			result += "if true (" + cond.toString() + ")";
		}
		
		return result;
	}

}
