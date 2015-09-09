/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;

/**
 * ConditionalStatement class gives the basic structure to all conditionals
 * 
 * @author Vigilant
 *
 */
public abstract class ConditionalStatement extends Command {
	private boolean isInverted;
	protected Command parent; // the containing command for this Conditional
	
	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public ConditionalStatement(String input, Command parent) {
		super(input);
		this.parent = parent;
	}
	
	/**
	 * Returns appropriate subclass of Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public static ConditionalStatement getConditional(String input)
	{
		return null;
	}
	
	/**
	 * Executes the Conditional, returning true/false as appropriate
	 * @param playerChar Valid EpCharacter to be used when resolving the conditional
	 * @return True or False
	 */
	public abstract boolean resolve(EpCharacter playerChar);
	
	public boolean isInverted() {
		return isInverted;
	}

	public void setInverted(boolean isInverted) {
		this.isInverted = isInverted;
	}

	
}
