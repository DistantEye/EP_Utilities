package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Morph;

/**
 * Command of following syntax types:
 * morph(<morphname>)
 * morph(randomRoll)
 * 
 * @author Vigilant
 *
 */
public class MorphCommand extends Command {
	private boolean randomRoll;
	
	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public MorphCommand(String input) {
		super(input);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}

		if (subparts[1].equalsIgnoreCase("randomroll"))
		{
			params.put(1, "randomroll");
			randomRoll = true;
		}
		else if (subparts[1].length() > 0)
		{
			if (Morph.exists(subparts[1]) || isUncertain(subparts[1]))
			{
				params.put(1, subparts[1]);
				randomRoll = false;
			}
			else
			{
				throw new IllegalArgumentException("Morph does not exist : " + subparts[1]);
			}
			
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}
	
	

	public boolean isRandomRoll() {
		return randomRoll;
	}



	public String toString()
	{
		return "Set character morph to " + subparts[1];
	}
}
