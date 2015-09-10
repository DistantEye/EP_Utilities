package com.github.distanteye.ep_utils.commands;

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
	
	public String toString()
	{
		return "Set character's NextPath to " + subparts[1];
	}

}
