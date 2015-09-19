package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Directive of syntax:
 * mult(num1,num2)						Math method : mult num1*num2
 * 
 * @author Vigilant
 *
 */
public class MultDirective extends Directive{

	/**
	*Creates a Directive from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public MultDirective(String input) {
		super(input);
		
		if (subparts.length != 2  || !Utils.isInteger(subparts[1]) || !Utils.isInteger(subparts[2]))
		{
			throw new IllegalArgumentException("Effect : " + input + " calls for add but lacks the correct format");
		}
		
		// check both parameters for being either Integers or other Directives
		for (int i = 1; i< 3; i++)
		{
			if (Utils.isInteger(subparts[i]))
			{
				params.set(i, Integer.parseInt(subparts[i]));
			}
			else if (containsDirective(subparts[i]))
			{
				Directive temp = DirectiveBuilder.getDirective(subparts[i]);
				params.set(i, temp);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {		
		ensureIntegers(1, 2, env);
		
		int left = getIntParam(1);
		int right = getIntParam(2);
		
		return String.valueOf(left*right);
	}

}
