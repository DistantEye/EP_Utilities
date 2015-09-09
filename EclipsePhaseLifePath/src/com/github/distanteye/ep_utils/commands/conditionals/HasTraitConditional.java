/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.containers.Trait;

/**
 * Conditional with syntax :
 * ?hasTrait(trait)
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
public class HasTraitConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public HasTraitConditional(String input, Command parent) {
		super(input, parent);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Invalidly formatted condition " + input + ")");
		}
		
		if (!Trait.exists(subparts[1]))
		{
			throw new IllegalArgumentException("Trait : " + subparts[1] + " does not exist!");
		}
		
		params.put(0, subparts[0]);
		params.put(1, subparts[1]);
	}

	@Override
	public boolean resolve(EpCharacter playerChar) {
		return playerChar.traits().containsKeyIgnoreCase(subparts[1]);
	}

	public String toString()
	{
		return "player has Trait : " + subparts[1]; 
	}

}
