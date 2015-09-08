package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * roll(<dieNumber>,#-#=effect/#-#=effect)  (list can be as long as needed)		(ex, roll(1-6=morph,splicer/7-10=morph(bouncer)  ) 
 * forceRoll can be used to make sure a user in interactive mode still rolls these 
 * 
 * @author Vigilant
 *
 */
public class RollCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public RollCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
