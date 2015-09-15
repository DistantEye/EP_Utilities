package com.github.distanteye.ep_utils.containers;
import java.util.HashMap;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Represents any valid Sleight defined in the Internal Data file. Sleights are immutable
 * This class has a static exists method for determining whether a name is a valid Sleight
 * 
 * @author Vigilant
 *
 */
public class Sleight {
	private SleightType sleightType; 
	private boolean isExsurgent;
	private String name;		
	private UsageType psiType;
	private ActionType actionType;
	private Range range;
	private Duration duration;
	private String strainMod;
	private String skillUsed;
	private String description;
	
	// stores all the below sleights
	public static HashMap<String,Sleight> sleightList = new HashMap<String,Sleight>();
	 
	/**
	 * @param sleightType Classification of Sleight : chi, gamma, etc
	 * @param isExsurgent Whether the sleight is meant to be exsurgent only
	 * @param name Name of the sleight
	 * @param psiType Active or Passive
	 * @param actionType What type of action the sleight uses
	 * @param range Range of the sleight
	 * @param duration Duration of the sleight
	 * @param strainMod Strain mod of the sleight (if any)
	 * @param skillUsed Skill the sleight uses (if active
	 * @param description Human readable description of sleight
	 */
	private Sleight(SleightType sleightType, Boolean isExsurgent, String name,
			UsageType psiType, ActionType actionType, Range range, Duration duration,
			String strainMod, String skillUsed, String description) {
		super();
		this.sleightType = sleightType;
		this.isExsurgent = isExsurgent;
		this.name = name;
		this.psiType = psiType;
		this.actionType = actionType;
		this.range = range;
		this.duration = duration;
		this.strainMod = strainMod;
		this.skillUsed = skillUsed;
		this.description = description;
	}

	/**
	 * Checks the predefined sleights to see if one exists with the given name
	 * @param sleightName Name of sleight to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String sleightName)
	{
		return sleightList.containsKey(sleightName);				
	}
	
	public String toString()
	{
		return this.name + " (" + this.sleightType + ")";
	}
	
	/**
	 * As before, but includes the description as well
	 * @return A longer String representing the Sleight
	 */
	public String toStringLong()
	{
		return this.name + " (" + this.sleightType + ") " + " : " + this.description;
	}
	

	public SleightType getSleightType() {
		return sleightType;
	}

	public Boolean isExsurgent() {
		return isExsurgent;
	}

	public String getName() {
		return name;
	}

	public UsageType getPsiType() {
		return psiType;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public Range getRange() {
		return range;
	}

	public Duration getDuration() {
		return duration;
	}

	public String getStrainMod() {
		return strainMod;
	}

	public String getSkillUsed() {
		return skillUsed;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Creates a new sleight that is stored statically in the class
	 * @param input String[] of format SleightType;IsExsurgent;SleightName;ActivePassive;ActionType;Range;Duration;StrainMod;skillUsed;Description
	 */
	public static void CreateInternalsleight(String[] input)
	{
		if (input.length != 10 )
		{
			throw new IllegalArgumentException("Array for Sleight must have 10 parts");
		}
		
		int cnt = 0;
		SleightType sleightType = EnumFactory.getEnum(SleightType.class,input[cnt++]);
		Boolean isExsurgent = Boolean.parseBoolean(input[cnt++]);
		String sleightName = input[cnt++];
		UsageType activePassive = EnumFactory.getEnum(UsageType.class,input[cnt++]);
		ActionType actionType = EnumFactory.getEnum(ActionType.class,input[cnt++]);
		Range range = EnumFactory.getEnum(Range.class,input[cnt++]);
		Duration duration = EnumFactory.getEnum(Duration.class,input[cnt++]);
		String strainMod = input[cnt++];
		String skillUsed = input[cnt++];
		String description = input[cnt++];
		
		
		Sleight temp = new Sleight(sleightType, isExsurgent, sleightName,activePassive, actionType, range, duration, strainMod, skillUsed, description);
		Sleight.sleightList.put(temp.getName(),temp);
	}
	
	/**
	 * The classification of this Sleight: CHI/GAMMA/EPSILON,etc
	 * @author Vigilant
	 */
	public enum SleightType
	{
		CHI,GAMMA,EPSILON
	}
	
	/**
	 * Sleights can be either actively triggered or passive/constant
	 * @author Vigilant
	 */
	public enum UsageType
	{
		ACTIVE,PASSIVE
	}
	
	/**
	 * If the Sleight is passive, this will be AUTOMATIC, if Active,
	 * this enum stores what kind of active action it is to use the Sleight
	 * @author Vigilant
	 */
	public enum ActionType
	{
		AUTOMATIC,QUICK,COMPLEX,TASK
	}
	
	/**
	 * The range of the Sleight, what area it effects
	 * @author Vigilant
	 */
	public enum Range
	{
		SELF,TOUCH,CLOSE
	}
	
	/**
	 * How long the Sleight lasts (Passive Sleights are CONSTANT)
	 * @author Vigilant
	 */
	public enum Duration
	{
		CONSTANT,INSTANT,TEMP_ACTION_TURNS,TEMP_MINUTES,TEMP_HOURS,SUSTAINED
	}
		
	public String toXML(int tab)
	{
		return Utils.tab(tab) + "<sleight>\n" +
				Utils.tab(tab+1) + "<name>" + getName() + "</name>\n" +
			Utils.tab(tab) + "</sleight>\n";		
	}
	
	public static Sleight fromXML(String xml)
	{
		String sleightBlock = Utils.returnStringInTag("sleight", xml, 0);
		String nameStr = Utils.returnStringInTag("name", sleightBlock, 0);
		
		if (!sleightList.containsKey(nameStr))
		{
			throw new IllegalArgumentException("Trait called for name : " + nameStr + ", but no such Sleight exists!");
		}
		
		return Sleight.sleightList.get(nameStr);
	}
}
