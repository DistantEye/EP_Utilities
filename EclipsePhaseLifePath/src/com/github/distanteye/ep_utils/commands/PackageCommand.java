package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.Package;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * package(<name>)				(add package -- assume 1 PP if it needs a value)
 * package(<name>,<value>)		(add package of a certain PP value)
 * 
 * @author Vigilant
 *
 */
public class PackageCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public PackageCommand(String input) {
		super(input);

		// checks for package being valid
		if (subparts.length == 2 || subparts.length == 3)
		{
			if ( subparts[1].length() == 0 )
			{
				throw new IllegalArgumentException("Poorly formatted effect (" +  input + "): no package name found");
			}
			
			if (! DataProc.dataObjExists(subparts[1]))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
			}
			
			if (! DataProc.getDataObj(subparts[1]).getType().equals("package"))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a package");
			}
			
			int pp = 1;
			
			Package temp = (Package)DataProc.getDataObj(subparts[1]); 
			
			if (subparts.length == 3)
			{
				if ( isUncertain(subparts[2]))
				{
					params.set(2, subparts[2]);
				}				
				else if (! Utils.isInteger(subparts[2]) )
				{
					throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
				}
				else if (!temp.getAllEffects().containsKey(Integer.parseInt(subparts[2])))
				{
					throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is a listed PP for package " + subparts[1]);
				}
				else
				{
					params.set(2, Integer.parseInt(subparts[2]));
				}
				
			}
			
			params.set(1, temp);
			
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
	}

	public String toString()
	{
		Package temp = (Package)params.get(1);
		
		return "Execute package("+ temp.getName() + ") with PP:" + params.get(2);
	}
	
}
