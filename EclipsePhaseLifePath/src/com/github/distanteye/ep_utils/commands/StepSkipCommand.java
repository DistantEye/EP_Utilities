package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * stepskip(<name>)			(immediately skip to step of this name)
 * stepskipNoStop(<name>)			(immediately skip to step of this name, doesn't interrupt the UI)
 * 
 * @author Vigilant
 *
 */
public class StepSkipCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public StepSkipCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
