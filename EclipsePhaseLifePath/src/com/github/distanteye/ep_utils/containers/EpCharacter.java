package com.github.distanteye.ep_utils.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

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
		stats.put("MOX",new Stat("MOX",1)); // Mox has a default value of 1
		stats.setImmutable();
	
		
		reps = new AspectHashMap<Rep>("\n",false);
		
		// gather all valid Rep categories from Rep class and add them at 0
		for (String repKey : Rep.repTypes.keySet())
		{
			reps.put(repKey,Rep.getCopyOf(repKey));
		}
		
		sleights = new AspectHashMap<Sleight>(", ",false);
		
		this.setVar("{credits}", "0");
		this.setVar("{creditsSpent}", "0");
		this.setVar("{faction}", "");
		this.setVar("{background}", "");
		this.setVar("{stress}", "0");
		this.setVar("{CR}", "0");
		this.setVar("{path}", "");
		this.setVar("{isSynth}", "0");
		allBackgrounds = new LinkedList<String>();
	}
	
	/**
	 * Calculates and updates derived secondary stats based on primary values,
	 * as well as the option to determine how much CP the character has used
	 */
	public void calcStats()
	{
		// don't try and calc if we don't have a morph yet
		if (getCurrentMorph() == null)
		{
			return;
		}
		
		int speedBon = 0;
		
		if (this.hasVar("{speedBonus}"))
		{
			Integer.parseInt(this.getVar("{speedBonus}"));
		}
		
		stats.get("SPD").setValue(1+speedBon);
		
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
			
			stats.get("DB").setValue(stats().get("SOM").getValue()/10);
		}
		else
		{
			stats.get("DUR").setValue(0);
			stats.get("WT").setValue(0);
			stats.get("DR").setValue(0);
			stats.get("DB").setValue(0);
		}
		
		stats.get("LUC").setValue(stats().get("WIL").getValue()*2);
		stats.get("TT").setValue((int)Math.round(stats.get("LUC").getValue()/5));
		stats.get("IR").setValue(stats.get("LUC").getValue()*2);
		stats.get("INIT").setValue((int)Math.round( ( (stats().get("INT").getValue()+stats().get("REF").getValue())) * 2 ) / 5 );
		
		// calculate CP used if applicable mode
		if (hasVar("{cpCalc}"))
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
			totalCredits = this.getVarInt("{credits}");
			
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
			for (String[] arr : getSkills())
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
			this.setVar("{cpUsed}", ""+cpUsed); 
					
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
		String result = this.getName() + "(" + this.getAge() + ")"+ "\n";
		result = "Morph : " + this.getMorphName() + ", Faction : " + this.getFaction()  + ", Path : " + this.getPath() 
					+ ", Background : " + this.getBackground() +"\n";
		
		result += "Traits : " + this.traits.toString() + "\n";
		result += "Sleights : " + this.sleights.toString() + "\n";
		result += this.stats.toString() + "\n";
		result += this.getSkillsString() + "\n";
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
	 * Returns any available {factionName}, or "" if none exist
	 * 
	 * Silent fail as "" done to be more friendly to UI as this can often
	 * be blank at start and filled in later
	 * 
	 * @return String either equal to {factionName} or ""
	 */
	public String getFaction()
	{
		if (hasVar("{factionName}"))
		{
			return getVar("{factionName}");
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Returns any availible {pathName}, or "" if none exist
	 * 
	 * Silent fail as "" done to be more friendly to UI as this can often
	 * be blank at start and filled in later
	 * 
	 * @return String either equal to {pathName} or ""
	 */
	public String getPath()
	{
		if (hasVar("{pathName}"))
		{
			return getVar("{pathName}");
		}
		else
		{
			return "";
		}
	}
	
	public int getCredits() 
	{
		return Integer.parseInt(this.getVar("{credits}"));
	}

	/**
	 * Set credits. Leaving negative values as a possibility in case someone wants to model debt this way
	 * 
	 * @param credits the credits to set
	 */
	public void setCredits(int credits) 
	{
		this.setVar("{credits}",String.valueOf(credits));
	}
	
	/**
	 * Adds val to credits. Leaving negative values as a possibility in case someone wants to model debt this way
	 * 
	 * @param val Value to add to credits. Can be positive or negative
	 */
	public void incCredits(int val) 
	{
		this.incVar("{credits}",val);
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
	}
	
	/**
	 * Moxie accessor method
	 * @return int value of character's current moxie stat (max, not current amount)
	 */
	public int getMox()
	{
		return this.stats.get("MOX").getValue();
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
		
		this.stats.get("MOX").setValue(val);
	}
	
	/**
	 * Adds the given value to the players Moxie stat
	 * @param val int value to add to the player's moxie stat to (max, not current amount)
	 */
	public void incMox(int val)
	{
		if (this.stats.get("MOX").getValue() + val < 1)
		{
			throw new IllegalArgumentException("MOX value must be positive");
		}
		
		this.stats.get("MOX").setValue(this.stats.get("MOX").getValue() + val);
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
		if (hasVar("{background}"))
		{
			return this.getVar("{background}");
		}
		else
		{
			return "";
		}
	}

	public void setBackground(String background) 
	{
		this.setVar("{background}", background);
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
	
}