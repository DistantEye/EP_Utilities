/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;

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
public class AndConditional extends ConditionalStatement {

	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public AndConditional(String input, Command parent) {
		super(input, parent);
		
		
		params.put(1, ConditionalStatement.getConditional(subparts[0], parent));
		params.put(2, ConditionalStatement.getConditional(subparts[1], parent));
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
		
		params.put(0, subparts[0]); // we always want to set commandname to params 0
		
		return new String[]{"AND",part1,part2};
	}
	
	@Override
	public boolean resolve(EpCharacter playerChar) {
		// we control params so we know this will be a safe cast
		ConditionalStatement left = (ConditionalStatement)params.get(1);
		ConditionalStatement right = (ConditionalStatement)params.get(2);
		
		return left.resolve(playerChar) && right.resolve(playerChar);
	}

	public String toString()
	{
		// we control params so we know this will be a safe cast
		ConditionalStatement left = (ConditionalStatement)params.get(1);
		ConditionalStatement right = (ConditionalStatement)params.get(2);
		return "(" + left.toString() + ") AND (" + right.toString() +")"; 
	}

}
