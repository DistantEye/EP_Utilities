package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * faction(<name>)
 * 
 * @author Vigilant
 *
 */
public class FactionCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public FactionCommand(String input) {
		super(input);
		
		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		
		subpartsToParams();
	}
	
	public String toString()
	{
		return "Set character faction to " + subparts[1];
	}

}
