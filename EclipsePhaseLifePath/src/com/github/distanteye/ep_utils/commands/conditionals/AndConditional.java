/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Special conditional for resolving boolean And
 * 
 * @author Vigilant
 *
 */
public class AndConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public AndConditional(String input, Command parent) {
		super(input, parent);
		
		
		params.put(1, ConditionalBuilder.getConditional(subparts[1], parent));
		params.put(2, ConditionalBuilder.getConditional(subparts[2], parent));
	}

	/**
	 * Parses input into a pair of statements representing the two parts of the And Conditional
	 * @param input Valid input string, this should be the full String with command name and () still. Must contain &&
	 * @return Sting[] of the input split
	 */
	public String[] splitParts(String input)
	{
		String part1, part2;

		part1 = input.substring(0, input.indexOf("&&"));
		part2 = input.substring(input.indexOf("&&")+2);
		
		params.put(0, "AND"); // we always want to set commandname to params 0
		
		return new String[]{"AND",part1,part2};
	}
	
	@Override
	public boolean resolve(CharacterEnvironment env) {
		// we control params so we know this will be a safe cast
		ConditionalStatement left = (ConditionalStatement)params.get(1);
		ConditionalStatement right = (ConditionalStatement)params.get(2);
		
		return left.resolve(env) && right.resolve(env);
	}

	public String toString()
	{
		// we control params so we know this will be a safe cast
		ConditionalStatement left = (ConditionalStatement)params.get(1);
		ConditionalStatement right = (ConditionalStatement)params.get(2);
		return "(" + left.toString() + ") AND (" + right.toString() +")"; 
	}

}
