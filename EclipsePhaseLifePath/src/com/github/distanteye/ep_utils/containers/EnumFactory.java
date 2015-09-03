/**
 * 
 */
package com.github.distanteye.ep_utils.containers;

/**
 * Basic common code means of generating Enums for a particular Enum type and String name
 * Case-insensitive check.
 * 
 * @author Vigilant
 *
 */
public class EnumFactory {

	public static <T extends Enum<T>> T getEnum(Class<T> c, String name)
	{
		for (T e : c.getEnumConstants())
		{
			if (name.toUpperCase().equals(e.name()))
			{
				return e;
			}
		}
		
		throw new IllegalArgumentException(name + " is not a valid " + c.getName());
	}
}
