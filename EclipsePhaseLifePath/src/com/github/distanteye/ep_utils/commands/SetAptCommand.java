package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * setApt(<aptitudeName>,<value>)
 * 
 * @author Vigilant
 *
 */
public class SetAptCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public SetAptCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
