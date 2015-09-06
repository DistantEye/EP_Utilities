package com.github.distanteye.ep_utils.containers;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.github.distanteye.ep_utils.core.Step;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Represents a BaseCharacter that has both Skills and primary Stats
 * 
 * Has awareness for packages and table flow as well. This is a good class
 * to use as a base for generating non Eclipse-Phase characters
 * 
 * @author Vigilant
 */
public class SkilledCharacter extends BaseCharacter {
	
	protected HashMap<String,Skill> skills; // Skills are too tightly coupled to Character's state to be useful as AspectHashMap
	protected StatHashMap stats;
	
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
	public SkilledCharacter(String name, boolean autoApplyMastery) 
	{
		super(name);
		this.autoApplyMastery = autoApplyMastery;
		skills = new HashMap<String, Skill>();
		stats = new StatHashMap(" ",false);
		
		currentTable = "";
		lastRolls = new LinkedList<Integer>();
		packages = new ArrayList<String[]>();
	}
	
	public String toString()
	{
		String result = this.getName() + "(" + this.getAge() + ")"+ "\n";	

		if (hasBonusStats())
		{
			HashMap<String,Integer> bonuses = getBonusStats();
			result += this.stats.toString(bonuses) + "\n";
			result += this.getSkillsString(bonuses);	
		}
		else
		{
			result += this.stats.toString() + "\n";
			result += this.getSkillsString();
		}
		
		return result;
	}
	
	
	protected boolean hasBonusStats()
	{
		for (Stat stat : stats.values())
		{
			if (hasVar("bonus"+stat.getName()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	protected HashMap<String,Integer> getBonusStats()
	{
		HashMap<String,Integer> result = new HashMap<String,Integer>();
		
		// we need to populate for every stat we have, putting 0 if there's no variable that matches, that's still ok.
		for (Stat stat : stats.values())			
		{	
			String key = "bonus"+stat.getName();
			if (hasVar(key))
			{
				String val = getVar(key);
				
				if (Utils.isInteger(val))
				{
					result.put("bonus"+stat.getName(), Integer.parseInt(val));
				}
				else
				{
					result.put("bonus"+stat.getName(), 0);
				}
			}
			else
			{
				result.put("bonus"+stat.getName(), 0);
			}
		}
		
		return result;
	}
	
	/**
	 * If a valid aptitude name is provided, will add the value provided to it
	 * @param stat Aptitude name
	 * @param value Integer value between 1 and APTITUDE MAX
	 */
	public void incPrimaryStat(String stat, int value)
	{
		if (!this.stats.containsKey(stat))
		{
			throw new IllegalArgumentException(stat + " is not a valid Primary Stat!");
		}
		
		
		this.stats.get(stat).addValue(value);
		
		calc();
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
			getSkill(skill.getFullName()).addValue(skill.getValue(), true);						
		}
		else
		{
			this.skills.put(skill.getFullName(), skill);
			this.setVar("{newestSkill}", skill.getFullName());
		}
		
		calc();
	}
	
	/**
	 * Returns a skill from the character, throwing an error if it doesn't exist
	 * 
	 * @param skillName Name of the skill the character has
	 * @return Skill object matching skillName
	 */
	public Skill getSkill(String skillName)
	{
		if (this.skills.containsKey(skillName))
		{
			return this.skills.get(skillName);
		}
		else
		{
			throw new IllegalArgumentException("No matching Skill in character for name: " + skillName);
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
			
			calc();
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
			getSkill(skillName).addValue(amount, true);
			calc();
			return true;
		}
		else if (amount > 0)
		{
			this.addSkill(Skill.CreateSkill(skillName, amount));
			calc();
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
			getSkill(skillName).setValue(amount);
			calc();
			return true;
		}
		else if (Skill.isSkill(skillName))
		{
			Skill tempSkl = Skill.CreateSkill(skillName, amount);
			this.skills.put(tempSkl.getFullName(), tempSkl);
			calc();
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
			getSkill(skillName).setSpecialization(specialization);
			calc();
			return true;
		}
		else if (Skill.exists(skillName))
		{
			getSkill(skillName).setSpecialization(specialization);			
			incSkill(skillName,5);
			calc();
			return true;
		}
		else
		{
			throw new IllegalArgumentException("No skill exists for : " + skillName);
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
	
	/**
	 * Takes all the character's skills and returns a list of String[]
	 * in the form of {skillName,value} . skillName will include the specialization if applicable
	 * Character's base aptitude values will be factored into the calculation
	 * @param bonuses Optional array of bonus values to factor in. Can be null safely
	 * @return ArrayList of strings representing character skill values
	 */
	public ArrayList<String[]> getSkills(HashMap<String,Integer> bonuses)
	{
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		for (Skill skill : skills.values())
		{			
			int bonus = 0;
			if (bonuses != null)
			{
				String key = "bonus"+skill.getLinkedApt();
				
				// if we have bonuses our logic gets a bit more complicated
				if (bonuses.containsKey(key))
				{
					bonus = bonuses.get(key);
				}
			}
			
			int finalVal = Math.min(99, getFinalSklVal(skill)+bonus); // note that bonuses are outside of usual rules for increases past the reduction point
																	  // they apply in full
			
			String[] temp = {skill.getFullName(), ""+finalVal};
			result.add(temp);
		}
		
		return result;
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
		return getSkillsString(null);
	}
	
	public String getSkillsString(HashMap<String,Integer> bonuses)
	{
		String result = "";
		int cnt = 0;
		boolean first = true;
		
		
		for (String[] skl : this.getSkills(bonuses))
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
	
	public StatHashMap stats() {
		return stats;
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
