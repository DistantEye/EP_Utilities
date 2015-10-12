package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Directive of syntax:
 * getVar(<name>)			(returns data stored for this var) (some character fields can be accessed via {}, like _nextPath)
 * 
 * @author Vigilant
 *
 */
public class GetVarDirective extends Directive {

	/**
	*Creates a Directive from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public GetVarDirective(String input) {
		super(input);

		// no advanced validation on this command is particularly possible
		subpartsToParams();
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {
		ensureStrings(1, 1, env);
		
		String varName = getStrParam(1);
		
		// getVar will do it's own validation as to whether varName exists
		// we want this to throw an error if varName is invalid.
		return env.getPC().getVar(varName);
	}

}
