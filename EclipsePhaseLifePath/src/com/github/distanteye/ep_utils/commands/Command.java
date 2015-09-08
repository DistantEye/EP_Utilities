package com.github.distanteye.ep_utils.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	protected HashMap<Integer,Object> params; // Params can be Strings, Integers, or other Commands, unfortunately
										// the emphasis on controlled, well-formed Command subclasses is meant to offset this
	protected String[] subparts; // subparts are the raw String version of params
	
	public Command(String input)
	{
		origString = input;
		params = new HashMap<Integer,Object>();
		subparts = getParts(input);
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
	 * Parses input into an array of subparts, respecting command syntax/nesting. Placed here incase subclasses need to
	 * override this with more specialized behaviors
	 * @param input Valid input string
	 * @return Sting[] of the input split, may be length 1 if no subparts were found
	 */
	public String[] getParts(String input)
	{
		return Utils.splitCommands(input);
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
	 * @param input
	 * @return True or false as appropriate
	 */
	public static boolean containsUncertainty(String input)
	{
		if (containsChoice(input))
		{
			return true;
		}
		else
		{
			String[] specialStrs = {"!RANDSKILL!","!RANDAPT!","!RAND_DER!"};
			
			for (String str : specialStrs)
			{
				if (input.contains(str))
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	
	
}
