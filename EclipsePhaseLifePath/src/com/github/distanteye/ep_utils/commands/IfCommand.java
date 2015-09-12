package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;

/**
 * Command of following syntax types:
 * if(<condition>,<effectWhenTrue>,<effectWhenFalse>)		(The latter can be blank)
 * 
 * @author Vigilant
 *
 */
public class IfCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public IfCommand(String input) {
		super(input);

		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts.length == 3 && !(subparts[1].length() > 0 && subparts[2].length() > 0))
		{
			throw new IllegalArgumentException("Poorly formated effect (effects empty) " + input);
		}
		else if (subparts.length == 4 && !(subparts[1].length() > 0 && subparts[2].length() > 0 && subparts[3].length() > 0))
		{
			throw new IllegalArgumentException("Poorly formated effect (effects empty) " + input);
		}
		
		this.cond = ConditionalStatement.getConditional(subparts[1], this);
		params.set(1, cond);
		
		for (int i = 2; i < subparts.length; i++)
		{
			params.set(i, subparts[i]);
		}
		
	}
	
	public String toString()
	{
		String result =  "if (" + params.get(1).toString() + ") then (" + params.get(2) + ")";
		
		if (params.size() == 4)
		{
			result += " else(" + params.get(3) + ")";
		}
		
		return result;
	}

}
