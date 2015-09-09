package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * incSkl(<skill>,<value>)
 * incSkl(<skill>,<value>,<conditional>)
 * 
 * @author Vigilant
 *
 */
public class IncSklCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public IncSklCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
