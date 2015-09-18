package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Directive of syntax:
 * concat(<value1>,<value2>) (appends value2 to the end of value1)
 * getRand(<type>)			(picks random item from all possibilities APT,DERANGEMENT, etc)
 * getRandFromChar(<type>) (picks random item that the character already has)
 * getVar(<name>)			(returns data stored for this var) (some character fields can be accessed via {}, like {nextPath})
 * rollDice(<sides>,<message>)			players can choose the result of this if choose mode is on
 * simpRollDice(<numDice>,<sides>)		players cannot choose the result of this (always forceRoll true)
 * add(num1,num2)						Math method : adds num1+num2
 * mult(num1,num2)						Math method : mult num1*num2
 * 
 * @author Vigilant
 *
 */
public class MultDirective extends Directive{


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
	public String process(EpCharacter pc) {		
		ensureIntegers(1, 2, pc);
		
		int left = getIntParam(1);
		int right = getIntParam(2);
		
		return String.valueOf(left*right);
	}

}
