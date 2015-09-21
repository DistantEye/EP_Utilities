package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;

/**
 * Command of following syntax types:
 * loadCharBackup()
 * 
 * @author Vigilant
 *
 */
public class LoadCharBackupCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public LoadCharBackupCommand(String input) {
		super(input);

		if (subparts.length != 1)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
		}
	}
	
	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		env.loadBackup();
		
		return "";
	}

	public String toString()
	{
		return "Loads the current character state from backups";
	}
}
