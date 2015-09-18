package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;
import com.github.distanteye.ep_utils.containers.Aptitude;
import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * addApt(<aptitudeName>,<value>)					(can also be used to subtract with a negative value)
 * addApt(<aptitudeName>,<value>,<conditional)		(the three parameter version throw an error if the conditional isn't true)
 * 
 * @author Vigilant
 *
 */
public class AddAptCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public AddAptCommand(String input) {
		super(input);

		if (subparts.length != 3 && subparts.length != 4 )
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
		else if (subparts[1].length() > 0 )
		{
			
			if (Aptitude.exists(subparts[1]) || isUncertain(subparts[1]))
			{
				params.set(1, subparts[1]);
			}
			else
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a valid aptitude");
			}
			
			// we can't parse if it's an wildcard/choice/etc but we can still store it as "valid"
			if (Utils.isInteger(subparts[2]) )
			{
				params.set(2, Integer.parseInt(subparts[2]));
			}
			else if ( isUncertain(subparts[2]))
			{
				params.set(2, subparts[2]);
			}
			else
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			
			
			if (subparts.length == 4)
			{	
				this.cond = ConditionalStatement.getConditional(subparts[3], this);
				params.set(3,cond);
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
		pc.stats().get(params.get(1)).addValue((Integer)params.get(2));
		
		return "";
	}
	
	public String toString()
	{
		String result = "Add " + subparts[2] + " to APT:" + subparts[1];
		
		if (subparts.length == 4)
		{
			result += ", Conditional must be true: " + cond.toString();
		}
		
		return result;
	}
}
