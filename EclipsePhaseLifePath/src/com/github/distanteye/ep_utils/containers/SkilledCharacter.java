package com.github.distanteye.ep_utils.containers;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.github.distanteye.ep_utils.core.Step;

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
	protected AspectHashMap<Stat> stats;
	
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
		stats = new AspectHashMap<Stat>(" ",false);
		
		currentTable = "";
		lastRolls = new LinkedList<Integer>();
		packages = new ArrayList<String[]>();
	}
	
	public String toString()
	{
		String result = this.getName() + "(" + this.getAge() + ")"+ "\n";
		result += this.stats.toString() + "\n";
		result += this.getSkillsString();
		
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
	
	public AspectHashMap<Stat> stats() {
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
