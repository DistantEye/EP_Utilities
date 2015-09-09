package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * addApt(<aptitudeName>,<value>)					(can also be used to subtract with a negative value)
 * addApt(<aptitudeName>,<value>,<conditional)		(the three parameter version throw an error if the conditional isn't true)
 * 
 * @author Vigilant
 *
 */
public class AddAptCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public AddAptCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
