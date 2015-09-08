package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * rep(<type>,<value>)
 * rep(<type>,<value>,<conditional>)	(as with others, conditional must be true for the command to work)
 *
 * @author Vigilant
 *
 */
public class RepCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public RepCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
