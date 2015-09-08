package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * package(<name>)				(add package -- assume 1 PP if it needs a value)
 * package(<name>,<value>)		(add package of a certain PP value)
 * 
 * @author Vigilant
 *
 */
public class PackageCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public PackageCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
