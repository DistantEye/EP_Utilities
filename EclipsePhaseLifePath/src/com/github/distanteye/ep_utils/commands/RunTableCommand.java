package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * runTable(<tableName>,<number>)
 * runTable(<tableName>,<number>,<wildCardReplace>) (similar to rollTable Except you specify what the number is)
 * 
 * @author Vigilant
 *
 */
public class RunTableCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public RunTableCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
