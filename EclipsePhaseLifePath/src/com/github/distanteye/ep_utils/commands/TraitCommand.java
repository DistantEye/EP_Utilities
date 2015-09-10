package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Trait;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * trait(<trait>)
 * trait(<trait>,level)
 * 
 * @author Vigilant
 *
 */
public class TraitCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public TraitCommand(String input) {
		super(input);

		Trait t = null;
		
		if (subparts.length < 2 || subparts.length > 3)
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
		else if (subparts.length == 2 && Trait.exists(subparts[1]))
		{
			t = Trait.getTrait(subparts[1], 1);
			params.put(2,1);

		}
		else if (subparts.length == 2 && Trait.existsPartial(subparts[1]) )
		{
			t = Trait.getTraitFromPartial(subparts[1], 1);
			params.put(2,1);
		}
		else if (subparts.length == 3 && Trait.exists(subparts[1]) )
		{
			if (! Utils.isInteger(subparts[2]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			
			t = Trait.getTrait(subparts[1], Integer.parseInt(subparts[2]));

		}
		else if (subparts.length == 3 && Trait.existsPartial(subparts[1]) )
		{
			if (! Utils.isInteger(subparts[2]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
			}
			
			t = Trait.getTraitFromPartial(subparts[1], Integer.parseInt(subparts[2]));
		}
		else
		{
			throw new IllegalArgumentException("Trait " + subparts[1] + " does not exist, or other formating problem: ("+ input + ")");
		}
		
		params.put(1, t);
		params.put(2, subparts[2]);
	}
	
	public String toString()
	{
		return "Add Trait(" + subparts[1]  + ") to character, with level: " + params.get(2);  
	}

}
