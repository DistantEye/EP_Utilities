package com.github.distanteye.ep_utils.containers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Represents entirety of a character in Eclipse Phase, holding Aptitude, gear, skills, sleights,etc
 * Has methods for aggregating and updating this data as driven by UI or Generator classes 
 * 
 * All String instance variables are stored in BaseCharacter.otherVars, although accessors and mutators exist as shortcuts
 * 
 * @author Vigilant
 */
public class EpCharacter extends SkilledCharacter {

	// some values constant to all Characters
	public static HashMap<String,String> charConstants = new HashMap<String,String>();
	public static String[] SECONDARY_STATS = {"DUR","WT","DR","LUC","TT","IR","INIT","SPD","DB"};
	
	private AspectHashMap<Trait> traits;
	private AspectHashMap<Rep> reps;
	private AspectHashMap<Sleight> sleights;
	
	private ArrayList<String> gearList;
	private LinkedList<String> allBackgrounds;
	private Morph currentMorph;
	private String statusText; // for some forms of generators
	
	/**
	 * @param name Character name
	 * @param autoApplyMastery Whether Skills will start to receive half gains automatically after level > Skill.EXPENSIVE_LEVEL
	 */
	public EpCharacter(String name, boolean autoApplyMastery) 
	{
		super(name,autoApplyMastery);
		
		traits = new AspectHashMap<Trait>(", ",false);
		
		gearList = new ArrayList<String>();
		
		// We use Aptitudes, a more EP-Tailored Primary Stat
		for (String stat : Aptitude.TYPES)
		{
			stats.put(stat, new Aptitude(stat,0));	
		}
		
		// now do it for MOX and the rest of the derived stats
		// all stats other than mox,INIT,Speed reflect the user's bonus to that category, since the rest are calculated stats
		for (String stat : SECONDARY_STATS)
		{
			stats.put(stat, new Stat(stat,0));	
		}
		stats.setImmutable();
	
		// build the order we need to print as for Stats
		ArrayList<String> tempOrder = new ArrayList<String>(Arrays.asList(Aptitude.TYPES));
		tempOrder.addAll(Arrays.asList(SECONDARY_STATS));
		stats.setOrder(tempOrder);
		
		reps = new AspectHashMap<Rep>("\n",false);
		
		// gather all valid Rep categories from Rep class and add them at 0
		for (String repKey : Rep.repTypes.keySet())
		{
			reps.put(repKey,Rep.getCopyOf(repKey));
		}
		
		sleights = new AspectHashMap<Sleight>(", ",false);
		
		this.setVar("_MOX", "1");
		this.setVar("_credits", "0");
		this.setVar("_creditsSpent", "0");
		this.setVar("_faction", "");
		this.setVar("_background", "");
		this.setVar("_stress", "0");
		this.setVar("_CP", "0");
		this.setVar("_path", "");
		this.setVar("_isSynth", "0");
		allBackgrounds = new LinkedList<String>();
	}
	
