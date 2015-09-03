package com.github.distanteye.ep_utils.containers;
import java.util.HashMap;

/**
 * Container for Rep types, for the different Reputation networks.
 * Has an exists method, to validate whether a name is a valid Rep type.
 * Rep are intended to only be of certain names/descriptions predefined at start
 * 
 * @author Vigilant
 *
 */
public class Rep implements Comparable<Rep> {
	public static HashMap<String,Rep> repTypes = new HashMap<String,Rep>(); // these are dynamic so no Enums
	
	private String name;
	private String description;
	private String networkingField;
	private int value;
	
	/**
	 * @param name Name of the rep type (short form, usually only a letter like i,@,etc)
	 * @param description Full description of the rep
	 * @param networkingField name of linked networking skill
	 * @param value Value of the rep (0-100)
	 */
	private Rep(String name, String description, String networkingField, int value) {
		super();
		this.name = name;
		this.description = description;
		this.networkingField = networkingField;
		this.setValue(value);
		
	}

	/**
	 * Takes appropriately formed input string and adds it to the list of valid Rep types
	 * @param inputStr Format 'name;description;networkingField'
	 */
	public static void addRepType(String inputStr)
	{
		String[] parts = inputStr.split(";");
		if (parts.length == 3 && parts[0].length() > 0 && parts[1].length() > 0 && parts[2].length() > 0)
		{
			Rep.repTypes.put(parts[0], new Rep(parts[0],parts[1], parts[2], 0));
		}
		else
		{
			throw new IllegalArgumentException("Invalid format for rep : " + inputStr);
		}
	}
	
	/**
	 * Looks up a Rep type and returns a copy of it (with value set to 0) if it exists
	 * @param repName Short form name of the Rep to return (must exist in static repTypes list)
	 * @return
	 */
	public static Rep getCopyOf(String repName)
	{
		if (repTypes.containsKey(repName))
		{
			Rep temp = repTypes.get(repName);
			return new Rep(temp.getName(),temp.getDescription(),temp.getNetworkingField(), 0);
		}
		else
		{
			throw new IllegalArgumentException("No rep exists of type : " + repName);
		}
		
	}
	
	public int compareTo(Rep o)
	{
		return this.name.compareTo(o.getName());
	}
	
	/**
	 * Compares to Rep objects for equality based on title
	 * @param o Rep object to compare to
	 * @return True if o shares the same name as this Rep object, false otherwise
	 */
	public boolean equals(Rep o)
	{
		return this.name.equals(o.getName());
	}
	
	/**
	 * Checks the static list of defined Rep objects and returns whether one of the defined name exists.
	 * 
	 * @param repName String of Rep object to look for 
	 * @return True if repName exists in the collection, false otherwise
	 */
	public static boolean exists(String repName)
	{
		return repTypes.containsKey(repName);
	}
	
	public String toString()
	{
		return name+"-rep : " + value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Sets value, capping between 0 and 100 (resetting to those values if either end is exceeded)
	 * 
	 * The number of cases that could set something outside of the allowed range is so large that it's better
	 * to cap rather than throw an error. Sometimes trying to add things above 100 is expected behavior from other
	 * parts of the program.
	 * 
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
		
		if (this.value < 0 )
		{
			this.value = 0;
		}
		else if (value > 100)
		{
			this.value = 100;
		}
		
	}
	
	/**
	 * Adds value to current Rep value, obeying min/Max values
	 * @param value
	 */
	public void incValue (int value)
	{
		this.setValue(this.getValue() + value);
	}
	
	/**
	 * Adds to value, capping between 0 and 100 (resetting to those values if either end is exceeded)
	 * @param value the value to add (can be negative)
	 */
	public void addValue(int value) {
		this.setValue(value+this.value);
	}

	public String getNetworkingField() {
		return networkingField;
	}

	public void setNetworkingField(String networkingField) {
		this.networkingField = networkingField;
	}
	
	
	
	
}
