package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Sleight;

/**
 * Command of following syntax types:
 * psigamma(<name>)				(can use ?1?,?2?, etc)
 * 
 * @author Vigilant
 *
 */
public class PsiGammaCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public PsiGammaCommand(String input) {
		super(input);
		
		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() > 0 )
		{
			if ( isUncertain(subparts[1]) )
			{
				params.set(1, subparts[1]);
			}
			else if (! Sleight.exists(subparts[1] ) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
			}
			else if ( Sleight.sleightList.get(subparts[1]).getSleightType()!=Sleight.SleightType.GAMMA )
			{
				throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " is not a Psi Gamma sleight");
			}
			else
			{
				Sleight s = Sleight.sleightList.get(subparts[1]);
				
				params.set(1, s);
			}
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}
	
	public String toString()
	{
		return "Add Psi-Gamma Sleight: " + subparts[1];
	}
}
