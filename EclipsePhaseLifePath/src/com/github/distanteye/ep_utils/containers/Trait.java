package com.github.distanteye.ep_utils.containers;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Container for Eclipse Phase traits.
 * Has an exists method, to validate whether a name is a valid Trait.
 * Trait objects are intended to only be of certain names/descriptions predefined at start
 * 
 * Has at this time one static support method for getting random variants 
 * of a particular trait (Derangement), with more possible in the future 
 * 
 * @author Vigilant
 *
 */
public class Trait {
	private String name;
	private String description;
	private String cost;
	private String bonus;
	private int level; // default to 1
	
	// stores all the below skills
	public static ArrayList<Trait> traitList = new ArrayList<Trait>();
	
	/**
	 * @param name Name of trait
	 * @param description Human readable description of trait 
	 * @param cost String containing various CP costs for the trait (not always used)
	 * @param bonus String containing various CP bonuses for the trait (not always used)
	 * @param level Level of trait : default is 1
	 */
	private Trait(String name, String description, String cost, String bonus, int level) {
		super();
		this.name = name;
		this.description = description;
		this.level = level;
	}
	
	private Trait(Trait t)
	{
		super();
		this.name = t.name;
		this.description = t.description;
		this.level = t.level;
		this.cost = t.cost;
		this.bonus = t.bonus;
	}

	/**
	 * Checks the predefined traits to see if one exists with the given name
	 * @param traitName Name of trait to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String traitName)
	{
		// loop throught our traits and see if we find a match
		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the predefined traits to see if one exists with the given name. Will do a partial (startsWith) search
	 * @param traitName Name of trait to search for
	 * @return True/False as appropriate
	 */
	public static boolean existsPartial(String traitName)
	{
		// loop throught our traits and see if we find a match
		for (Trait t : traitList)
		{
			String partialT = t.getName().replaceAll("[ ]*\\([^\\)]+\\)", "").trim();
			
			if (traitName.startsWith(partialT))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Gets a copy of a trait from the predefined list (the level will be mutable, possibly more in the future)
	 * 
	 * @param traitName Name of trait to search for
	 * @param level Level to set the returned trait (if found) to
	 * @return Trait found, or null
	 */
	public static Trait getTrait(String traitName, int level)
	{
		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitName))
			{
				Trait result = new Trait(t);
				result.setLevel(level);
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * Version of getTrait that attempts to retrieve off a partial match, but sets the result to the traitName specified
	 * @param traitName Name of trait to search for
	 * @param level Level to set the returned trait (if found) to
	 * @return @return Trait found, or null
	 */
	public static Trait getTraitFromPartial(String traitName, int level)
	{
		if (existsPartial(traitName))
		{
			String partialTraitName = traitName.replaceAll("[ ]*\\([^\\)]+\\)", "").trim();
			
			for (Trait t : traitList)
			{
				if (t.getName().startsWith(partialTraitName))
				{
					Trait result = new Trait(t);
					result.setLevel(level);
					result.name = traitName;
					return result;
				}
			}
			
			return null;
		}
		else
		{
			return null;
		}		
	}
	
	public String toStringLong()
	{
		return this.toString() + " : " + this.description;
	}
	
	public String toString()
	{
		return this.name + " (" + this.level + ")";
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Creates a new Trait that is stored statically in the class
	 * @param name Name of trait
	 * @param description Human readable description of trait 
	 * @param cost String containing various CP costs for the trait (not always used)
	 * @param bonus String containing various CP bonuses for the trait (not always used)
	 * @param level Level of trait : default is 1
	 */
	public static void CreateInternalTrait(String name, String desc, String cost, String bonus, int level)
	{		
		Trait temp = new Trait(name,desc,cost,bonus,level);
		Trait.traitList.add(temp);
	}
	
	
	public static Trait getRandomDerangement(SecureRandom rng)
	{
		ArrayList<Trait> tempList = new ArrayList<Trait>();
		for (Trait t : traitList)
		{
			if (t.getName().toUpperCase().startsWith("DERANGEMENT"))
			{
				tempList.add(t);
			}
		}
		
		return tempList.get(rng.nextInt(tempList.size()));
	}
	
	public String toXML()
	{
		Element root = new Element("trait");
		Document doc = new Document(root);
		
		doc.getRootElement().addContent(new Element("name").setText( getName() ));
		doc.getRootElement().addContent(new Element("level").setText( ""+getLevel() ));
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setOmitDeclaration(true));
		
		return xmlOut.outputString(doc);
	}
	
	public static Trait fromXML(String xml)
	{
		Document document = Utils.getXMLDoc(xml);
		Element root = document.getRootElement();
		
		Utils.verifyTag(root, "trait");
		Utils.verifyChildren(root, new String[]{"name","level"});
		
		String nameStr = root.getChildText("name");
		String valueStr = root.getChildText("level");
		
		int val = -1;
		if (Utils.isInteger(valueStr))
		{
			val = Integer.parseInt(valueStr);
		}
		else
		{
			throw new IllegalArgumentException("Level must be an integer.");
		}
		
		return getTraitFromPartial(nameStr,val);
	}
}
