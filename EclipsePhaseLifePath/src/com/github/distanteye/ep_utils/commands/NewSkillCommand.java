package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command to represent when a NewSkill is added, these take the form of any statement Skill can parse, ex:
 * Language: English 30
 * Navigation 40
 * etc.
 * 
 * @author Vigilant
 *
 */
public class NewSkillCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full skill String
	*/
	public NewSkillCommand(String input) {
		super(input);
		
		if (!Skill.isSkill(input))
		{
			throw new IllegalArgumentException("Poorly formated effect, input is not a skill : " + input);	
		}
		
		subpartsToParams();
	}
	
	public void changeParam(int key,Object value)
	{
		throw new UnsupportedOperationException();
	}
	
	protected boolean resolveConditional(CharacterEnvironment env)
	{
		return true;
	}
	
	/**
	 * Parses input into a pair of statements representing the name of the command and it's input. NewSkillCommand is a non-standard command that doesn't split parameters
	 * @param input Valid input string, this should be the full Skill String
	 * @return Sting[] of the input split
	 */
	public String[] splitParts(String input)
	{
		return new String[]{"NEW_SKILL",input};
	}
	
	public String run(CharacterEnvironment env)
	{	
		super.run(env);
		
		String effect = getStrParam(1);

		Skill temp = Skill.CreateSkillFromString(effect);
		env.getPC().addSkill(temp);
		
		return "";
	}
	
	public String toString()
	{
		
		String result =  "Add " + subparts[2] + " from Skill " + subparts[1] + " on character";
		
		if (cond != null)
		{
			result += "if true (" + cond.toString() + ")";
		}
		
		return result;
	}

}
