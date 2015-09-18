package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

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
public class GetVarDirective extends Directive {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public GetVarDirective(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {
		// TODO Auto-generated method stub
		return null;
	}

}
