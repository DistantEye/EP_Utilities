package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Directive of syntax:
 * getRand(<type>)			(picks random item from all possibilities APT,DERANGEMENT, etc)
 * 
 * @author Vigilant
 *
 */
public class GetRandDirective extends Directive {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public GetRandDirective(String input) {
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
