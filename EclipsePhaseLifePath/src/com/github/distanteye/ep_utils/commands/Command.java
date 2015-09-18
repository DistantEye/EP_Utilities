package com.github.distanteye.ep_utils.commands;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.distanteye.ep_utils.commands.conditionals.ConditionalStatement;
import com.github.distanteye.ep_utils.commands.directives.Directive;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Abstract class for all Commands. In general, the purpose of a command 
 * is to take in a String in the constructor, validate that the effect is well formed,
 * return the parameters, report on whether a particular effect string matches a particular command,
 * handle wildcard substitution/resolution on prompting, and other helper functions like
 * toString() resolution. A command's parameters can themselves be commands.
 * 
 * A command itself cannot run itself since it lacks full context to do that.
 * 
 * @author Vigilant
 *
 */
public abstract class Command {
	protected String origString;
	
	// for both params and subparts, index 0 is the command name
	protected ArrayList<Object> params; // Params can be Strings, Integers, Funcs, Tables, or other Commands, unfortunately
											  // the emphasis on controlled, well-formed Command subclasses is meant to offset this

	protected String[] subparts; // subparts are the raw String version of params. Oftentimes this is enough. Params is for more advanced behaviors
	protected ConditionalStatement cond;
	protected boolean forceRoll; // any dice rolled in this command cannot be chosen manually
	
	public Command(String input)
	{
		origString = input; // used to preserve what's there after wildcard/choice substitution
		params = new ArrayList<Object>();
		subparts = splitParts(input);
		cond = null; // null by default
	}
	
	public boolean isForceRoll() {
		return forceRoll;
	}

	public void setForceRoll(boolean forceRoll) {
		this.forceRoll = forceRoll;
	}

	/**
	 * For a given effects string, pulls the name of the first command in the string
	 * @param input Valid input string, this should be the full String with command name and () still
	 * @return Command name such that is the first text before an '(' character in string
	 */
	public static String getCommandName(String input)
	{
		return input.substring(0,input.indexOf('('));
	}
	
	/**
	 * Returns the name of the command for this current object
	 * @return String containing the command's name
	 */
	public String getCommandName()
	{
		return subparts[0];
	}
	
	public void changeParam(int key,Object value)
	{
		if (key >= 0 && key < params.size())
		{
			Object old = params.get(key);
			
			// types must match, or old must be a wildcard/uncertain value
			if (old.getClass().getName().equals(value.getClass().getName()) || 
					(old instanceof String && isUncertain((String)old)))
			{
				params.set(key, value);
			}
			else
			{
				throw new IllegalArgumentException("New param must be same type as old param: " + old.getClass().getName());
			}
		}
		else
		{
			throw new IllegalArgumentException("Params must contain idx(" + key + ")");
		}
	}
	
	/**
	 * Returns the parameter at the given index
	 * @param i valid index within range for params
	 * @return Appropriate parameter for that slot, be it Integer, String, or Command object
	 */
	public Object getParam(int i)
	{
		if (i < 0 || i >= params.size())
		{
			throw new IllegalArgumentException("Index out of range for getParam : " + i);
		}
		else
		{
			return params.get(i);
		}
	}
	
	/**
	 * Shortcut for converting getParam output to String
	 * @param i valid index within range for params
	 * @return Appropriate parameter for that slot, converted to String (using toString);
	 */
	public String getStrParam(int i)
	{
		return getParam(i).toString();
	}
	
	/**
	 * Shortcut for converting getParam output to Int. Note. as this is an internal only shortcut,
	 * it is assumed validation was already done to confirm the value in question is an integer.
	 * @param i valid index within range for params
	 * @return Appropriate parameter for that slot, converted to an integer;
	 */
	protected int getIntParam(int i)
	{
		return Integer.parseInt(getParam(i).toString());
	}
	
	/**
	 * Parses input into an array of subparts, respecting command syntax/nesting. Placed here incase subclasses need to
	 * override this with more specialized behaviors
	 * @param input Valid input string, this should be the full String with command name and () still
	 * @return Sting[] of the input split, may be length 1 if no subparts were found
	 */
	protected String[] splitParts(String input)
	{
		String insideParams = Utils.returnStringInParen(input);
		String commandName = getCommandName(input);
		String[] results = Utils.splitCommands(commandName+","+insideParams);
		
		params.set(0, commandName); // we always want to set commandname to params 0
		return results;
	}
	
	public String[] getSubparts()
	{
		return this.subparts;
	}
	
	/**
	 * Shortcut method for when it's a straight copy from subparts to params
	 */
	public void subpartsToParams()
	{
		for (int i = 0; i < subparts.length; i++)
		{
			params.set(i, subparts[i]);
		}
	}
	