	/**
	 * Calculates and updates derived secondary stats based on primary values,
	 * as well as the option to determine how much CP the character has used
	 */
	public void calc()
	{
		// don't try and calc if we don't have a morph yet
		if (getCurrentMorph() == null)
		{
			return;
		}
		
		int speedBon = 0;
		
		if (this.hasVar("_speedBonus"))
		{
			Integer.parseInt(this.getVar("_speedBonus"));
		}
		
		stats.get("SPD").setValue(1+speedBon);
		
		// for readability as well as factoring in bonuses we prefetch Aptitudes and set them to variables here
		int INT = stats().get("INT").getValue()+getVarInt("bonusINT");
		int REF = stats().get("REF").getValue()+getVarInt("bonusREF");
		int SOM = stats().get("SOM").getValue()+getVarInt("bonusSOM");
		int WIL = stats().get("WIL").getValue()+getVarInt("bonusWIL");
		
		// Infomorphs don't have physical damage stats 
		if (currentMorph.getMorphType()!=Morph.MorphType.INFOMORPH)
		{
			stats.get("DUR").setValue(currentMorph.getDurability());
			stats.get("WT").setValue(currentMorph.getWoundThreshold());
			int dr = currentMorph.getDurability();
			if (currentMorph.getMorphType()==Morph.MorphType.SYNTH)
			{
				stats.get("DR").setValue(dr*2);
			}
			else
			{
				stats.get("DR").setValue((int)Math.round(dr*1.5));
			}
			
			stats.get("DB").setValue(SOM/10);
		}
		else
		{
			stats.get("DUR").setValue(0);
			stats.get("WT").setValue(0);
			stats.get("DR").setValue(0);
			stats.get("DB").setValue(0);
		}
		
		stats.get("LUC").setValue(WIL*2);
		stats.get("TT").setValue(Math.round(stats.get("LUC").getValue()/5));
		stats.get("IR").setValue(stats.get("LUC").getValue()*2);
		stats.get("INIT").setValue(Math.round( (INT+REF) * 2 ) / 5 );
		
		// calculate CP used if applicable mode
		if (hasVar("_cpCalc"))
		{
			int cpUsed;
			int mox, totalRep,totalApt,numSleights,numSpec,activeSkillPoints,knowledgeSkillPoints,totalCredits;
			
			mox = stats.get("MOX").getValue();
			totalRep = 0;
			totalApt = 0;
			numSleights = 0;
			numSpec = 0;
			activeSkillPoints = 0;
			knowledgeSkillPoints = 0;
			totalCredits = this.getVarInt("_credits");
			
			// repCount			
			for (Rep r : reps.values())
			{
				totalRep = r.getValue();
			}
			
			for (Stat apt : stats.values())
			{
				totalApt += apt.getValue();
			}
			
			// sleights we just need a simple count
			numSleights = sleights.size();
			
			// count skills that have specializations 
			for (Skill skl : skills.values())
			{
				if (skl.getSpecialization().length() > 0)
				{
					numSpec++;
				}
			}
			
			// figure out skillPoint stuff : note, we use getSkills because it already factors in aptitude values
			for (String[] arr : getSkills(null))
			{				
				int sklVal = Integer.parseInt(arr[1]);
				
				if (sklVal > 60)
				{
					int remainder = sklVal-60;
					sklVal += remainder; // the effect is to double remainder to reflect the double cost when past 60
				}
				
				// sanity check, should always be true
				if (hasSkill(arr[0]))
				{
					Skill tmp = skills.get(arr[0]);
					
					// the aptitude isn't part of the cost
					sklVal -= stats.get(tmp.getLinkedApt()).getValue();
					
					if (tmp.isKnowledge())
					{
						if (arr[0].equalsIgnoreCase(getVarSF("NatLang")))
						{
							sklVal -= getIntConst("FREE_NAT_LANG"); // don't count the free aspect of this skill
						}
						
						knowledgeSkillPoints += sklVal;
					}
					else
					{
						activeSkillPoints += sklVal;
					}
				}
				else
				{
					throw new IllegalArgumentException("Skill output from getSkills() wasn't found in skillList (" 
								+ arr[0] +") this should not happen.");
				}
			}					
			
			// we adjust some values by their free amounts, making sure they never are negative
			mox = Math.max(0, 		mox - getIntConst("FREE_MOX"));			
			totalRep = Math.max(0,	totalRep - getIntConst("FREE_REP"));
			totalApt = Math.max(0, 	totalApt - getIntConst("FREE_APT"));
			numSleights = 0;
			numSpec = 0;
			activeSkillPoints = 0;
			knowledgeSkillPoints = 0;
			totalCredits = Math.max(0, 	totalCredits - getIntConst("FREE_CREDIT"));
			
			cpUsed = 15*mox + 10*totalApt + 5*numSleights + 5*numSpec + activeSkillPoints + knowledgeSkillPoints + totalRep/10 + totalCredits/1000;
			
			// set to relevant character variable
			this.setVar("_cpUsed", ""+cpUsed); 
					
		}
	}
	
