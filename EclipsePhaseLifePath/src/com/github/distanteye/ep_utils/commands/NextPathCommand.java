package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command of following syntax types:
 * nextPath(<name>)
 * 
 * @author Vigilant
 *
 */
public class NextPathCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public NextPathCommand(String input) {
		super(input);
		
		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
		subpartsToParams();
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		EpCharacter pc = env.getPC();
		pc.setVar("{path}",getStrParam(1));
		
		return "";
	}
	
	public String toString()
	{
		return "Set character's NextPath to " + subparts[1];
	}

}
