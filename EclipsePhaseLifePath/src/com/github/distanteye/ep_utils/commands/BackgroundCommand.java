package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.EpCharacter;

/**
 * Command of following syntax types:
 * background(<name>)
 * 
 * @author Vigilant
 *
 */
public class BackgroundCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public BackgroundCommand(String input) {
		super(input);
		
		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Invalidly formatted effect " + input + ")");
		}
		
		subpartsToParams();
	}
	
	public String run(EpCharacter pc)
	{
		super.run(pc);
		
		pc.setBackground((String)params.get(1));
		
		return "";
	}
	
	public String toString()
	{
		return "Set character background to " + subparts[1];
	}

}
