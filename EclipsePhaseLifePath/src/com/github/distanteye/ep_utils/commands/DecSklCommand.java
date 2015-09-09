package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * decSkl(<skill>,<value/all>)					(decSkl all will set two variables {lastRemSkl} {lastRemSklVal}, equal to what was removed)
 * decSkl(<skill>,<value/all>,<conditional>)	' the three parameter versions throw an error if the conditional isn't true		
 * 
 * @author Vigilant
 *
 */
public class DecSklCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public DecSklCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
