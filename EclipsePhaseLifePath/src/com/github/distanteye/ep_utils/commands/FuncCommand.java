package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * func(<name>)
 * func(<name>,<param1>,<param2>,<...etc>)  (any params passed after name will substitute in for <1>,<2>, etc, in the function 
 * 
 * @author Vigilant
 *
 */
public class FuncCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public FuncCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
