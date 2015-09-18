/**
 * 
 */
package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.containers.EpCharacter;

/**
 * Directives encapsulate a preprocessing command that should be processed and replaced before execution of the actual command.
 * @author Vigilant
 *
 */
public abstract class Directive extends Command {

	public static final String[] DIRECTIVES = {"concat","getRand","getRandFromChar","getVar","rollDice","simpRollDice","add","mult"};
	
	/**
	* Creates a Directive from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public Directive(String input) {
		super(input);
	}
	
	/**
	 * For a given effects string, pulls the name of the first Directive in the string
	 * @param input Valid input string, this should be the full String with command name and () still
	 * @return Command name such that is the first text before an '(' character in string
	 */
	public static String getDirective(String input)
	{
		return input.substring(0,input.indexOf('('));
	}
	
	/**
	 * Processes the directive, returning an appropriate result to replace the directive with.
	 * Note the directive itself is not replaced, only the replace value is given
	 * @param pc Character object, sometimes needed for context for making the decision
	 * @return String containing the data replace for this.
	 */
	abstract public String process(EpCharacter pc);
	
	public static boolean containsDirective(String input)
	{
		for (String str : DIRECTIVES)
		{
			if (input.toLowerCase().contains(str.toLowerCase()))
			{
				return true;
			}
		}
		
		return false;
	}

}
