package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Directive of syntax:
 * rollDice(<sides>,<message>)			players can choose the result of this if choose mode is on
 * 
 * @author Vigilant
 *
 */
public class RollDiceDirective extends Directive {

	/**
	*Creates a Directive from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public RollDiceDirective(String input) {
		super(input);

		// validation comes during process()
		subpartsToParams();
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {
		ensureStrings(1,2,env);
		ensureIntegers(1,1,env);
		
		int sides = getIntParam(1);
		String message = getStrParam(2);
		
		return ""+env.rollDice(sides, message, false);
	}

}
