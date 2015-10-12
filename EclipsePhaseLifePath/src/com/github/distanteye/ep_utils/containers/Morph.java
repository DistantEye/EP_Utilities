package com.github.distanteye.ep_utils.containers;
import java.util.ArrayList;
import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Container for Morphs, holding all possible Morph information.
 * Has static methods for validating whether a Morph exists, and only
 * accepts creating copies of a list of morphs made from CreateInternalMorph
 * 
 * @author Vigilant
 *
 */
public class Morph {
	private String name;
	private MorphType morphType;
	private String description;
	private String implants;
	private HashMap<String,Integer> aptitudeMaximums;
	private int durability;
	private int woundThreshold;
	private int CP;
	private String creditCost;
	private String effects;
	private String notes;
	
	
	// stores all the below skills
	public static ArrayList<Morph> morphList = new ArrayList<Morph>();
	
	/**
	 * @param name Name of morph
	 * @param morphType The type of the morph Biomorph, Infomorph, Synth, Pod. Not case sensitive
	 * @param description Human readable description of morph
	 * @param implants String containing list of implants for the morph
	 * @param aptitudeMaxStr String of aptitude maximums. Can be a single value for all or a single default value with caveats
	 * @param durability int. Durability value of morph (this may include bonuses from implants)
	 * @param woundThreshold int. Wound threshold for morph
	 * @param CP int. CP cost for morph
	 * @param creditCost String containing the cost class and/or minimum credit value for the morph
	 * @param effects Effects string that models the effects caused by possessing the morph 
	 * @param Notes Any remaining notes about the morph
	 */
	private Morph(String name, MorphType morphType, String description, String implants, String aptitudeMaxStr, int durability, int woundThreshold, int CP,
					String creditCost, String effects, String notes) {
		super();
		this.name = name;
		this.morphType = morphType;
		this.description = description;
		this.implants = implants;
		
		this.aptitudeMaximums = new HashMap<String,Integer>();
		
		String[] tempAptMax = aptitudeMaxStr.split(";");
		
		for (String pair : tempAptMax)
		{
			String[] parts = pair.split(":");
			// this should always be safe below since it comes from a controlled source
			this.aptitudeMaximums.put(parts[0], Integer.parseInt(parts[1]));
		}
		
		this.durability = durability;
		this.woundThreshold = woundThreshold;
		this.CP = CP;
		this.creditCost = creditCost;
		this.effects = effects;
		this.notes = notes;
	}
	
	private Morph(Morph t)
	{
		super();
		this.name = t.name;
		this.morphType = t.morphType;
		this.description = t.description;
		this.implants = t.implants;
		
		this.aptitudeMaximums = new HashMap<String,Integer>();
		for (String key : t.aptitudeMaximums.keySet())
		{
			this.aptitudeMaximums.put(key, t.aptitudeMaximums.get(key));
		}
		
		this.durability = t.durability;
		this.woundThreshold = t.woundThreshold;
		this.CP = t.CP;
		this.creditCost = t.creditCost;
		this.effects = t.effects;
		this.notes = t.notes;
	}
	
	public String getImplants() {
		return implants;
	}

	public void setImplants(String implants) {
		this.implants = implants;
	}

	public String getEffects() {
		return effects;
	}

	public void setEffects(String effects) {
		this.effects = effects;
	}

	public String getName() {
		return name;
	}

	public MorphType getMorphType() {
		return morphType;
	}

	public String getDescription() {
		return description;
	}

	public HashMap<String, Integer> getAptitudeMaximums() {
		return aptitudeMaximums;
	}

	public int getDurability() {
		return durability;
	}

	public int getWoundThreshold() {
		return woundThreshold;
	}

	public int getCP() {
		return CP;
	}

	public String getCreditCost() {
		return creditCost;
	}	
	
	public String getNotes() {
		return notes;
	}

	/**
	 * Checks the predefined morphs to see if one exists with the given name
	 * @param morphName Name of morph to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String morphName)
	{
		// loop throught our morphs and see if we find a match
		for (Morph m : morphList)
		{
			if (m.getName().equalsIgnoreCase(morphName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the predefined morphs to see if one exists with the given name. Will do a partial (startsWith) search
	 * @param morphName Name of morph to search for
	 * @return True/False as appropriate
	 */
	public static boolean existsPartial(String morphName)
	{
		// loop throught our morphs and see if we find a match
		for (Morph t : morphList)
		{
			if (t.getName().startsWith(morphName))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns whether name is a valid morph
	 * @param name String containing name of a Morph
	 * @return True/False as appropriate
	 */
	public boolean itemExists(String name)
	{
		return Morph.exists(name);
	}
	
	/**
	 * Gets a copy of a morph from the predefined list (the level will be mutable, possibly more in the future)
	 * 
	 * @param morphName Name of morph to search for
	 * @return
	 */
	public static Morph createMorph(String morphName)
	{
		for (Morph t : morphList)
		{
			if (t.getName().equalsIgnoreCase(morphName))
			{
				Morph result = new Morph(t);
				return result;
			}
		}
		
		return null;
	}
	
	public String toString()
	{
		return this.toStringShort() + " : " + this.description + ";" + this.effects;
	}
	
	public String toStringShort()
	{
		return this.name;
	}

	/**
	 * Creates a new Morph that is stored statically in the class.
	 * 
	 * isSynth is a true/false field
	 * @param input String[] {name, morphType, description, implants, aptitudeMaxStr, durability, woundThreshold, CP, creditCost, effects, notes}
	 */
	public static void CreateInternalMorph(String[] parts)
	{
		if (parts.length != 11 || !Utils.isInteger(parts[5]) || !Utils.isInteger(parts[6]) || !Utils.isInteger(parts[7]))
		{
			throw new IllegalArgumentException("Invalidly formatted Morph string[] : " + Utils.joinStr(parts,","));
		}
		
		int cnt = 0;
		String name = parts[cnt++];
		MorphType morphType = EnumFactory.getEnum(MorphType.class, parts[cnt++]);
		String description = parts[cnt++];
		String implants = parts[cnt++];
		String aptitudeMaxStr = parts[cnt++];
		int durability = Integer.parseInt(parts[cnt++]);
		int woundThreshold = Integer.parseInt(parts[cnt++]);
		int CP = Integer.parseInt(parts[cnt++]);
		String creditCost = parts[cnt++];
		String effects = parts[cnt++];
		String notes = parts[cnt++];
		
		Morph temp = new Morph(name,morphType,description,implants,aptitudeMaxStr,durability,woundThreshold,CP,creditCost,effects,notes);
		Morph.morphList.add(temp);
	}
	
	/**
	 * What family of morphs the morph is from: Biomorph, Synth, Pod, Infomorph
	 * @author Vigilant
	 */
	public enum MorphType
	{
		BIOMORPH, INFOMORPH, SYNTH, POD;
	}
	
	
	public String toXML()
	{
		Element root = new Element("morph");
		Document doc = new Document(root);
		
		doc.getRootElement().addContent(new Element("name").setText( getName() ));
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setOmitDeclaration(true));
		
		return xmlOut.outputString(doc);	
	}
	
	public static Morph fromXML(String xml)
	{
		Document document = Utils.getXMLDoc(xml);
		Element root = document.getRootElement();
		
		Utils.verifyTag(root, "morph");
		Utils.verifyChildren(root, new String[]{"name"});
		
		String nameStr = root.getChildText("name");
		
		if (!exists(nameStr))
		{
			throw new IllegalArgumentException("Morph called for name : " + nameStr + ", but no such Morph exists!");
		}
		
		return Morph.createMorph(nameStr); 
	}
	
}
