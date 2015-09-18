/**
 * 
 */
package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * An internal only representation for effects Strings that contain multiple commands
 * For both run and toString, will attempt to resolve the Commands in sequence
 * 
 * While advantageous to keep as a child of Command, this class does not fully support all Command functions
 * 
 * @author Vigilant
 *
 */
public class CommandList extends Command {	
	/**
	 *Creates a CommandList from the given effects string
	 * @param input list of commands delimited by ;
	*/
	public CommandList(String input) {
		super(input);
		
		subparts = Utils.splitCommands("CommandList;" + input,";");
		
		for (int i = 1; i < subparts.length; i++)
		{
			params.set(i, CommandBuilder.getCommand(subparts[i]));
		}
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
	 * Returns a join of the remaining effects after (not including) idx in params
	 * @param idx positive int < params.size()
	 * @return String containing the effect Strings for all commands in subparts after idx
	 */
	private String remainingEffects(int idx)
	{
		String[] temp = new String[params.size()-idx];
		
		int tempIdx = 0;
		for (int i = idx+1; i < subparts.length; i++)
		{
			temp[tempIdx++] = subparts[i];
		}
		
		return Utils.joinStr(temp, ";");
	}
	
	/**
	 * Attempts to resolve and run the command, checking readiness and conditionals (if applicable)
	 * CommandList will run all commands contained within in sequence, only breaking the sequence if a particular exit condition arises. In the case of those
	 * sequences it will return the raw effects Strings for anything it hadn't ran yet.
	 * 
	 * Note that, by design, implementations of run will/should not check before casting objects, since
	 * type checking of params was already supposed to be done during construction and changeParam is highly restricted in changing types
	 *  
	 * @param pc The character to run the command on
	 */
	public String run(CharacterEnvironment env)
	{
		for (int i = 1; i < params.size(); i++)
		{
			String result = ((Command)params.get(i)).run(env);
			
			if (result.length() > 0)
			{
				String addendum = ""; // add any remaining effects if they exist
				
				if (i < params.size() - 1)
				{
					addendum = ";" + remainingEffects(i);
				}
				
				return result + addendum;
			}
		}
		
		return "";
	}
	
	public String toString()
	{
		if (params.size() <= 0)
		{
			return "";
		}
		else
		{
			String result = params.get(1).toString();
			for (int i = 2; i < params.size(); i++)
			{
				result += ";" + params.get(i).toString();
			}
		
			return result;
		}
	}
}
