/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;

/**
 * Conditional with syntax :
 * ?equals(string1,string2)
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
public class EqualsConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public EqualsConditional(String input, Command parent) {
		super(input, parent);
		
		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Invalidly formatted Equals condition (wrong number of parts " + input + ")");
		}
		
		
		params.put(1, subparts[1]);
		params.put(2, subparts[2]);
	}

	@Override
	public boolean resolve(EpCharacter playerChar) {
		return subparts[1].equals(subparts[2]);
	}

	public String toString()
	{
		return subparts[1] + " = " + subparts[2]; 
	}

}
