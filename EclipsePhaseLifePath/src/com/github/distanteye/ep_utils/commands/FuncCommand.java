package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.Function;

/**
 * Command of following syntax types:
 * func(<name>)
 * func(<name>,<param1>,<param2>,<...etc>)  (any params passed after name will substitute in for <1>,<2>, etc, in the function 
 * 
 * @author Vigilant
 *
 */
public class FuncCommand extends Command {	
	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public FuncCommand(String input) {
		super(input);
		
		if (subparts.length < 2 )
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number of params)" + input);
		}
		else
		{
			if (DataProc.dataObjExists(subparts[1]) && DataProc.getDataObj(subparts[1]).getType().equals("function"))
			{
				Function temp = (Function)DataProc.getDataObj(subparts[1]);
				
				
				params.set(1, temp);
				
				for (int i = 2; i < subparts.length; i++)
				{
					params.set(i, subparts[i]);
				}
			}
			else
			{
				throw new IllegalArgumentException("Poorly formated effect, " + subparts[1] + " does not exist or is not a function" + input);
			}
		}
	}
	
	public String toString()
	{
		String result =  "Execute function("+ subparts[1] + ")";
		if (subparts.length > 2)
		{
			result += " with params(" + subparts[3];
			
			for (int i = 4; i < subparts.length; i++)
			{
				result += "," + subparts[i];
			}
			
			result += ")";
		}
		
		result += " function effects: " + params.get(1).toString();
		
		return result;
	}
		
}
