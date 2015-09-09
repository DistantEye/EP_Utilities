package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * psichi(<name>)				(can use ?1?,?2?, etc)
 * 
 * @author Vigilant
 *
 */
public class PsiChiCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public PsiChiCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
