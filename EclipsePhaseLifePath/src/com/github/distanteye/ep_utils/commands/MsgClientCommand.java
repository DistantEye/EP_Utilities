package com.github.distanteye.ep_utils.commands;

import org.apache.commons.lang3.StringUtils;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command of following syntax types:
 * msgClient(<message>)					(says something to the UI about character changes)
 * 
 * @author Vigilant
 *
 */
public class MsgClientCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public MsgClientCommand(String input) {
		super(input);
		
		if (subparts.length < 2 )
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
		else if ( subparts[1].length() > 0)
		{
			// sometimes we have extra commas because of message text
			// these should be escaped, but since it's hard to enforce that from users,
			// auto join parts after the first index
			if (subparts.length > 2)
			{
				String message = subparts[1];
				
				for (int i = 2; i < subparts.length; i++)
				{
					message += "," + subparts[i];
				}
				
				this.subparts = new String[]{ subparts[0], message };
			}
			
			params.put(1, subparts[1]);					
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect (params empty)" + input);
		}
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		env.getUI().statusUpdate(getStrParam(1));
		
		return "";
	}
	
	public String toString()
	{
		return "Print message to client : " + subparts[1];
	}

}
