/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.containers.BaseCharacter;

/**
 * @author Vigilant
 *
 */
public abstract class CharAccessWrapper<T> extends AccessWrapper<T> {
	protected BaseCharacter aChar;
	
	public BaseCharacter getChar() {
		return aChar;
	}
	public void setChar(BaseCharacter aChar) {
		this.aChar = aChar;
	}
	
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
