package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.Table;
import com.github.distanteye.ep_utils.core.TableRow;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * runTable(<tableName>,<number>)
 * runTable(<tableName>,<number>,<wildCardReplace>) (similar to rollTable Except you specify what the number is)
 * 
 * @author Vigilant
 *
 */
public class RunTableCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public RunTableCommand(String input) {
		super(input);

		if (subparts.length != 3 && subparts.length != 4)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
			if (! DataProc.dataObjExists(subparts[1]))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
			}
			
			if (! DataProc.getDataObj(subparts[1]).getType().equals("table"))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a table");
			}
			
			Table temp = (Table)DataProc.getDataObj(subparts[1]);

			params.put(1, temp);

			
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
				params.put(3, subparts[3]);
			}		
				
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		

		Table temp = (Table)params.get(1);
		int val = getIntParam(2);
		
		TableRow tempRow = null;
		
		if (params.size() == 3)
		{
			tempRow = temp.findMatch(val, getStrParam(2));
		}
		else
		{
			tempRow = temp.findMatch(val);
		}
		env.getUI().statusUpdate(tempRow.getDescription());
		this.setExtraContext(tempRow.getDescription());
		return tempRow.getEffects();		
	}
	
	public String toString()
	{	
		String addendum = "";
		
		if (subparts.length == 4)
		{
			addendum = " (with wildcard replace(" + params.get(3) + "))"; 
		}
		
		return "Run result(" + params.get(2) + ")" + addendum + " from :\n" + params.get(1).toString();
		
		
	}

}
