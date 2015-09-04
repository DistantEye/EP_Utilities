package com.github.distanteye.ep_utils.containers;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.github.distanteye.ep_utils.core.Step;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Represents entirety of a character in Eclipse Phase, holding Aptitude, gear, skills, sleights,etc
 * Has methods for aggregating and updating this data as driven by UI or Generator classes 
 * 
 * @author Vigilant
 */
public class EpCharacter {

	// some values constant to all Characters
	public static HashMap<String,String> charConstants = new HashMap<String,String>();
	public static final String[] secStats = {"DUR","WT","DR","LUC","TT","IR","INIT","SPD","DB"};
	
	private HashMap<String,Skill> skills; // Skills are too tightly coupled to Character's state to be useful as AspectHashMap
	private AspectHashMap<Aptitude> aptitudes;
	private AspectHashMap<Trait> traits;
	private AspectHashMap<Rep> reps;
	private AspectHashMap<Sleight> sleights;
	
	private AspectHashMap<Integer> nonAppStats;
	private HashMap<String,String> otherVars;
	private ArrayList<String> gearList;
	private String name;
	private int age;
	private LinkedList<String> allBackgrounds;
	private Morph currentMorph;
	
	private LinkedList<Integer> lastRolls;
	private String currentTable;
	private Step lastStep;
	private ArrayList<String[]> packages; // stores pkgs added to character
	private boolean autoApplyMastery;
	
	/**
	 * Returns the current Table name the player is rolling on (if LifePath generation)
	 * @return String name of current table
	 */
	public String getCurrentTable() 
	{
		return currentTable;
	}

	public void setCurrentTable(String currentTable) 
	{
		if (!this.currentTable.equals(currentTable))
		{
			lastRolls = new LinkedList<Integer>();
		}
			
		this.currentTable = currentTable;
	}

	/**
	 * Whenever character is going through some kind of character gen, 
	 * knowing what they rolled last may be useful. Returns that value
	 * 
	 * @return Integer for the last value rolled
	 */
	public int getLastRoll() 
	{
		return lastRolls.getLast();
	}

	/**
	 * Checks whether a roll is in the stack of last rolls at all
	 * @param val Int to check the rolls for
	 * @return True if present in collection, false otherwise
	 */
	public boolean rollsContain(int val)
	{
		return lastRolls.contains(val);
	}
	
	/**
	 * Pushes a value onto the stack of last rolls
	 * @param lastRoll int value
	 */
	public void addLastRoll(int lastRoll) 
	{
		this.lastRolls.push(lastRoll);
	}

