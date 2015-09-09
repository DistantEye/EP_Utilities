package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * morph(<morphname>)
 * morph(randomRoll)
 * 
 * @author Vigilant
 *
 */
public class MorphCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public MorphCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
