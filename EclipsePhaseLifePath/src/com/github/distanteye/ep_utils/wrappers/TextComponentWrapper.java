/**
 * 
 */
package com.github.distanteye.ep_utils.wrappers;

import javax.swing.text.JTextComponent;

/**
 * Handles access control for the text field of a JTextComponent
 * 
 * @author Vigilant
 *
 */
public class TextComponentWrapper extends AccessWrapper<String> {

	private JTextComponent field;
	
	public TextComponentWrapper(JTextComponent field) {
		this.field = field;
	}

	@Override
	public String getValue() {
		return field.getText();
	}

	@Override
	public void setValue(String item) {
		field.setText(item);
	}

	@Override
	public boolean isInt() {
		return false;
	}

}
