package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.containers.EpCharacter;

/**
 * Directive of syntax:
 * add(num1,num2)						Math method : adds num1+num2
 * 
 * @author Vigilant
 *
 */
public class AddDirective extends Directive {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public AddDirective(String input) {
		super(input);

	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(EpCharacter pc) {
		// TODO Auto-generated method stub
		return null;
	}

}
