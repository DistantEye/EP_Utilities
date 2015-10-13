/**
 * 
 */
package com.github.distanteye.ep_utils.commands.directives;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

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
	public static String[] DIRECTIVES_LC; // shortcut so we don't have toLower DIRECTIVES all the time
	
	static
	{
		DIRECTIVES_LC = new String[DIRECTIVES.length];
		for (int i = 0; i < DIRECTIVES.length; i++)
		{
			DIRECTIVES_LC[i] = DIRECTIVES[i].toLowerCase();
		}
	}
	
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
	public static String getDirectiveName(String input)
	{
		int idx = input.indexOf('(',0);
		String candidate = input.substring(0,idx);
		
		while (!ArrayUtils.contains(DIRECTIVES_LC, candidate.toLowerCase()))
		{
			idx = input.indexOf('(',idx+1);
			candidate = input.substring(0,idx);
			// this still contains far to much, we take from the tail the variable name
			Matcher m = Pattern.compile("[a-zA-Z0-9_]+$").matcher(candidate);
			if (!m.find())
			{
				throw new IllegalArgumentException("No valid directive could be found inside: " + input);
			}		
			
			candidate = m.group();
		}
		
		return candidate;
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
		for (String str : DIRECTIVES_LC)
		{
			if (input.toLowerCase().contains(str+"("))
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
				params.put(i, temp.process(env));
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
				params.put(i, Integer.parseInt(param));
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
