package com.github.distanteye.ep_utils.containers;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Container for Stats, a general class for encapsulating stats
 * Stats are, at minimum, a name and a non-negative integer value
 * @author Vigilant
 */
public class Stat {
	private String name;
	private int value;
	
	/**
	 * @param name Name of Stat type
	 * @param value Current value, should be non-negative
	 */
	public Stat(String name, int value) {
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
	 * Sets Stat value to val
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
	
	public String toXML()
	{
		Element root = new Element("stat");
		Document doc = new Document(root);
		
		doc.getRootElement().addContent(new Element("name").setText( name ));
		doc.getRootElement().addContent(new Element("value").setText( ""+value ));
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setOmitDeclaration(true));
		
		return xmlOut.outputString(doc);
	}
	
	public static Stat fromXML(String xml)
	{
		Document document = Utils.getXMLDoc(xml);
		Element root = document.getRootElement();
		
		Utils.verifyTag(root, "stat");
		Utils.verifyChildren(root, new String[]{"name","value"});
		
		String nameStr = root.getChildText("name");
		String valueStr = root.getChildText("value");
		
		int val = -1;
		if (Utils.isInteger(valueStr))
		{
			val = Integer.parseInt(valueStr);
		}
		else
		{
			throw new IllegalArgumentException("Value must be an integer.");
		}
		
		return new Stat(nameStr,val);
	}
}
