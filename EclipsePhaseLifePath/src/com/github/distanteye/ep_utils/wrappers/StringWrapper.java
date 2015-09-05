package com.github.distanteye.ep_utils.wrappers;

/**
 * Basic implementation for Strings. Stores instance string inside and can get/set it
 * @author Vigilant
 */
public class StringWrapper extends AccessWrapper<String> {
	String value;	
	
	public StringWrapper(String value)
	{
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String item) {
		this.value = item;		
	}

	@Override
	public boolean isInt() {
		return false;
	}

}
