package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Command of following syntax types:
 * extendedChoice(Text,1=effect/2=effect/3=effect/etc)   (this allows us a bit more freedom when a choice is complicated)
 * User is given a list of numbers to choose from, picks one, and that effect triggers
 * 
 * @author Vigilant
 *
 */
public class ExtendedChoiceCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public ExtendedChoiceCommand(String input) {
		super(input);
	
		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if (subparts[1].length() <= 0 && subparts[2].length() <= 0)
		{
			throw new IllegalArgumentException("Poorly formated effect (empty params)" + input);
		}
		else
		{
			subpartsToParams();
			this.setExtraContext(subparts[1]);
		}
		
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		String response = "";						
		int choice = -1;						
		String[] choiceEffects = subparts[2].split("/");
		
		while (!Utils.isInteger(response) || Integer.parseInt(response) <= 0 || Integer.parseInt(response) > choiceEffects.length)
		{
			response = env.getUI().promptUser(subparts[1], "Entering blank will choose a random value"); 
			
			// response should be an integer
			if (Utils.isInteger(response))
			{
				// only do these when we at least get a number
				choice = Integer.parseInt(response);
				choiceEffects = subparts[2].split("/");
			}
			else if (response.length() == 0)
			{
				choice = env.getRng().nextInt(choiceEffects.length)+1;
				response = ""+choice;
				choiceEffects = subparts[2].split("/");
			}
		}
		
		String effect = choiceEffects[choice-1].split("=")[1];
		
		Command c = CommandBuilder.getCommand(effect);

		String result = c.run(env);
		this.setExtraContext(c.getExtraContext()); // inherit any context from inner command
		return result;
	}

	public String toString()
	{
		return "Player makes choice according to Text(" + subparts[1] + ")";
	}
	
}
