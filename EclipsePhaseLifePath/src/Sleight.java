import java.util.HashMap;

/**
 * Represents any valid sleight defined in the Internal Data file. Sleights are immutable
 */

/**
 * @author Vigilant
 *
 */
public class Sleight {
	private String sleightType; 
	private String isExsurgent;
	private String name;		
	private String psiType;
	private String actionType;
	private String range;
	private String duration;
	private String strainMod;
	private String skillUsed;
	private String description;
	
	// stores all the below skills
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
	private Sleight(String sleightType, String isExsurgent, String name,
			String psiType, String actionType, String range, String duration,
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
		return this.name + " (" + this.sleightType + ") " + " : " + this.description;
	}
	

	/**
	 * @return the sleightType
	 */
	public String getSleightType() {
		return sleightType;
	}

	/**
	 * @return the isExsurgent
	 */
	public String getIsExsurgent() {
		return isExsurgent;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the psiType
	 */
	public String getPsiType() {
		return psiType;
	}

	/**
	 * @return the actionType
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * @return the range
	 */
	public String getRange() {
		return range;
	}

	/**
	 * @return the duration
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * @return the strainMod
	 */
	public String getStrainMod() {
		return strainMod;
	}

	/**
	 * @return the skillUsed
	 */
	public String getSkillUsed() {
		return skillUsed;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
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
		
		Sleight temp = new Sleight(input[0],input[1], input[2],input[3], input[4], input[5], input[6], input[7], input[8], input[9]);
		Sleight.sleightList.put(temp.getName(),temp);
	}
	
		
	
}
