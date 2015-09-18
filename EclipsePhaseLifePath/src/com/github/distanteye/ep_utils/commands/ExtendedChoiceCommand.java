package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command of following syntax types:
 * extendedChoice(Text,1=effect/2=effect/3=effect/etc)   (this allows us a bit more freedom when a choice is complicated)
 * User is given a list of numbers to choose from, picks one, and that effect triggers
 * 
 * @author Vigilant
 *
 */
public class ExtendedChoiceCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public ExtendedChoiceCommand(String input) {
		super(input);
	
		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() <= 0 && subparts[2].length() <= 0)
		{
			throw new IllegalArgumentException("Poorly formated effect (empty params)" + input);
		}
		else
		{
			subpartsToParams();
		}
		
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		// TODO Working here! Am not sure how to resolve the need to prompt the UI.
		// either we give more rights back to lifepath generator or we take some away via also passing UI context to run()
		
		// and strictly speaking if we were to pass that, there'd be no reason why Command couldn't resolve it's own wildcards...
		
		return "";
	}

	public String toString()
	{
		return "Player makes choice according to Text(" + subparts[1] + ")";
	}
	
}
