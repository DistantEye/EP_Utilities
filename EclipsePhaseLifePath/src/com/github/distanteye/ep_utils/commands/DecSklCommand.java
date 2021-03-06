package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalBuilder;
import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * decSkl(<skill>,<value/all>)					(decSkl all will set two variables _lastRemSkl _astRemSklVal, equal to what was removed)
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
			this.cond = ConditionalBuilder.getConditional(subparts[3],this);
			params.put(3, cond);
		}
		
		// does the Skill exist? or is at least a wildcard value
		if (!Skill.isSkill(subparts[1]) && !isUncertain(subparts[1]))
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
		}

		
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		EpCharacter pc = env.getPC();
		String skillname = getStrParam(1);
		
		// determine whether a dec or a decAll
		if (params.get(2) instanceof Integer)
		{
			int val = (Integer)params.get(2);
			
			if (! pc.incSkill(skillname, val * -1) )
			{
				throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + skillname);
			}
		}
		else
		{
			// must be a 'remove all'
			if (! pc.removeSkill(skillname) )
			{
				throw new IllegalArgumentException("Poorly formated effect, skill does not exist : " + skillname);
			}
		}
		
		return "";
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
