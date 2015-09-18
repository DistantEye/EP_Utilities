/**
 * 
 */
package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Static only class responsible for building Command objects out of input strings
 * @author Vigilant
 *
 */
public class DirectiveBuilder {

	/**
	 * Takes in input string, and runs it against list of possible commands,
	 * returning the appropriate Directive subclass that best matches
	 * @param input Effects string matching a SINGLE valid Directive. 
	 * 			Will throw errors if multiple or unknown directive provided
	 * @return Directive object matching the input string
	 */
	public static Directive getDirective(String input)
	{
		String lcEffect = Directive.getCommandName(input).toLowerCase();
		
		if (Utils.splitCommands(input, ";").length > 1)
		{
			throw new IllegalArgumentException("Can only process a single Directive");
		}
		else if (lcEffect.startsWith("concat"))
		{
			return new ConcatDirective(input);
		}
		else if (lcEffect.startsWith("getRand"))
		{
			return new GetRandDirective(input);
		}
		else if (lcEffect.startsWith("getRandFromChar"))
		{
			return new GetRandFromCharDirective(input);
		}
		else if (lcEffect.startsWith("getVar"))
		{
			return new GetVarDirective(input);
		}
		else if (lcEffect.startsWith("rollDice"))
		{
			return new RollDiceDirective(input);
		}
		else if (lcEffect.startsWith("simpRollDice"))
		{
			return new SimpRollDiceDirective(input);
		}
		else if (lcEffect.startsWith("add"))
		{
			return new AddDirective(input);
		}
		else if (lcEffect.startsWith("mult"))
		{
			return new MultDirective(input);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}

}