	/**
	 * Attempts to retrieve integer constant from charConstants 
	 * @param name Name of constant
	 * @return Valid integer constant tied to name
	 */
	public static int getIntConst(String name)
	{
		if (charConstants.containsKey(name))
		{
			String val = charConstants.get(name);
			
			if (Utils.isInteger(val))
			{
				return Integer.parseInt(val);
			}
			else
			{
				throw new IllegalArgumentException("Character Constant : " + name + " does not exist!");
			}
		}
		else
		{
			throw new IllegalArgumentException("Character Constant : " + name + " does not exist!");
		}
	}
	
	public String toString()
	{
		String result = "Name: " + this.getName() + ", Age(" + this.getAge() + ")"+ "\n";
		result += "Morph : " + this.getMorphName() + ", Faction : " + this.getFaction()  + ", Path : " + this.getPath() 
					+ ", Background : " + this.getBackground() +"\n";
		
		String statString = this.stats.toString().replace(SECONDARY_STATS[0], "\n" + SECONDARY_STATS[0]) + "\n"; // quick modification to split to two lines
		String skillString = this.getSkillsString() + "\n"; 
		
		// replace the stats/skills strings with more appropriate values if bonuses are availible to be factored in
		if (hasBonusStats())
		{
			HashMap<String,Integer> bonuses = getBonusStats();
			statString = this.stats.toString(bonuses).replace(SECONDARY_STATS[0], "\n" + SECONDARY_STATS[0]) + "\n"; // quick modification to split to two lines
			skillString = this.getSkillsString(bonuses) + "\n"; 
		}
		
		result += "Traits : " + this.traits.toString() + "\n";
		result += "Sleights : " + this.sleights.toString() + "\n";		
		result += statString;
		result += "MOX: " + getMox() + ", Credits: " + getCredits() + "\n";
		result += skillString;
		result += this.reps.toString() + "\n";		
		result += "Gear : " + this.getGearString();		
		
		return result;
	}
	
	/**
	 * Increases the current value of a particular Rep category
	 * 
	 * @param repName Rep category to look for
	 * @param val int value to add to the Rep category specified. If doing so would bring rep below 0 or above 100, result is capped to these values
	 * @return
	 */
	public void incRepValue(String repName, int val)
	{
		if (!Rep.exists(repName))
		{
			throw new IllegalArgumentException(repName + " is not a valid Rep category!");
		}
		
		Rep charRep = this.reps.get(repName);
		charRep.incValue(val);
		calc();
	}
	
