package com.github.distanteye.ep_utils.wrappers;

/**
 * AccessWrapper governs the basic set of methods needed for retrieving and storing data
 * from an arbitrary element : getValue and setValue, along with any significantly relevant
 * shortcut methods to boost usability 
 * 
 * @author Vigilant
 */
public abstract class AccessWrapper<T> {
	abstract public T getValue();
	abstract public void setValue(T item);
	
	/**
	 * Reflects whether the underlying value is an Integer or not
	 * 
	 * This provides an accessibility/performance bonus to some contexts
	 * 
	 * @return True/False as appropriate 
	 */
	abstract public boolean isInt();
	
}
