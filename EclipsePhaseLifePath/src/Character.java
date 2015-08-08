import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class Character {

	private HashMap<String,Skill> skillList;
	private HashMap<String,Aptitude> aptitudeList;
	private ArrayList<Trait> traitList;
	private HashMap<String,Integer> nonAppStats;
	private HashMap<String,String> otherVars;
	private ArrayList<String> gearList;
	private String name;
	private int age;
	//private String background;
	private LinkedList<String> allBackgrounds;
	private String currentMorph;
	
	// todo, fully remove commented variables
	//private String currentFaction;
	//private String currentPath;
	private LinkedList<Integer> lastRolls;
	private String currentTable;
	private HashMap<String, Rep> repList;
	//private int credits;
	private HashMap<String, Sleight> sleightList;
	private Step lastStep;
	
	/**
	 * @return the currentTable the player is rolling on (if character gen)
	 */
	public String getCurrentTable() 
	{
		return currentTable;
	}

	/**
	 * @param currentTable the currentTable to set
	 */
	public void setCurrentTable(String currentTable) 
	{
		if (!this.currentTable.equals(currentTable))
		{
			lastRolls = new LinkedList<Integer>();
		}
			
		this.currentTable = currentTable;
	}

	/**
	 * Whenever character is going through some kind of character gen, knowing what they rolled last may be useful
	 * 
	 * @return returns last Roll
	 */
	public int getLastRoll() 
	{
		return lastRolls.getLast();
	}

	/**
	 * Checks whether a roll is in the stack of last rolls at al
	 * @param val Int to check the rolls for
	 * @return True if present in collection, false otherwise
	 */
	public boolean rollsContain(int val)
	{
		return lastRolls.contains(val);
	}
	
	/**
	 * @param lastRoll int value to push onto the stack of last rolls
	 */
	public void addLastRoll(int lastRoll) 
	{
		this.lastRolls.push(lastRoll);
	}

	/**
	 * @param name
	 */
	public Character(String name) 
	{
		this.name = name;
		
		skillList = new HashMap<String,Skill>();
		aptitudeList = new HashMap<String,Aptitude>();
		traitList = new ArrayList<Trait>();
		gearList = new ArrayList<String>();
		nonAppStats = new HashMap<String,Integer>();
		otherVars = new HashMap<String,String>();
	
		age = -1; // placeholder
		
		// set up placeholder values for aptitudes
		aptitudeList.put("COG", new Aptitude("COG",0));
		aptitudeList.put("COO", new Aptitude("COO",0));
		aptitudeList.put("INT", new Aptitude("INT",0));
		aptitudeList.put("REF", new Aptitude("REF",0));
		aptitudeList.put("SAV", new Aptitude("SAV",0));
		aptitudeList.put("SOM", new Aptitude("SOM",0));
		aptitudeList.put("WIL", new Aptitude("WIL",0));
		
		// do it for MOX and the rest of the derived stats
		// all stats other than mox,INIT,Speed reflect the user's bonus to that category, since the rest are calculated stats
		nonAppStats.put("MOX", 1);
		nonAppStats.put("TT", 0);
		nonAppStats.put("LUC", 0);
		nonAppStats.put("IR", 0);
		nonAppStats.put("WT", 0);
		nonAppStats.put("DUR", 0);
		nonAppStats.put("DR", 0);
		nonAppStats.put("INIT", 1);
		nonAppStats.put("SPD", 1);
		nonAppStats.put("DB", 0);
	
		
		repList = new HashMap<String,Rep>();
		
		// gather all valid Rep categories from Rep class and add them at 0
		for (String repKey : Rep.repTypes.keySet())
		{
			repList.put(repKey,Rep.getCopyOf(repKey));
		}
		
		sleightList = new HashMap<String, Sleight>();
		
		this.setVar("{credits}", "0");
		this.setVar("{faction}", "");
		this.setVar("{path}", "");
		
		currentTable = "";
		lastRolls = new LinkedList<Integer>();
		allBackgrounds = new LinkedList<String>();
	}
	
	// calculates stats like durability and insanity and such
	public void calcStats()
	{
		// this isn't currently implemented
	}
	
	public String toString()
	{
		String result = this.name + "(" + this.age + ")"+ "\n";
		result = "Morph : " + this.currentMorph + ", Faction : " + this.getVar("{faction}")  + ", Path : " + this.getVar("{path}") + "\n";
		
		result += "Traits : " + this.getTraitsString() + "\n";
		result += this.getAptitudesString() + "\n";
		result += this.getNonAptitudesString() + "\n";
		result += this.getSkillsString() + "\n";
		result += this.getRepString() + "\n";
		result += "Gear : " + this.getGearString();
		
		return result;
	}
	
	/**
	 * Gets the current value of a particular Rep category
	 * 
	 * @param repName Rep category to look for
	 * @return
	 */
	public int getRepValue(String repName)
	{
		if (!Rep.exists(repName))
		{
			throw new IllegalArgumentException(repName + " is not a valid Rep category!");
		}
		
		return this.repList.get(repName).getValue();
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
		
		this.repList.get(repName).incValue(val);
	}
	
	
	
	/**
	 * @return the credits
	 */
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
	
	public boolean isValidAptitude(String str)
	{
		return this.aptitudeList.containsKey(str);
	}
	
	/**
	 * If a valid aptitude name is provided, will set it to the value profided
	 * @param apt Aptitude name
	 * @param value Integer value between 1 and APTITUDE MAX
	 */
	public void setAptitude(String apt, int value)
	{
		if (!isValidAptitude(apt))
		{
			throw new IllegalArgumentException(apt + " is not a valid Aptitude");
		}
		
		if (value < 1 || value > Aptitude.APTITUDE_MAX)
		{
			throw new IllegalArgumentException(value + " must be between 1 and " + Aptitude.APTITUDE_MAX);
		}
		
		this.aptitudeList.get(apt).setValue(value);
	}
	
	/**
	 * If a valid aptitude name is provided, will add the value provided to it
	 * @param apt Aptitude name
	 * @param value Integer value between 1 and APTITUDE MAX
	 */
	public void incAptitude(String apt, int value)
	{
		if (!isValidAptitude(apt))
		{
			throw new IllegalArgumentException(apt + " is not a valid Aptitude");
		}
		
		
		this.aptitudeList.get(apt).addValue(value);
	}
	
	/**
	 *  If a valid aptitude name is provided, returns its value
	 * @param apt Aptitude name
	 * @return Integer value between 1 and APTITUDE MAX
	 */
	public int getAptitude(String apt)
	{
		if (!isValidAptitude(apt))
		{
			throw new IllegalArgumentException(apt + " is not a valid Aptitude");
		}
		
		return this.aptitudeList.get(apt).getValue();
	}
	
	/**
	 * Moxie accessor
	 * @return int value of character's current moxie stat (max, not current amount)
	 */
	public int getMox()
	{
		return this.nonAppStats.get("MOX");
	}
	
	/**
	 * Moxie mutator
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
		
		this.nonAppStats.put("MOX", val);
	}
	
	
	
	/**
	 * Either adds the skill to the character (if it doesn't exist), or increments the skill level
	 * of the skill that's already there, based on the value of the skill passed in
	 * @param skill Valid Skill object with a meaningful Skill level value
	 */
	public void addSkill(Skill skill)
	{
		if (this.skillList.containsKey(skill.getName()) )
		{
			this.skillList.get(skill.getName()).addValue(skill.getValue(), true);
		}
		else
		{
			this.skillList.put(skill.getName(), skill);
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
		if (this.skillList.containsKey(skillName))
		{
			Skill temp = this.skillList.remove(skillName);
			this.setVar("{lastRemSkl}", temp.getName());
			this.setVar("{lastRemSklVal}", ""+temp.getValue());
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Adds/substracts levels from a skill that already exists
	 * 
	 * @param skillName Name of the skill the character has
	 * @param amount Amount to add to the skill, can be negative
	 * @return True if successful, false if skill could not be found or created
	 */
	public boolean incSkill(String skillName, int amount) 
	{
		if (this.skillList.containsKey(skillName))
		{
			this.skillList.get(skillName).addValue(amount, true);
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
		if (this.skillList.containsKey(skillName))
		{
			this.skillList.get(skillName).setValue(amount);
			return true;
		}
		else if (Skill.isSkill(skillName))
		{
			Skill tempSkl = Skill.CreateSkill(skillName, amount);
			this.skillList.put(tempSkl.getFullName(), tempSkl);
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
		if (this.skillList.containsKey(skillName))
		{
			this.skillList.get(skillName).setSpecialization(specialization);
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
		return this.skillList.containsKey(skillName);
	}
	
	/**
	 * Takes all the character's skills and returns a list of String[]
	 * in the form of {skillName,value} . skillName will include the specialization if applicable
	 * 
	 * @return ArrayList of strings representing character skill values
	 */
	public ArrayList<String[]> getSkills()
	{
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		for (Skill skill : skillList.values())
		{
			String[] temp = skill.toString().split(":");
			temp[0] = temp[0].trim();
			temp[1] = temp[1].trim();
			result.add(temp);
		}
		
		return result;
	}
	
	/**
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) 
	{
		this.name = name;
	}

	/**
	 * @return the age
	 */
	public int getAge() 
	{
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(int age) 
	{
		this.age = age;
	}

	/**
	 * @return the background
	 */
	public String getBackground() 
	{
		return this.getVar("{background}");
	}

	/**
	 * @param background the background to set
	 */
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
	
	/**
	 * @return the currentMorph
	 */
	public String getCurrentMorph() 
	{
		return currentMorph;
	}

	/**
	 * @param currentMorph the currentMorph to set
	 */
	public void setCurrentMorph(String currentMorph) 
	{
		this.currentMorph = currentMorph;
	}

	/**
	 * @return the gearList
	 */
	public ArrayList<String> getGearList() 
	{
		return gearList;
	}
	
	/**
	 * Add item to gear list
	 * @param gear Valid equipment name
	 */
	public void addGear(String gear)
	{
		gearList.add(gear);
	}
	
	/**
	 * Remove item from gear list, throwing an error if it doesn't exist
	 * @param gear Valid equipment name of an item that exists, or it will have no effect
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

	/**
	 * Gets trait from characters list, throwing an error if it doesn't exist
	 * @param traitStr Name of trait to search for
	 * @return Trait object matching the search name
	 */
	public Trait getTrait(String traitStr)
	{

		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitStr))
			{
				return t;
			}
		}

		throw new IllegalArgumentException("Character lacks trait : " + traitStr);
	}
	
	/**
	 * Returns whether character has a trait of the name provided
	 * @param traitStr Trait to search for
	 * @return True/False as appropriate
	 */
	public boolean hasTrait(String traitStr)
	{
		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitStr))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns whether character has a trait of the name provided. Will do a partial (startsWith) search
	 * @param traitStr Trait to search for
	 * @return True/False as appropriate
	 */
	public boolean hasTraitPartial(String traitStr)
	{
		for (Trait t : traitList)
		{
			if (t.getName().startsWith(traitStr))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Add item to Trait list
	 * @param t Valid trait object
	 */
	public void addTrait(Trait t)
	{
		traitList.add(t);
	}
	
	/**
	 * Remove trait from Trait list
	 * @param t Valid name of trait that already exists on the character, or it will have no effect
	 * @return The Trait removed, or null if it couldn't be removed
	 */
	public Trait removeTrait(String traitStr)
	{
		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitStr))
			{
				Trait temp = t;
				traitList.remove(t);
				return temp;
			}
		}
		
		throw new IllegalArgumentException("Character lacks trait : " + traitStr);
	}
	
	/**
	 * Gets sleight from characters list, throwing an error if it doesn't exist
	 * @param sleightStr Name of sleight to search for
	 * @return Sleight object matching the search name
	 */
	public Sleight getSleight(String sleightStr)
	{

		if ( sleightList.containsKey(sleightStr))
		{
			return sleightList.get(sleightStr);
		}
		else
		{
			throw new IllegalArgumentException("Character lacks sleight : " + sleightStr);
		}
	}
	
	/**
	 * Add item to Sleight list
	 * @param t Valid sleight object
	 */
	public void addSleight(Sleight t)
	{
		sleightList.put(t.getName(),t);
	}
	
	/**
	 * Remove sleight from Sleight list
	 * @param sleightStr Valid name of sleight that already exists on the character, or it will have no effect
	 * @return The Sleight removed, or null if it couldn't be removed
	 */
	public Sleight removeSleight(String sleightStr)
	{
		if ( sleightList.containsKey(sleightStr))
		{
			Sleight temp =  sleightList.remove(sleightStr);
			return temp;
		}
		else
		{
			throw new IllegalArgumentException("Character lacks sleight : " + sleightStr);
		}
	}
	
	// Helper functions for toString
	
	public String getTraitsString()
	{
		if (this.traitList.size() == 0)
		{
			return "";
		}
		
		String result = this.traitList.get(0).toStringShort();
		
		for (int x = 1; x < this.traitList.size(); x++)
		{
			result += ", " + this.traitList.get(x);
		}
		
		return result;
	}
	
	public String getAptitudesString()
	{
		String result = "";
				
		for (Aptitude apt : aptitudeList.values())
		{
			result += apt.toString() + " ";
		}
		
		// removing trailing space
		return result.trim();
	}
	
	public String getNonAptitudesString()
	{
		String result = "";
				
		for (String key : nonAppStats.keySet())
		{
			result += key + " " + nonAppStats.get(key) + " ";
		}
		
		// removing trailing space
		return result.trim();
	}
	
	public String getRandSkill()
	{
		int idx = LifePathGenerator.rng.nextInt(skillList.size());
		return ((Skill)skillList.values().toArray()[idx]).getFullName();
	}
	
	public String getRandApt()
	{
		int idx = LifePathGenerator.rng.nextInt(aptitudeList.size());
		return (String)aptitudeList.keySet().toArray()[idx];
	}
	
	public int getNumSkills()
	{
		return this.skillList.size();
	}
	
	public String getSkillsString()
	{
		String result = "";
		int cnt = 0;
		boolean first = true;
		
		
		for (Skill skl : skillList.values())
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
			
			result += separator + skl.toString();
			cnt++;
		}
		
		return result.trim();
	}
	
	public String getRepString()
	{
		String result = "";
		
		for (Rep rep : repList.values())
		{
			result += rep.toString() + "\n";
		}
		
		// removing trailing space
		return result.trim();
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
	 * Retrieves a variable from the general store (or from the character special store)
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
	 * @param name Name/Key to search for
	 * @return True if exists, false otherwise
	 */
	public boolean hasVar(String name)
	{
		return this.otherVars.containsKey(name);
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

	/**
	 * @return the lastStep
	 */
	public Step getLastStep() {
		return lastStep;
	}

	/**
	 * @param lastStep the lastStep to set
	 */
	public void setLastStep(Step lastStep) {
		this.lastStep = lastStep;
	}
	
	
	
}
