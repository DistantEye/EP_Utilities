package com.github.distanteye.ep_utils.commands;

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
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
		else
		{
			subpartsToParams();
		}
		
	}

	public String toString()
	{
		return "Player makes choice according to Text(" + subparts[1] + ")";
	}
	
}
