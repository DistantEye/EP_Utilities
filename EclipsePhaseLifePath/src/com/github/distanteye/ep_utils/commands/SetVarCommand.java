package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * setVar(<name>,<value>)
 * 
 * @author Vigilant
 *
 */
public class SetVarCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SetVarCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
