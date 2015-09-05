
package com.github.distanteye.ep_utils.wrappers;

/**
 * Extension of MathWrapper that adds the two values together
 * @author Vigilant
 *
 */
public class SumWrapper extends MathWrapper {

	/**
	 * @param left Any AccessWrapper that carries an integer parseable value (checked at run, not at construction)
	 * @param right Any AccessWrapper that carries an integer parseable value (checked at run, not at construction)
	 */
	public SumWrapper(AccessWrapper<String> left, AccessWrapper<String> right) {
		super(left, right);
	}

	/* (non-Javadoc)
	 * @see com.github.distanteye.ep_utils.wrappers.AccessWrapper#getValue()
	 */
	@Override
	public String getValue() {
		return String.valueOf(getIntVal(left) + getIntVal(right));
	}

}
