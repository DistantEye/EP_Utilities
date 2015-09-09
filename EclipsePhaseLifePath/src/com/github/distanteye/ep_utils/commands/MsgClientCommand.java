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
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public MsgClientCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
