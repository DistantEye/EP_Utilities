package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Directive of syntax:
 * simpRollDice(<numDice>,<sides>)		players cannot choose the result of this (always forceRoll true)
 * 
 * @author Vigilant
 *
 */
public class SimpRollDiceDirective extends Directive {

	/**
	 *Creates a command from the given effects string
	 * @param input Valid formatted command effect string, this should be the full String with command name and  still
	 */
	public SimpRollDiceDirective(String input) {
		super(input);
		
		// validation comes during process()
		subpartsToParams();
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {
		ensureIntegers(1,2,env);

		int numDice = getIntParam(1);
		int sides = getIntParam(2);

		int total = 0;
		
		for (int i = 0; i < numDice; i++)
		{
			total += env.rollDice(sides, "", true); 
		}
		
		return ""+total;
	}

}
