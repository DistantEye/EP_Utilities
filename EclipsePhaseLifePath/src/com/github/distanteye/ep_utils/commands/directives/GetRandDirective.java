package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.containers.Trait;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Directive of syntax:
 * getRand(<type>)			(picks random item from all possibilities DERANGEMENT, APT, SKILL, SKILL_CHAR, etc)
 * 
 * @author Vigilant
 *
 */
public class GetRandDirective extends Directive {
	/**
	*Creates a Directive from the given effects string
	* @param input Valid formatted command effect string, this should be the full String with command name and  still
	*/
	public GetRandDirective(String input) {
		super(input);

		// no advanced validation on this command is particularly possible
		subpartsToParams();
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.commands.directives.Directive#process(com.github.distanteye.ep_utils.containers.EpCharacter)
	 */
	@Override
	public String process(CharacterEnvironment env) {
		ensureStrings(1, 1, env);
		
		String type = getStrParam(1);
		
		if (type.equals("DERANG"))
		{
			return Trait.getRandomDerangement(env.getRng()).getName();
		}
		else if (type.equals("APT"))
		{
			return Trait.getRandomDerangement(env.getRng()).getName();
		}
		else if (type.equals("SKILL_CHAR"))
		{
			return env.getPC().getRandSkill(env.getRng());
		}
		else if (type.equals("SKILL"))
		{
			return Skill.getRandomSkill(env.getRng(), 1).getFullName();
		}
		else
		{
			throw new IllegalArgumentException("Type(" + type + ") not supported by getRand()");
		}
	}

}
