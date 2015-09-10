/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Conditional with syntax :
 * ?between(input,lower,upper)
 * 
 * $0,$1,$2,$3, etc when inside conditionals references the subparams of the effect containing the conditional, so 
 * incSkl(<skill>,<number>,<conditional>) leads to $0 accessing incSkl, $1 accessing <skill> and so on
 * 
 * || and && are partially supported
 * 
 * replacing ? with ! is for boolean not. so !hasTrait;trait => not having that trait
 * 
 * @author Vigilant
 *
 */
public class BetweenConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public BetweenConditional(String input, Command parent) {
		super(input, parent);

		if (subparts.length != 4)
		{
			throw new IllegalArgumentException("Invalidly formatted between condition (wrong number of parts " + input + ")");
		}
		
		// check all conditions for being valid numbers
		for (int i = 1; i < subparts.length; i++)
		{
			if (!Utils.isInteger(subparts[i]))
			{
				throw new IllegalArgumentException("Invalidly formatted between condition ( " + subparts[i] + ") is not an number");
			}
		}
		
		
		params.put(1, Integer.parseInt(subparts[1]));
		params.put(2, Integer.parseInt(subparts[2]));
		params.put(3, Integer.parseInt(subparts[3]));
		
	}

	@Override
	public boolean resolve(EpCharacter playerChar) {
		// we control params, so we know these are safe casts
		int input = (Integer)params.get(1);
		int low = (Integer)params.get(2);
		int high = (Integer)params.get(3);
		
		return input >= low && input <= high;
	}



}
