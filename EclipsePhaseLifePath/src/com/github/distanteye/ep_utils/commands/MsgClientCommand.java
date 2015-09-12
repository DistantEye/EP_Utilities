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

		if (subparts.length != 2 )
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if ( subparts[1].length() > 0)
		{
			params.set(1, subparts[1]);					
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect (params empty)" + input);
		}
	}

}
