package com.github.distanteye.ep_utils.wrappers;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * An abstract aggregation wrapper : it does not support setValue,
 * but it's getValue will call getValue from left and right, and child classes
 * can easily add, subtract, etc, those values
 * 
 * @author Vigilant
 *
 */
public abstract class MathWrapper extends AccessWrapper<String> {
	protected AccessWrapper<String> left,right;

	/**
	 * @param left Any AccessWrapper that carries an integer parseable value (checked at run, not at construction)
	 * @param right Any AccessWrapper that carries an integer parseable value (checked at run, not at construction)
	 */
	public MathWrapper(AccessWrapper<String> left, AccessWrapper<String> right) {
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Retrieves the integer value from a AccessWrapper
	 * @param aw Any valid integer containing AccessWrapper
	 * @return Either the integer value contained underneath aw, or 0 if aw didn't hold a valid int
	 */
	protected int getIntVal(AccessWrapper<String> aw)
	{
		String str = aw.getValue();
		// we convert blank values into 0's
		if (str.trim().length() == 0)
		{
			return 0;
		}
		
		// make sure we have integers
		if (Utils.isInteger(str))
		{
			return Integer.parseInt(str);			
		}
		else
		{
			return 0; // silent fail
		}
	}

	@Override
	public void setValue(String item) {
		throw new UnsupportedOperationException("MathWrappers can't use setValue()");
	}

	@Override
	public boolean isInt() {
		return true;
	}
	
}
