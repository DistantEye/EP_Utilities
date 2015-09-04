package com.github.distanteye.ep_utils.containers;
/**
 * Container for Aptitudes, a characters primary stat type
 * @author Vigilant
 */
public class Aptitude {
	private String name;
	private int value;
	// APT_MAX normally 30 (EP Core 122), 40 possible with Exceptional Aptitude (EP Core 146)
	public static final int APTITUDE_MAX = 40;
	public static String[] aptitudes = {"COG","COO","INT","REF","SAV","SOM","WIL"};
	
	/**
	 * @param name Name of aptitude
	 * @param value Current value, should be between 1 and 40
	 */
	public Aptitude(String name, int value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String toString()
	{
		return this.name + " " + this.value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Sets Aptitude value to val
	 * Only accepts a range between 1 and APTITUDE_MAX,
	 * will cap to these values if val is outside of them 
	 * @param val Valid integer value
	 */
	public void setValue(int val) {
		if (val < 1)
		{
			val = 1;
		}
		else if (val > APTITUDE_MAX)
		{
			val = APTITUDE_MAX;
		}
		else
		{
			this.value = val;
		}
	}
	
	public void addValue(int value) {
		setValue(getValue() + value);
	}
	
	/**
	 * Checks the predefined Aptitudes to see if one exists with the given name
	 * @param aptName Name of Aptitude to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String aptName)
	{
		// loop throught our morphs and see if we find a match
		for (String apt : aptitudes)
		{
			if (apt.equalsIgnoreCase(aptName))
			{
				return true;
			}
		}
		
		return false;
	}
}
