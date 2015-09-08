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
	* @param input Valid formatted command effect string
	*/
	public RunTableCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
