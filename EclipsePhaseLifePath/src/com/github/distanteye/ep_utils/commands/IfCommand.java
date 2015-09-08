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
	* @param input Valid formatted command effect string
	*/
	public IfCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
