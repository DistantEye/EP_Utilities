/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;

/**
 * Conditional with syntax :
 * ?hastrait(trait)
 * ?hasSkill(skill)
 * ?skillIsType(skill,type)  (skill is name of skill, type is a type you want it to be, like Technical
 * ?hasBackground
 * ?hasHadBackground
 * ?hasRolled(number)
 * ?equals(string1,string2)
 * ?hasVar(varname)
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
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public BetweenConditional(String input, Command parent) {
		super(input, parent);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.ConditionalStatement#resolve()
	 */
	@Override
	public boolean resolve() {
		// TODO Auto-generated method stub
		return false;
	}

}
