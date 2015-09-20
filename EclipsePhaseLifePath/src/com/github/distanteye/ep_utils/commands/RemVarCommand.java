package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalBuilder;
import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * remVar(<name>)
 * 
 * @author Vigilant
 *
 */
public class RemVarCommand extends Command {

	/**
	* Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public RemVarCommand(String input) {
		super(input);

		if (subparts.length != 2 )
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() == 0 )
		{
			throw new IllegalArgumentException("Poorly formated effect (params blank) " + input);
		}
		
		params.put(1, subparts[1]);
	
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		env.getPC().removeVar(getStrParam(1));
		
		return "";
	}
	
	public String toString()
	{
		return "Remove character variable: " + getStrParam(1);
	}

}
