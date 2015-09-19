package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Directive of syntax:
 * concat(<value1>,<value2>) (appends value2 to the end of value1)
 * 
 * @author Vigilant
 *
 */
public class ConcatDirective extends Directive {

	/**
	* Creates a Directive from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public ConcatDirective(String input) {
		super(input);

		subpartsToParams();
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {
		ensureStrings(1,2,env);
		String left = getStrParam(1);
		String right = getStrParam(2);
		
		return left+right;
	}

}
