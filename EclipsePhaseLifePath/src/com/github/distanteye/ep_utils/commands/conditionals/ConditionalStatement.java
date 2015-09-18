/**
 * 
 */
package com.github.distanteye.ep_utils.commands.conditionals;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.core.CharacterEnvironment;

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
	
	/**
	 * Returns appropriate subclass of Conditional based on the input provided 
	 * @param input Validly formated conditional. Should still contain the command and ? or ! prefix
	 * @param Command that contains the calling conditional
	 * @return Conditional object (a subclass, as Conditional is abstract)
	 */
	public static ConditionalStatement getConditional(String input,Command parent)
	{
		// special cases first for AND and OR
		if (input.contains("||"))
		{
			return new OrConditional(input,parent);
		}
		else if (input.contains("&&"))
		{
			return new AndConditional(input,parent);
		}
		else
		{
			throw new IllegalArgumentException("No valid conditional recognized");
		}
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
