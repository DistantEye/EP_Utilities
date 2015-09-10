package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.Table;

/**
 * Command of following syntax types:
 * rollTable(<tableName>)						(replace semicolon, spaces and periods in table name with underscore, e.g. Table_6_5)
 * 										forceRoll and forceRollTable can be used to make sure a user in interactive mode still rolls these 
 * rollTable(<tableName>,<replaceValue>) 	(as before, but <replaceValue will sub in for any wildcards in the table) (wildcard is !!X!!)
 * 
 * @author Vigilant
 *
 */
public class RollTableCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public RollTableCommand(String input) {
		super(input);

		if (subparts.length != 2 && subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() > 0 )
		{
			if (! DataProc.dataObjExists(subparts[1]))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
			}
			
			if (! DataProc.getDataObj(subparts[1]).getType().equals("table"))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a table");
			}

			
			Table temp = (Table)DataProc.getDataObj(subparts[1]);
			
			if (temp.containsWildCards() && (subparts.length != 3 || subparts[2].length() == 0) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, Table " + subparts[1] + 
													" has wildcards but no wildcard value was specified for this call");
			}
			
			params.put(1, temp);
			
			if (subparts.length == 3)
			{
				params.put(2, subparts[2]);
			}
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}

	public String toString()
	{
		String start = "R";
		
		if (this.isForceRoll())
		{
			start = "Force r";
		}
		
		String addendum = "";
		
		if (subparts.length == 3)
		{
			addendum = " (with wildcard replace(" + params.get(2) + "))"; 
		}
		
		return start + "oll result" + addendum + " from :\n" + params.get(2).toString();
		
		
	}
}
