package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * if(<condition>,<effectWhenTrue>,<effectWhenFalse>)		(The latter can be blank)
 * 
 * @author Vigilant
 *
 */
public class IfCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public IfCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
