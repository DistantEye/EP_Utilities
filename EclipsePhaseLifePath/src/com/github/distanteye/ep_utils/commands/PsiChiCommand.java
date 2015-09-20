package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.containers.Sleight;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command of following syntax types:
 * psichi(<name>)				(can use ?1?,?2?, etc)
 * 
 * @author Vigilant
 *
 */
public class PsiChiCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public PsiChiCommand(String input) {
		super(input);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() > 0 )
		{
			if ( isUncertain(subparts[1]) )
			{
				params.put(1, subparts[1]);
			}
			else if (! Sleight.exists(subparts[1] ) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
			}
			else if ( Sleight.sleightList.get(subparts[1]).getSleightType()!=Sleight.SleightType.CHI )
			{
				throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " is not a Psi Chi sleight");
			}
			else
			{
				Sleight s = Sleight.sleightList.get(subparts[1]);
				
				params.put(1, s);
			}
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		EpCharacter pc = env.getPC();
		Sleight s = (Sleight)params.get(1);
		
		pc.sleights().put(s.getName(), s);
		
		return "";
	}
	
	public String toString()
	{
		return "Add Psi-Chi Sleight:" + subparts[1];
	}

}
