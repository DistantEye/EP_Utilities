/**
 * 
 */
package com.github.distanteye.ep_utils.commands.directives;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

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
	abstract public String process(CharacterEnvironment env);
	
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

	/**
	 * Loops through params, ensuring that all indexes in params between lowRange and highRange (inclusive)
	 * are either String/basic types, or Directives that can be resolved(by end of execution these are resolved as such)
	 * 
	 * By the end of execution, the params within the specified range will either be String values or primitive types
	 * 
	 * @param lowRange idx within params to start at
	 * @param highRange idx within params to finish at
	 * @param pc Character object to provide context for resolving directives.
	 */
	protected void ensureStrings(int lowRange, int highRange, CharacterEnvironment env)
	{
		
		for (int i = lowRange; i <= highRange; i++)
		{
			String param = getStrParam(i);
			if (containsDirective(param))
			{
				Directive temp = DirectiveBuilder.getDirective(param);
				params.set(i, temp.process(env));
				i--; // go back and make sure there aren't more directives nested (this may further loop before concluding)
			}

		}
		
	}
	
	/**
	 * Loops through params, ensuring that all indexes in params between lowRange and highRange (inclusive)
	 * are either Integer values, or Directives that can be resolved to an integer value (by end of execution these are resolved as such)
	 * 
	 * By the end of execution, the params within the specified range will either be integers or an error will have been thrown
	 * 
	 * Calls ensureStrings() as a part of execution.
	 * 
	 * @param lowRange idx within params to start at
	 * @param highRange idx within params to finish at
	 * @param pc Character object to provide context for resolving directives.
	 */
	protected void ensureIntegers(int lowRange, int highRange, CharacterEnvironment env)
	{
		ensureStrings(lowRange,highRange,env);
		// we have to check whether we have integers or Directives, and process the latter the rest of the way if need be
		for (int i = lowRange; i <= highRange; i++)
		{
			String param = getStrParam(i);
			if (Utils.isInteger(param))
			{
				params.set(i, Integer.parseInt(param));
			}
			else
			{
				throw new IllegalArgumentException(subparts[i] + " cannot be evaluated to an integer!");
			}
		}
	}
	
	
	public String toString()
	{
		return origString;
	}
}
