/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Conditional with syntax :
 * ?hasHadBackground(<value>)
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
public class HasHadBackgroundConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public HasHadBackgroundConditional(String input, Command parent) {
		super(input, parent);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Invalidly formatted condition " + input + ")");
		}
		
		
		params.set(1, subparts[1]);
	}

	@Override
	public boolean resolve(CharacterEnvironment env) {
		EpCharacter playerChar = env.getPC();
		
		// we control params, so we know these are safe casts
		String background = (String)subparts[1];
		
		return playerChar.hasHadBackground(background);
	}

	public String toString()
	{
		return "player has had background : " + subparts[1]; 
	}

}