	/**
	 * @param name Character name
	 * @param autoApplyMastery Whether Skills will start to receive half gains automatically after level > Skill.EXPENSIVE_LEVEL
	 */
	public EpCharacter(String name, boolean autoApplyMastery) 
	{
		this.name = name;
		this.autoApplyMastery = autoApplyMastery;
		skills = new HashMap<String, Skill>();
		aptitudes = new AspectHashMap<Aptitude>(" ",false);
		traits = new AspectHashMap<Trait>(", ",false);
		
		gearList = new ArrayList<String>();
		nonAppStats = new AspectHashMap<Integer>(" ", true);
		otherVars = new HashMap<String,String>();
	
		age = -1; // placeholder
		
		// set up placeholder values for aptitudes
		for (String stat : Aptitude.aptitudes)
		{
			aptitudes.put(stat, new Aptitude(stat,0));	
		}
		aptitudes.setImmutable();
		
		// do it for MOX and the rest of the derived stats
		// all stats other than mox,INIT,Speed reflect the user's bonus to that category, since the rest are calculated stats
		for (String stat : secStats)
		{
			nonAppStats.put(stat, 0);	
		}
		nonAppStats.put("MOX", 1); // these two
		nonAppStats.setImmutable();
	
		
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
		
		currentTable = "";
		lastRolls = new LinkedList<Integer>();
		allBackgrounds = new LinkedList<String>();
		packages = new ArrayList<String[]>();
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
		
		nonAppStats.put("SPD", 1+speedBon);
		
		// Infomorphs don't have physical damage stats 
		if (currentMorph.getMorphType()!=Morph.MorphType.INFOMORPH)
		{
			nonAppStats.put("DUR", currentMorph.getDurability());
			nonAppStats.put("WT", currentMorph.getWoundThreshold());
			int dr = currentMorph.getDurability();
			if (currentMorph.getMorphType()==Morph.MorphType.SYNTH)
			{
				nonAppStats.put("DR", dr*2);
			}
			else
			{
				nonAppStats.put("DR", (int)Math.round(dr*1.5));
			}
			
			nonAppStats.put("DB", aptitudes().get("SOM").getValue()/10);
		}
		else
		{
			nonAppStats.put("DUR", 0);
			nonAppStats.put("WT", 0);
			nonAppStats.put("DR", 0);
			nonAppStats.put("DB", 0);
		}
		
		nonAppStats.put("LUC", aptitudes().get("WIL").getValue()*2);
		nonAppStats.put("TT", (int)Math.round(nonAppStats.get("LUC")/5));
		nonAppStats.put("IR", nonAppStats.get("LUC")*2);
		nonAppStats.put("INIT", (int)Math.round( ( (aptitudes().get("INT").getValue()+aptitudes().get("REF").getValue())) * 2 ) / 5 );
		
		// calculate CP used if applicable mode
		if (hasVar("{cpCalc}"))
		{
			int cpUsed;
			int mox, totalRep,totalApt,numSleights,numSpec,activeSkillPoints,knowledgeSkillPoints,totalCredits;
			
			mox = nonAppStats.get("MOX");
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
			
			for (Aptitude apt : aptitudes.values())
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
					sklVal -= aptitudes.get(tmp.getLinkedApt()).getValue();
					
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
		String result = this.name + "(" + this.age + ")"+ "\n";
		result = "Morph : " + this.getMorphName() + ", Faction : " + this.getFaction()  + ", Path : " + this.getPath() 
					+ ", Background : " + this.getBackground() +"\n";
		
		result += "Traits : " + this.traits.toString() + "\n";
		result += "Sleights : " + this.sleights.toString() + "\n";
		result += this.aptitudes.toString() + "\n";
		result += this.nonAppStats.toString() + "\n";
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
		
		
		this.aptitudes.get(apt).addValue(value);
	}
	
	/**
	 * Moxie accessor method
	 * @return int value of character's current moxie stat (max, not current amount)
	 */
	public int getMox()
	{
		return this.nonAppStats.get("MOX");
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
		
		this.nonAppStats.put("MOX", val);
	}
	
	/**
	 * Adds the given value to the players Moxie stat
	 * @param val int value to add to the player's moxie stat to (max, not current amount)
	 */
	public void incMox(int val)
	{
		if (this.nonAppStats.get("MOX") + val < 1)
		{
			throw new IllegalArgumentException("MOX value must be positive");
		}
		
		this.nonAppStats.put("MOX", this.nonAppStats.get("MOX") + val);
	}
	
	
	
	/**
	 * Either adds the skill to the character (if it doesn't exist), or increments the skill level
	 * of the skill that's already there, based on the value of the skill passed in
	 * @param skill Valid Skill object with a meaningful Skill level value
	 */
	public void addSkill(Skill skill)
	{
		if (this.skills.containsKey(skill.getFullName()) )
		{
			this.skills.get(skill.getFullName()).addValue(skill.getValue(), true);						
		}
		else
		{
			this.skills.put(skill.getFullName(), skill);
			this.setVar("{newestSkill}", skill.getFullName());
		}
	}
	
	/**
	 * Removes a skill from the character
	 * 
	 * @param skillName Name of the skill the character has
	 * @return True if successful, false if skill could not be found
	 */
	public boolean removeSkill(String skillName) 
	{
		if (this.skills.containsKey(skillName))
		{
			Skill temp = this.skills.remove(skillName);
			this.setVar("{lastRemSkl}", temp.getFullName());
			this.setVar("{lastRemSklVal}", ""+temp.getValue());
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Adds/subtracts levels from a skill that already exists
	 * 
	 * @param skillName Name of the skill the character has
	 * @param amount Amount to add to the skill, can be negative
	 * @return True if successful, false if skill could not be found or created
	 */
	public boolean incSkill(String skillName, int amount) 
	{
		if (this.skills.containsKey(skillName))
		{
			this.skills.get(skillName).addValue(amount, true);
			return true;
		}
		else if (amount > 0)
		{
			this.addSkill(Skill.CreateSkill(skillName, amount));
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Sets a existing skill to a certain level, or creates it if it doesn't exist
	 * 
	 * @param skillName Name of the skill the character has
	 * @param amount Level to set the skill to
	 * @return True if successful, false if skill could not be found
	 */
	public boolean setSkill(String skillName, int amount) 
	{
		if (this.skills.containsKey(skillName))
		{
			this.skills.get(skillName).setValue(amount);
			return true;
		}
		else if (Skill.isSkill(skillName))
		{
			Skill tempSkl = Skill.CreateSkill(skillName, amount);
			this.skills.put(tempSkl.getFullName(), tempSkl);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Adds specialization to a skill that already exists
	 * 
	 * @param skillName Name of the skill the character has
	 * @param specialization Amount to add to the skill, can be negative
	 * @return True if successful, false if skill could not be found
	 */
	public boolean addSkillSpec(String skillName, String specialization) 
	{
		if (this.skills.containsKey(skillName))
		{
			this.skills.get(skillName).setSpecialization(specialization);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns whether player has the specified skill
	 * @param skillName Skill to search for
	 * @return True/False as appropriate
	 */
	public boolean hasSkill(String skillName)
	{
		return this.skills.containsKey(skillName);
	}
	
	/**
	 * Returns final adjusted value for a skill, factoring in base aptitude bonus and mastery adjustments, and skill cap
	 * @param skl Valid skill object
	 * @return Adjusted value/skill points for skill
	 */
	public int getFinalSklVal(Skill skl)
	{
		String linkedApt = skl.getLinkedApt();
		int aptValue = aptitudes().get(linkedApt).getValue();
		
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
	
	/**
	 * Takes all the character's skills and returns a list of String[]
	 * in the form of {skillName,value} . skillName will include the specialization if applicable
	 * Character's base aptitude values will be factored into the calculation
	 * 
	 * @return ArrayList of strings representing character skill values
	 */
	public ArrayList<String[]> getSkills()
	{
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		for (Skill skill : skills.values())
		{
			String[] temp = {skill.getFullName(), ""+getFinalSklVal(skill)};
			result.add(temp);
		}
		
		return result;
	}
	
	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public int getAge() 
	{
		return age;
	}

	public void setAge(int age) 
	{
		this.age = age;
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
	
	public String getRandSkill(SecureRandom rng)
	{
		int idx = rng.nextInt(skills.size());
		return ((Skill)skills.values().toArray()[idx]).getFullName();
	}
	
	public int getNumSkills()
	{
		return this.skills.size();
	}
	
	public String getSkillsString()
	{
		String result = "";
		int cnt = 0;
		boolean first = true;
		
		
		for (String[] skl : this.getSkills())
		{
			String separator = ",  ";
			
			if (first)
			{
				separator = "";
				first = false;
			}
			
			if (cnt >= 9)
			{
				separator = "\n";
				cnt = 0;
			}
			
			result += separator + skl[0] + " " + skl[1];
			cnt++;
		}
		
		return result.trim();
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
	
	/**
	 * Most generalized variable store. Sets a key,value pair for storage by the character
	 * @param name Name of the variable
	 * @param val Value of the variable
	 */
	public void setVar(String name, String val)
	{
		this.otherVars.put(name, val);
	}
	
	/**
	 * Overload for incVar that takes an integer second parameter (for convenience)
	 * @param name name of variable
	 * @param val Integer value
	 */
	public void incVar(String name, int val)
	{
		this.incVar(name, String.valueOf(val));
	}
	
	/**
	 * Most generalized variable store. Increments key,value pair for storage by the character
	 * will throw error if the variable passed or the value passed is not a number.
	 * Will create the variable if it doesn't exist, set to 0 (before val is added)
	 * 
	 * 
	 * @param name Name of the variable (must be numeric holding variable)
	 * @param val Value of the variable (must be numeric value in string)
	 */
	public void incVar(String name, String val)
	{
		if (!this.hasVar(name))
		{
			this.setVar(name, String.valueOf(0));
		}
		
		String var = this.getVar(name);
		
		if (!Utils.isInteger(var))
		{
			throw new IllegalArgumentException("incVar(" + name + "," + val +"): variable value " + var + " is not a number!");
		}
		if (!Utils.isInteger(val))
		{
			throw new IllegalArgumentException("incVar(" + name + "," + val +"): " + val + " is not a number!");
		}
		
		// otherwise proceed with increment
		int newVal = Integer.parseInt(var) + Integer.parseInt(val);
		this.setVar(name, String.valueOf(newVal));
	}
	
	/**
	 * Retrieves a variable from the general store. Does not throw error if it doesn't exist, returns 0 instead
	 * Will still throw error if the variable exists but is not a number
	 * @param name Name of variable to search for
	 * @return The matching value for name, or 0 if it doesn't exist. 
	 */
	public int getVarInt(String name)
	{
		if (this.hasVar(name))
		{
			if (Utils.isInteger(this.getVar(name)))
			{
				return Integer.parseInt(this.otherVars.get(name));
			}
			else
			{
				throw new IllegalArgumentException(name + " is a non-integer variable and can't be returned by getVarInt");
			}
		}
		else
		{
			return 0;
		}
	}	
	
	/**
	 * Retrieves a variable from the general store. 
	 * This version returns "" instead of throwing an error if no such variable exists
	 * @param name Name of variable to search for
	 * @return The matching value for name, or "" if none exists
	 */
	public String getVarSF(String name)
	{
		if (hasVar(name))
		{
			return getVar(name);
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Retrieves a variable from the general store
	 * @param name Name of variable to search for
	 * @return The matching value for name
	 * @throws IllegalArgumentException if no such variable exists
	 */
	public String getVar(String name)
	{
		if (this.hasVar(name))
		{
			return this.otherVars.get(name);
		}
		else
		{
			throw new IllegalArgumentException("getVar(" + name + "): No such variable exists in character: " + this.getName());
		}
	}
	
	/**
	 * Returns whether a variable exists in the store (or from the character special store)
	 * 
	 * Will return false if the variable exists but has no value (length 0 string)
	 * @param name Name/Key to search for
	 * @return True if exists with a non "" value, false otherwise
	 */
	public boolean hasVar(String name)
	{
		return this.otherVars.containsKey(name) && this.otherVars.get(name).length() != 0;
	}
	
	/**
	 * Removes a variable from the store and returns it, if it exists
	 * @param name Name/Key to remove
	 * @return The variable removed
	 * @throws IllegalArgumentException if nothing with that key exists
	 */
	public String removeVar(String name)
	{
		if (this.otherVars.containsKey(name))
		{
			return this.otherVars.remove(name);
		}
		else
		{
			throw new IllegalArgumentException("getVar(" + name + "): No such variable exists in character: " + this.getName());
		}
	}

	public Step getLastStep() {
		return lastStep;
	}

	public void setLastStep(Step lastStep) {
		this.lastStep = lastStep;
	}
	
	public boolean hasPackage(String name)
	{
		for (String[] info : this.packages)
		{
			if (info[0].equalsIgnoreCase(name))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	// Sub containers : these give access to character aspects big enough for their own class 
	
	public AspectHashMap<Aptitude> aptitudes() {
		return aptitudes;
	}

	public AspectHashMap<Trait> traits() {
		return traits;
	}

	public AspectHashMap<Rep> reps() { 
		return reps;
	}

	public AspectHashMap<Sleight> sleights() {
		return sleights;
	}

	public AspectHashMap<Integer> nonAppStats() {
		return nonAppStats;
	}
	
	//end sub-containers

	/**
	 * Adds package info to a character's list of added packages. This is mainly a log for purposes of keeping aware of duplications
	 * @param info Length 2 String[] of form {PackageName,PP}
	 */
	public void addPackage(String[] info)
	{
		this.packages.add(info);
	}

	public boolean isAutoApplyMastery() {
		return autoApplyMastery;
	}

	public void setAutoApplyMastery(boolean autoApplyMastery) {
		this.autoApplyMastery = autoApplyMastery;
	}
	
	
}
