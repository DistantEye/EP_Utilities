package com.github.distanteye.ep_utils.core;
/**
 * Simple function meant to allow to store references to effects lists that are long and used repeatedly.
 * Functions are shorter versions of packages that will auto-resolve to explain their effects. A convenience item
 * 
 * @author Vigilant
 */
public class Function implements UniqueNamedData {
	private String name;
	private String effect;
	
	
	/**
	 * @param name Name of function
	 * @param effect Raw mechanical effect
	 */
	public Function(String name, String effect) {
		super();
		this.name = name;
		this.effect = effect;
	}
	
	public String getName() {
		return name;
	}

	public String toString()
	{
		return DataProc.effectsToString(this.effect);
	}
	
	/**
	 * Differentiates this class from Table and such, which implement the same interface
	 * @return "function"
	 */
	public String getType()
	{
		return "function";
	}

	/**
	 * Return the effects this function contains
	 * @return The functions effects
	 */
	public String getEffect()
	{
		return this.effect;
	}
	
	
}
