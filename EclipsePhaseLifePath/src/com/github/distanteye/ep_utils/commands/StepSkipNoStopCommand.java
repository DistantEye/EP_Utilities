package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * stepskipNoStop(<name>)			(immediately skip to step of this name, doesn't interrupt the UI)
 * 
 * @author Vigilant
 *
 */
public class StepSkipNoStopCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public StepSkipNoStopCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
