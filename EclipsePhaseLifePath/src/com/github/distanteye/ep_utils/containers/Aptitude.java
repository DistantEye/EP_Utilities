package com.github.distanteye.ep_utils.containers;
/**
 * Container for Aptitudes, an EpCharacter's primary stat type
 * @author Vigilant
 */
public class Aptitude extends PrimaryStat {
	// APT_MAX normally 30 (EP Core 122), 40 possible with Exceptional Aptitude (EP Core 146)
	public static final int APTITUDE_MAX = 40;
	public static String[] TYPES = {"COG","COO","INT","REF","SAV","SOM","WIL"};
	
	/**
	 * @param name Name of aptitude
	 * @param value Current value, should be between 1 and 40
	 */
	public Aptitude(String name, int value) {
		super(name,value);
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
			super.setValue(val);
		}
	}
	
	/**
	 * Checks the predefined Aptitudes to see if one exists with the given name
	 * @param aptName Name of Aptitude to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String aptName)
	{
		// loop throught our morphs and see if we find a match
		for (String apt : TYPES)
		{
			if (apt.equalsIgnoreCase(aptName))
			{
				return true;
			}
		}
		
		return false;
	}
}
