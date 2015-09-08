package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * msgClient(<message>)					(says something to the UI about character changes)
 * 
 * @author Vigilant
 *
 */
public class MsgClientCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public MsgClientCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