	/**
	 * Returns any available Morph Name, or "" if none exist
	 * 
	 * Silent fail as "" done to be more friendly to UI as this can often
	 * be blank at start and filled in later
	 * @return String either equal to the character's Morph's Name or ""
	 */
	public String getMorphName()
	{
		if (getCurrentMorph() != null)
		{
			return getCurrentMorph().getName();
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Returns any available _factionName, or "" if none exist
	 * 
	 * Silent fail as "" done to be more friendly to UI as this can often
	 * be blank at start and filled in later
	 * 
	 * @return String either equal to _factionName or ""
	 */
	public String getFaction()
	{
		if (hasVar("_factionName"))
		{
			return getVar("_factionName");
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Returns any availible _pathName, or "" if none exist
	 * 
	 * Silent fail as "" done to be more friendly to UI as this can often
	 * be blank at start and filled in later
	 * 
	 * @return String either equal to _pathName or ""
	 */
	public String getPath()
	{
		if (hasVar("_pathName"))
		{
			return getVar("_pathName");
		}
		else
		{
			return "";
		}
	}
	
	public int getCredits() 
	{
		return Integer.parseInt(this.getVar("_credits"));
	}

	/**
	 * Set credits. Leaving negative values as a possibility in case someone wants to model debt this way
	 * 
	 * @param credits the credits to set
	 */
	public void setCredits(int credits) 
	{
		this.setVar("_credits",String.valueOf(credits));
		calc();
	}
	
	/**
	 * Adds val to credits. Leaving negative values as a possibility in case someone wants to model debt this way
	 * 
	 * @param val Value to add to credits. Can be positive or negative
	 */
	public void incCredits(int val) 
	{
		this.incVar("_credits",val);
		calc();
	}
	
	/**
	 * If a valid aptitude name is provided, will add the value provided to it
	 * @param apt Aptitude name
	 * @param value Integer value between 1 and APTITUDE MAX
	 */
	public void incAptitude(String apt, int value)
	{
		if (!Aptitude.exists(apt))
		{
			throw new IllegalArgumentException(apt + " is not a valid Aptitude");
		}
		
		
		this.stats.get(apt).addValue(value);
		// stats will trigger calc on its own
	}
	
	/**
	 * Moxie accessor method
	 * @return int value of character's current moxie stat (max, not current amount)
	 */
	public int getMox()
	{
		return this.getVarInt("_MOX");
	}
	
	/**
	 * Moxie mutator method
	 * @param val positive int value to set player's moxie stat to (max, not current amount)
	 */
	public void setMox(int val)
	{
		if (val < 1)
		{
			throw new IllegalArgumentException("MOX value must be positive");
		}
		
		this.setVar("_MOX", ""+val);
		calc();
	}
	
	/**
	 * Adds the given value to the players Moxie stat
	 * @param val int value to add to the player's moxie stat to (max, not current amount)
	 */
	public void incMox(int val)
	{
		int mox = this.getVarInt("_MOX");
		if (mox + val < 1)
		{
			throw new IllegalArgumentException("MOX value must be positive");
		}
		
		this.setMox(mox+val);
		// setMox will trigger calc()
	}
	
	/**
	 * Returns final adjusted value for a skill, factoring in base aptitude bonus and mastery adjustments, and skill cap
	 * Natural Language is returned at full value
	 * 
	 * @param skl Valid skill object
	 * @return Adjusted value/skill points for skill
	 */
	public int getFinalSklVal(Skill skl)
	{
		String linkedApt = skl.getLinkedApt();
		int aptValue = stats().get(linkedApt).getValue();
		
		int result = 0;

		// The character's Natural Language doesn't play by same rules in regards to advancement over 60
		if (hasVar("NatLang") && skl.getFullName().equalsIgnoreCase(getVar("NatLang")))
		{
			result = skl.getValue()+aptValue;
		}
		else
		{
			result = Skill.skillAdjustExpensiveCap(skl.getValue()+aptValue);
		}
		
		if (result > Skill.LEVEL_CAP)
		{
			result = Skill.LEVEL_CAP;
		}
		
		return result;
	}
	

	public String getBackground() 
	{
		if (hasVar("_background"))
		{
			return this.getVar("_background");
		}
		else
		{
			return "";
		}
	}

	public void setBackground(String background) 
	{
		this.setVar("_background", background);
		this.allBackgrounds.addFirst(background);
	}

	/**
	 * Checks the history of the characters backgrounds and returns whether the string passed in is one of them
	 * @param background The background to check for
	 * @return True/False as appropriate
	 */
	public boolean hasHadBackground(String background)
	{
		return this.allBackgrounds.contains(background);
	}
	
	public Morph getCurrentMorph() 
	{
		return currentMorph;
	}

	public void setCurrentMorph(Morph currentMorph) 
	{
		this.currentMorph = currentMorph;
		calc();
	}

	public ArrayList<String> getGearList() 
	{
		return gearList;
	}
	
	public void addGear(String gear)
	{
		gearList.add(gear);
	}
	
	/**
	 * Remove item from gear list, throwing an error if it doesn't exist
	 * @param gear Valid equipment name of an item that exists
	 * @return The item removed
	 */
	public String removeGear(String gear)
	{
		for (String item : gearList)
		{
			if (item.equalsIgnoreCase(gear))
			{
				String temp = item;
				gearList.remove(item);
				return temp;
			}
		}
		
		throw new IllegalArgumentException("Character lacks gear : " + gear);
	}
	
	public ArrayList<Rep> getAllRep()
	{
		ArrayList<Rep> result = new ArrayList<Rep>();
		result.addAll(reps.values());
		
		return result;
	}
	
	public String getGearString()
	{
		String result = "";

		for (String gear : gearList)
		{
			result += gear + " ";
		}
		
		return result.trim();
	}
	
	// Sub containers : these give access to character aspects big enough for their own class 

	public AspectHashMap<Trait> traits() {
		return traits;
	}

	public AspectHashMap<Rep> reps() { 
		return reps;
	}

	public AspectHashMap<Sleight> sleights() {
		return sleights;
	}
	
	//end sub-containers
	
	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
	public void appendStatusText(String text)
	{
		this.statusText += "\n" + text;
	}

	/**
	 * Collects the character's data into a set of XML tags, not enclosed in a greater tag,
	 * this less subclasses redefine saving while still being able to draw off the superclass
	 * @return Returns a String containing the character's vital data in a list of XML tags
	 */
	protected String getInnerXML()
	{
		StringWriter result = new StringWriter();
		Element root = new Element("Character");
		Document doc = new Document(root);
		
		Element elemTraits = new Element("traits");
		for (Trait t : traits.values())
		{
			Element e = Utils.getRootElement(t.toXML());
			elemTraits.addContent( e );
		}
		doc.getRootElement().addContent(elemTraits);
		
		Element elemReps = new Element("reps");
		for (Rep r : reps.values())
		{
			Element e = Utils.getRootElement(r.toXML());
			elemReps.addContent( e );
		}
		doc.getRootElement().addContent(elemReps);
		
		
		Element elemSleights = new Element("sleights");
		for (Sleight s : sleights.values())
		{
			Element e = Utils.getRootElement(s.toXML());
			elemSleights.addContent( e );
		}
		doc.getRootElement().addContent(elemSleights);
		
		Element elemGearList = new Element("gearList");
		for (String gear : gearList)
		{
			elemGearList.addContent( new Element("gear").setText( gear ) );
		}
		doc.getRootElement().addContent(elemGearList);
		
		Element elemAllBackgrounds = new Element("allBackgrounds");
		for (String background : allBackgrounds)
		{
			elemGearList.addContent( new Element("background").setText( background ) );
		}
		doc.getRootElement().addContent(elemAllBackgrounds);
		
		Element elemCharMorph = new Element("character_morph");
		if (currentMorph != null)
		{
			elemCharMorph.addContent( Utils.getRootElement( currentMorph.toXML() ));
		}
		doc.getRootElement().addContent(elemCharMorph);

		doc.getRootElement().addContent(new Element("statusText").setText( this.getStatusText() ));
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setOmitDeclaration(true));
		
		
		try {
			xmlOut.outputElementContent(root, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return super.getInnerXML() + "\n" + result.toString();
		
	}
	
	/**
	 * Discards the character's current data and replaces it with the data encoded into the xml string passed
	 * @param xml a validly formatted XML string as returned by getXML()
	 */
	public void loadXML(String xml)
	{
		super.loadXML(xml);
		
		// We use Aptitudes, a more EP-Tailored Primary Stat
		// We have to replace the applicable entries from SkilledCharacter with those
		// unfortunately because of setImmutable() we also have to recreate the whole thing
		StatHashMap statsTemp = new StatHashMap(" ",false);

		for (String key : stats.keySet())
		{
			Stat temp = stats.get(key);

			if (ArrayUtils.contains(Aptitude.TYPES, key))
			{
				statsTemp.put(key, new Aptitude(temp.getName(),temp.getValue()));
			}
			else
			{
				statsTemp.put(key, temp);
			}
		}
		stats = statsTemp; // our new aptitude containing array replaces the old stat-only one
		
		// build the order we need to print as for Stats
		ArrayList<String> tempOrder = new ArrayList<String>(Arrays.asList(Aptitude.TYPES));
		tempOrder.addAll(Arrays.asList(SECONDARY_STATS));
		stats.setOrder(tempOrder);
		
		Document document = Utils.getXMLDoc(xml);
		Element root = document.getRootElement();
		
		Utils.verifyTag(root, "Character");
		Utils.verifyChildren(root, new String[]{"traits","reps","sleights","gearList","allBackgrounds","character_morph"});
		
		// rebuild Traits
		Element elemTraits = root.getChild("traits");
		for (Element e : elemTraits.getChildren())
		{
			Trait tmp = Trait.fromXML( Utils.elemToString(e) );
			traits.put(tmp.getName(), tmp);
		}
		
		// rebuild Reps
		Element elemReps = root.getChild("reps");
		for (Element e : elemReps.getChildren())
		{
			Rep tmp = Rep.fromXML( Utils.elemToString(e) );
			reps.put(tmp.getName(), tmp);
		}
		
		// rebuild Sleights
		Element elemSleights = root.getChild("sleights");
		for (Element e : elemSleights.getChildren())
		{
			Sleight tmp = Sleight.fromXML( Utils.elemToString(e) );
			sleights.put(tmp.getName(), tmp);
		}
		
		// rebuild Gearlist
		Element elemGearList = root.getChild("gearList");
		for (Element e : elemGearList.getChildren())
		{
			gearList.add( e.getText() );
		}
		
		// rebuild AllBackgrounds
		Element elemBackgrounds = root.getChild("allBackgrounds");
		for (Element e : elemBackgrounds.getChildren())
		{
			allBackgrounds.addFirst( e.getText() );
		}
		
		// set morph (if applicable)
		Element elemMorph = root.getChild("character_morph");
		Element innerMorph = elemMorph.getChild("morph");
		if (innerMorph != null && innerMorph.getChildText("name").length() > 0)
		{
			setCurrentMorph(Morph.fromXML( Utils.elemToString(innerMorph)) );
		}
		
		// set status text
		this.setStatusText(root.getChildText("statusText"));
		
	}
	
	/**
	 * Discards character's current data and sets everything to default values
	 */
	public void setToDefaults()
	{
		super.setToDefaults();
		
		// zero out all current variables to their default state
		this.currentMorph = null;
		traits = new AspectHashMap<Trait>(", ",false);
		
		gearList = new ArrayList<String>();
		
		// We use Aptitudes, a more EP-Tailored Primary Stat
		for (String stat : Aptitude.TYPES)
		{
			stats.put(stat, new Aptitude(stat,0));	
		}
		
		// now do it for MOX and the rest of the derived stats
		// all stats other than mox,INIT,Speed reflect the user's bonus to that category, since the rest are calculated stats
		for (String stat : SECONDARY_STATS)
		{
			stats.put(stat, new Stat(stat,0));	
		}
		stats.setImmutable();
	
		// build the order we need to print as for Stats
		ArrayList<String> tempOrder = new ArrayList<String>(Arrays.asList(Aptitude.TYPES));
		tempOrder.addAll(Arrays.asList(SECONDARY_STATS));
		stats.setOrder(tempOrder);
		
		reps = new AspectHashMap<Rep>("\n",false);
		
		// gather all valid Rep categories from Rep class and add them at 0
		for (String repKey : Rep.repTypes.keySet())
		{
			reps.put(repKey,Rep.getCopyOf(repKey));
		}
		
		sleights = new AspectHashMap<Sleight>(", ",false);
		
		this.setVar("_MOX", "1");
		this.setVar("_credits", "0");
		this.setVar("_creditsSpent", "0");
		this.setVar("_faction", "");
		this.setVar("_background", "");
		this.setVar("_stress", "0");
		this.setVar("_CP", "0");
		this.setVar("_path", "");
		this.setVar("_isSynth", "0");
		allBackgrounds = new LinkedList<String>();

		this.setStatusText("");
	}
	
		
}