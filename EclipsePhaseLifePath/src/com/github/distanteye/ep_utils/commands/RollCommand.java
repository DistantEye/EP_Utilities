package com.github.distanteye.ep_utils.commands;

import java.util.ArrayList;

import com.github.distanteye.ep_utils.core.Table;
import com.github.distanteye.ep_utils.core.TableRow;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * roll(<dieNumber>,#-#=effect/#-#=effect)  (list can be as long as needed)		(ex, roll(1-6=morph,splicer/7-10=morph(bouncer)  ) 
 * forceRoll can be used to make sure a user in interactive mode still rolls these 
 * 
 * @author Vigilant
 *
 */
public class RollCommand extends Command {
	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public RollCommand(String input) {
		super(input);

		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() > 0  && subparts[2].length() > 0)
		{
			
			if (! Utils.isInteger(subparts[1]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");
			}
			
			int numDice = Integer.parseInt(subparts[1]);
			
			if (numDice <= 0)
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a positive number");
			}
			
			if (! subparts[2].substring(0,subparts[2].indexOf('=')).matches("[0-9]+-[0-9]+") )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " does not start with a proper roll range");
			}
			
			if (! (subparts[2].split("/").length > 1) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " does not have more than one effect");
			}
			
		}	
		
		int numDie = Integer.parseInt(subparts[1]);
		String[] effectList = subparts[2].split("/");
		
		// we make a fake table to hold the results since this process functions like an inline table
		ArrayList<TableRow> rows = new ArrayList<TableRow>();
		
		for (int x = 0; x < effectList.length; x++)
		{
			String currEffect = effectList[x];
			String range = currEffect.substring(0, currEffect.indexOf('='));
			String[] bounds = range.split("-");
			
			int low = 0;
			int high = 0;
			
			if (bounds.length == 1)
			{
				low = Integer.parseInt(bounds[0]);
				high = Integer.parseInt(bounds[0]);
			}
			else if (bounds.length == 2)
			{
				low = Integer.parseInt(bounds[0]);
				high = Integer.parseInt(bounds[1]);
			}
			else
			{
				throw new IllegalArgumentException("Poorly formated effect " + input);
			}
			
			// this gets everything after the =
			rows.add(new TableRow(low,high,"",currEffect.substring(currEffect.indexOf('=')+1))); 							
		}
		
		params.put(1, Integer.parseInt(subparts[1]));
		params.put(2, new Table("temp",numDie,rows, false));
	}
	
	public String toString()
	{
		String start = "R";
		
		if (this.isForceRoll())
		{
			start = "Force r";
		}
		
		return start + "oll result from :\n" + params.get(2).toString();
	}

}