	/**
	 * Returns a human-readable version of the Command. This is a fallback, subclasses should implement their own
	 * @return Returns basic command description
	 */
	public String toString()
	{
		return origString;
	}
	
	/**
	 * Recursive function that splits effects with a high numbered choice parameter like ?3? into single choice parameter effects
	 * trait(Mental Disorder (?3?)) would become trait(Mental Disorder (?1?));trait(Mental Disorder (?1?));trait(Mental Disorder (?1?)) 
	 * @param input
	 * @param buffer Initiall blank list used to store 
	 * @return The split effect String, or "" if nothing could be split
	 */
	public static String splitChoiceTokens(String input, ArrayList<String> buffer)
	{
		Pattern groups = Pattern.compile("\\?([2-9]+)\\?\\**");		
		Matcher m = groups.matcher(input);
		if(!m.find())
		{
			return "";
		}
		int val = Integer.parseInt(m.group(1));
		
		// clone effect for every value over 1, so that ?2? leads to two effects with ?1?
		input = input.replaceFirst("\\?" + val + "\\?","?1?");
		for (int i = 0; i < val; i++)
		{
			buffer.add(input);
		}
		
		ArrayList<String> buffer2 = new ArrayList<String>();
		
		for (String str : buffer)
		{
			splitChoiceTokens(str,buffer2);
		}
		
		buffer.addAll(buffer2);
		
		String result = "";
		
		if (buffer.size() == 0)
		{
			return "";
		}
		else
		{
			result = buffer.get(0);
			
			for (int i = 1; i < buffer.size(); i++)
			{
				result += ";" + buffer.get(i);
			}
		}
		
		return result;
	}
	
	/**
	 * Checks to see if the sample input contains any choice prompts, that is any symbols like ?1?, that prompt the user
	 * to enter a choice for the effect
	 * 
	 * @param input Sample effects string to check for User choice prompts
	 * @return True if the appropriate symbols are in the input, false otherwise
	 */
	public static boolean containsChoice(String input)
	{
		return Pattern.compile("\\?([0-9]+)\\?").matcher(input).find();
	}
	
	/**
	 * Like containsChoice, but returns true if input contains a choice wildcard, as well as any other wildcard or special symbol like !RANDSKILL!
	 * also triggers on preprocessing commands : getVar, rollDice, etc
	 * @param input
	 * @return True or false as appropriate
	 */
	public static boolean isUncertain(String input)
	{
		return containsChoice(input) || Directive.containsDirective(input);
		
	}
	
	/**
	 * Helper method, throws error if the Command's params list still contains any wildcard/ambiguous parameters. Meant to be redefined
	 * if the implementing Command wants a more specialized response.
	 */
	protected void throwErrorIfAmbiguities()
	{
		for (Object o : params)
		{
			if (o instanceof String)
			{
				String str = (String)o;
				if (isUncertain(str))
				{
					throw new IllegalStateException("Uncertainties still exist in params(" + str + "), Command cannot be run at this time.");
				}
			}
		}
	}
	
	/**
	 * Helper method, processes the Command's conditional, if it exists, returning true if the conditional is satisfied, throwing an error if it's not
	 * (this is default behavior for most commands, it can be redefined in the case of others)
	 * 
	 * The boolean return is optional given the error throwing for false and meant more so that subclasses can implement a more useful boolean return
	 * @param pc Character to give the Conditional as context
	 * @return True if Cond is null or resolves to true, IllegalArgumentException if Cond resolves to false
	 */
	protected boolean resolveConditional(CharacterEnvironment env)
	{
		if (cond == null)
		{
			return true;
		}
		else if (cond.resolve(env))
		{
			return true;
		}
		else
		{
			throw new IllegalArgumentException("Condition must be true (" + cond.toString() + ")");
		}
	}
	
	/**
	 * Attempts to resolve and run the command, checking readiness and conditionals (if applicable)
	 * 
	 * Note that, by design, implementations of run will/should not check before casting objects, since
	 * type checking of params was already supposed to be done during construction and changeParam is highly restricted in changing types
	 * 
	 * Note that Command cannot resolve wildcard/uncertainties if they are still present. If it's prompted to anyways, it will throw an error.
	 * If it is about to enter a situation in resolution of run that would result in these situations, it'll dump the pending effects to String and return
	 * them so the context environment can handle it
	 *  
	 * @param pc The character to run the command on
	 * @return Any pending effects after this command has run. Often "". Returning a String not length 0 indicates execution cannot continue until the 
	 * 			context environment does more work on the pending effects
	 */
	public String run(CharacterEnvironment env)
	{
		// we check the active params for any ambiguities. Having wildcards unresolved makes the command nonprocessable
		throwErrorIfAmbiguities();
		
		// if cond exists, we check that next, it must be true or else error
		resolveConditional(env);
		
		// subclasses implement the rest
		return "";
	}
	
}
