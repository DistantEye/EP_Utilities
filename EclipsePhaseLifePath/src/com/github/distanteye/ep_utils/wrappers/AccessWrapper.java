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
	
	/**
	 * Returns the Type of Wrapper, usually this is the Class name
	 * @return String representing object identifier
	 */
	public String getType()
	{
		return this.getClass().getName();
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof AccessWrapper)
		{
		   @SuppressWarnings("unchecked") // mostly safe cast other than the generics
		   AccessWrapper<T> temp = (AccessWrapper<T>)o;
		   
		   return this.getType().equals(temp.getType());
		}
		else
		{
			return false;
		}
	}
}
