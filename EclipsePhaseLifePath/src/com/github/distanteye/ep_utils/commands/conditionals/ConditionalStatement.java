/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * ConditionalStatement class gives the basic structure to all conditionals
 * 
 * @author Vigilant
 *
 */
public abstract class ConditionalStatement extends Command {
	private boolean isInverted;
	protected Command parent; // the containing command for this Conditional
	
	/**
	 * Returns appropriate Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public ConditionalStatement(String input, Command parent) {
		super(replaceParentRefs(input,parent));
		this.parent = parent;
		cond = this; // to patch some interactions with Command as parent
	}
	
	/**
	 * Replaces $0,$1,etc with the corresponding subpart/parameter from parent 
	 * @param input Valid effect String for a conditional
	 * @param parent valid Command object that contains the current conditional represented by input
	 * @return
	 */
	public static String replaceParentRefs(String input, Command parent)
	{
		for (int i = 0; i < parent.getSubparts().length; i++)
		{
			if (input.contains("$"+i))
			{
				input = input.replace("$"+i, parent.getSubparts()[i]);
			}
		}
		
		return input;
	}
	
	public static boolean containsParentRef(String input)
	{
		String insideParams = Utils.stringInParen(input);
		String[] tempParams = Utils.splitCommands(insideParams);
		
		for (int x = 0; x < tempParams.length; x++)
		{
			if (tempParams[x].matches("\\$[0-9]+"))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Executes the Conditional, returning true/false as appropriate
	 * @param playerChar Valid EpCharacter to be used when resolving the conditional
	 * @return True or False
	 */
	public abstract boolean resolve(CharacterEnvironment env);
	
	public boolean isInverted() {
		return isInverted;
	}

	public void setInverted(boolean isInverted) {
		this.isInverted = isInverted;
	}

	
}
