package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * gear(<gearName>)
 * 
 * @author Vigilant
 *
 */
public class GearCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public GearCommand(String input) {
		super(input);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Invalidly formatted effect " + input + ")");
		}
		
		subpartsToParams();
	}
	
	public String toString()
	{
		return "Add gear to character " + subparts[1];
	}

}
