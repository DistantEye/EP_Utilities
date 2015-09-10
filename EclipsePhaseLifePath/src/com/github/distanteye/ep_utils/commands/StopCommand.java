package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * stop()
 * 
 * @author Vigilant
 *
 */
public class StopCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public StopCommand(String input) {
		super(input);

		if (subparts.length != 1)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
	}

	public String toString()
	{
		return "Orders the UI to stop";
	}
}
