/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Special conditional for resolving boolean NOT
 * 
 * @author Vigilant
 *
 */
public class NotConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public NotConditional(String input, Command parent) {
		super(input, parent);
		
		params.put(1, ConditionalBuilder.getConditional(subparts[1], parent));
	}

	/**
	 * Parses input into a a single parameter, the condition this Boolean Not is operating on
	 * @param input Valid input string, this should be the full String with command name and () still.
	 * @return Sting[] of the input split
	 */
	public String[] splitParts(String input)
	{		
		params.put(0, "NOT"); // we always want to set commandname to params 0
		
		return new String[]{"NOT",input};
	}
	
	@Override
	public boolean resolve(CharacterEnvironment env) {
		// we control params so we know this will be a safe cast
		ConditionalStatement left = (ConditionalStatement)params.get(1);
		
		return !left.resolve(env);
	}

	public String toString()
	{
		// we control params so we know this will be a safe cast
		ConditionalStatement left = (ConditionalStatement)params.get(1);
		return "NOT(" + left.toString() + ")"; 
	}

}
