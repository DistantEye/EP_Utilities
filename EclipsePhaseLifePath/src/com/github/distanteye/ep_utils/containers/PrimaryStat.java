package com.github.distanteye.ep_utils.containers;
/**
 * Container for PrimaryStats, a generic class for primary stat types
 * @author Vigilant
 */
public class PrimaryStat {
	private String name;
	private int value;
	
	/**
	 * @param name Name of Stat type
	 * @param value Current value, should be non-negative
	 */
	public PrimaryStat(String name, int value) {
		super();
		this.name = name;
		this.setValue(value);
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
	 * Sets PrimaryStat value to val
	 * @param val Valid non negative integer value
	 */
	public void setValue(int val) {
		if (val < 0)
		{
			throw new IllegalArgumentException("val must be non-negative integer");
		}
		else
		{
			this.value = val;
		}
	}
	
	public void addValue(int value) {
		setValue(getValue() + value);
	}
	
}
