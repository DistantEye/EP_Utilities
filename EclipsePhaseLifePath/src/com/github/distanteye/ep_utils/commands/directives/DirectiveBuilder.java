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
	 * @param input Effects string containing at least one valid directive will only match/return the first found
	 * @return Directive object matching the input string
	 */
	public static Directive getDirective(String input)
	{
		String lcEffect = Directive.getCommandName(input).toLowerCase();
		
		// structured so only the first top level directive is returned
		String insides = Utils.returnStringInParen(input, input.indexOf(lcEffect));
		String inputMod = lcEffect + "(" + insides + ")";
		
		if (lcEffect.startsWith("concat"))
		{
			return new ConcatDirective(inputMod);
		}
		else if (lcEffect.startsWith("getRand"))
		{
			return new GetRandDirective(inputMod);
		}
		else if (lcEffect.startsWith("getVar"))
		{
			return new GetVarDirective(inputMod);
		}
		else if (lcEffect.startsWith("rollDice"))
		{
			return new RollDiceDirective(inputMod);
		}
		else if (lcEffect.startsWith("simpRollDice"))
		{
			return new SimpRollDiceDirective(inputMod);
		}
		else if (lcEffect.startsWith("add"))
		{
			return new AddDirective(inputMod);
		}
		else if (lcEffect.startsWith("mult"))
		{
			return new MultDirective(inputMod);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + inputMod);
		}
	}

}
