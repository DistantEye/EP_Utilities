/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;

/**
 * Static only class responsible for building ConditionalStatement objects out of input strings
 * Syntax
 * ?hasTrait(trait)
 * ?hasSkill(skill)
 * ?skillIsType(skill,type)  (skill is name of skill, type is a type you want it to be, like Technical
 * ?hasBackground
 * ?hasHadBackground
 * ?hasRolled(number)
 * ?equals(string1,string2)
 * ?hasVar(varname)
 * ?between(input,lower,upper)
 * 
 * @author Vigilant
 *
 */
public class ConditionalBuilder {

	/**
	 * Returns appropriate subclass of Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public static ConditionalStatement getConditional(String input,Command parent)
	{
		String condName = Command.getCommandName(input);
		String lcCond = condName.toLowerCase();
		
		// special cases first for AND and OR
		if (input.contains("||"))
		{
			return new OrConditional(input,parent);
		}
		else if (input.contains("&&"))
		{
			return new AndConditional(input,parent);
		}
		
		if (lcCond.startsWith("?"))
		{
			lcCond = lcCond.substring(1);
		}
		else if (lcCond.startsWith("!"))
		{
			input = input.replace(condName, "?"+condName.substring(1));
			return new NotConditional(input,parent);
		}
		else
		{
			throw new IllegalArgumentException("No valid conditional recognized : must start with ? or !");
		}

		if (lcCond.startsWith("hastrait"))
		{
			return new HasTraitConditional(input,parent);
		}
		else if (lcCond.startsWith("hasskill"))
		{
			return new HasSkillConditional(input,parent);
		}
		else if (lcCond.startsWith("skillistype"))
		{
			return new SkillIsTypeConditional(input,parent);
		}
		else if (lcCond.startsWith("hasbackground"))
		{
			return new HasBackgroundConditional(input,parent);
		}
		else if (lcCond.startsWith("hashadbackground"))
		{
			return new HasHadBackgroundConditional(input,parent);
		}
		else if (lcCond.startsWith("hasrolled"))
		{
			return new HasRolledConditional(input,parent);
		}
		else if (lcCond.startsWith("equals"))
		{
			return new EqualsConditional(input,parent);
		}
		else if (lcCond.startsWith("hasvar"))
		{
			return new HasVarConditional(input,parent);
		}
		else if (lcCond.startsWith("between"))
		{
			return new BetweenConditional(input,parent);
		}
		else
		{
			throw new IllegalArgumentException("No valid conditional recognized");
		}
		
		
	}
	
	

}
