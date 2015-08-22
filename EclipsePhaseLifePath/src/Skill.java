import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class Skill {

	private String name;
	private String linkedApt;
	private String subtype;
	private String specialization;
	private int value;
	private boolean isActive;
	private boolean isKnowledge;
	private boolean canDefault = true;
	private String[] categories;
	
	// stuff related to regexes
	// we define constants to make the regexes more readable
	
	// Skill,subtype,specialization name
	private static final String nameReg = "([a-zA-Z/ ][a-zA-Z\\-/\\*\\(\\) ]*|\\?[0-9]*\\?)"; // we allow for parsing of choice notation like ?1? and /
	// Skill value
	private static final String skillValueReg = "([0-9]{1,2})";
	// optional space
	private static final String optSpace = "[ ]?";
	
	private static final Pattern basicSkill = Pattern.compile(nameReg + " " + skillValueReg, Pattern.CASE_INSENSITIVE);
	private static final Pattern skillSubtype = Pattern.compile(nameReg + ":" + optSpace + nameReg + " " + skillValueReg, Pattern.CASE_INSENSITIVE);
	private static final Pattern skillSpecialization = Pattern.compile(nameReg + optSpace + "\\[" + nameReg +"\\]" + optSpace + nameReg, Pattern.CASE_INSENSITIVE);
	private static final Pattern skillFullForm =  Pattern.compile(nameReg + ":" + optSpace + nameReg + optSpace + "\\[" + nameReg +"\\]" + optSpace + nameReg, Pattern.CASE_INSENSITIVE);
	
	public static final Pattern[] formats = {basicSkill,skillSubtype,skillSpecialization,skillFullForm};
	public static final String[] formatTypes = {"basic","subtype","specialization","full"};
	
	// commonly used categories
	private static final String[] CAT_KF = {"Knowledge","Field"};
	private static final String[] CAT_K = {"Knowledge"};
	private static final String[] CAT_AS = {"Active", "Social"};
	private static final String[] CAT_AC = {"Active", "Combat"};
	private static final String[] CAT_AM = {"Active", "Mental"};
	private static final String[] CAT_AMPs = {"Active", "Mental","Psi"};
	private static final String[] CAT_AT = {"Active", "Technical"};
	private static final String[] CAT_APh = {"Active", "Physical"};
	private static final String[] CAT_AV = {"Active", "Vehicle"};
	
	// stores all the below skills
	public static ArrayList<Skill> skillList = new ArrayList<Skill>();
	
	/* We now add these based on file IO
	public static final Skill Academics = new Skill("Academics","COG","","",-1,true,CAT_K);
	public static final Skill AnimalHandling = new Skill("Animal Handling","SAV","","",-1,true,CAT_AS);
	public static final Skill Art = new Skill("Art","INT","","",-1,true,CAT_K);
	public static final Skill BeamWeapons = new Skill("Beam Weapons","COO","","",-1,true,CAT_AC);
	public static final Skill Blades = new Skill("Blades","SOM","","",-1,true,CAT_AC);
	public static final Skill Climbing = new Skill("Climbing","SOM","","",-1,true,CAT_APh);
	public static final Skill Clubs = new Skill("Clubs","SOM","","",-1,true,CAT_AC);
	public static final Skill Control = new Skill("Control","WIL","","",-1,false,CAT_AMPs);
	public static final Skill Deception = new Skill("Deception","SAV","","",-1,true,CAT_AS);
	public static final Skill Demolitions = new Skill("Demolitions","COG","","",-1,false,CAT_AT);
	public static final Skill Disguise = new Skill("Disguise","INT","","",-1,true,CAT_APh);
	public static final Skill ExoticLanguage = new Skill("Exotic Language","INT","","",-1,true,CAT_KF);
	public static final Skill ExoticMeleeWeapon = new Skill("Exotic Melee Weapon","SOM","","",-1,true,CAT_AC);
	public static final Skill ExoticRangedWeapon = new Skill("Exotic Ranged Weapon","COO","","",-1,true,CAT_AC);
	public static final Skill Flight = new Skill("Flight","SOM","","",-1,true,CAT_APh);
	public static final Skill Fray = new Skill("Fray","REF","","",-1,true,CAT_AC);
	public static final Skill FreeFall = new Skill("Free Fall","REF","","",-1,true,CAT_APh);
	public static final Skill FreeRunning = new Skill("Freerunning","SOM","","",-1,true,CAT_APh);
	public static final Skill Gunnery = new Skill("Gunnery","INT","","",-1,true,CAT_AC);
	public static final Skill Hardware = new Skill("Hardware","COG","","",-1,true,CAT_AT);
	public static final Skill Impersonation = new Skill("Impersonation","SAV","","",-1,true,CAT_AS);
	public static final Skill Infiltration = new Skill("Infiltration","COO","","",-1,true,CAT_APh);
	public static final Skill Infosec = new Skill("Infosec","COG","","",-1,false,CAT_AT);
	public static final Skill Interest = new Skill("Interest","COG","","",-1,true,CAT_K);
	public static final Skill Interfacing = new Skill("Infacing","COG","","",-1,true,CAT_AT);
	public static final Skill Intimidation = new Skill("Intimidation","SAV","","",-1,true,CAT_AT);
	public static final Skill Investigation = new Skill("Investigation","INT","","",-1,true,CAT_AM);
	public static final Skill Kinesics = new Skill("Kinesics","SAV","","",-1,true,CAT_AS);
	public static final Skill KineticWeapons = new Skill("KineticWeapons","COO","","",-1,true,CAT_AC);
	public static final Skill Language = new Skill("Language","INT","","",-1,true,CAT_K);
	public static final Skill Medicine = new Skill("Medicine","COG","","",-1,true,CAT_AT);
	public static final Skill Navigation = new Skill("Navigation","INT","","",-1,true,CAT_AM);
	public static final Skill Networking = new Skill("Networking","SAV","","",-1,true,CAT_AS);
	public static final Skill Palming = new Skill("Palming","COO","","",-1,true,CAT_APh);
	public static final Skill Perception = new Skill("Perception","INT","","",-1,true,CAT_AM);
	public static final Skill Persuasion = new Skill("Persuasion","SAV","","",-1,true,CAT_AS);
	public static final Skill Pilot = new Skill("Pilot","REF","","",-1,true,CAT_AV);
	public static final Skill Profession = new Skill("Profession","COG","","",-1,true,CAT_K);
	public static final Skill Programming = new Skill("Programming","COG","","",-1,false,CAT_AT);
	public static final Skill Protocol = new Skill("Protocol","SAV","","",-1,true,CAT_AS);
	public static final Skill PsiAssault = new Skill("Psi Assualt","WIL","","",-1,false,CAT_AMPs);
	public static final Skill Psychosurgery = new Skill("Psychosurgery","INT","","",-1,true,CAT_AT);
	public static final Skill Research = new Skill("Research","COG","","",-1,true,CAT_AT);
	public static final Skill Scrounging = new Skill("Scrounging","INT","","",-1,true,CAT_AM);
	public static final Skill SeekerWeapons = new Skill("Seeker Weapons","COO","","",-1,true,CAT_AC);
	public static final Skill Sense = new Skill("Sense","INT","","",-1,false,CAT_AMPs);
	public static final Skill SprayWeapons = new Skill("Spray Weapons","COO","","",-1,true,CAT_AC);
	public static final Skill Swimming = new Skill("Swimming","SOM","","",-1,true,CAT_APh);
	public static final Skill ThrowingWeapons = new Skill("Throwing Weapons","COO","","",-1,true,CAT_AC);
	public static final Skill UnarmedCombat = new Skill("Unarmed Combat","SOM","","",-1,true,CAT_AC);
	*/
	
	/**
	 * @param name
	 * @param linkedApt
	 * @param subtype
	 * @param specialization
	 * @param value
	 * @param canDefault
	 * @param categories Cannot be null, first entry should always be knowledge or active
	 */
	private Skill(String name, String linkedApt, String subtype,
			String specialization, int value,boolean canDefault, String[] categories) {
		super();
		this.name = name;
		this.linkedApt = linkedApt;
		this.subtype = subtype;
		this.specialization = specialization;
		this.value = value;
		this.canDefault = canDefault;
		this.categories = categories;
		
		if (categories.length < 1)
		{
			throw new IllegalArgumentException("Categories must contain at least one value");
		}
		else
		{
			if (categories[0].equalsIgnoreCase("knowledge"))
			{
				this.isActive = false;
				this.isKnowledge = true;
			}
			else if (categories[0].equalsIgnoreCase("active"))
			{
				this.isActive = true;
				this.isKnowledge = false;
			}
			else
			{
				throw new IllegalArgumentException("The first category must be either knowledge or active");
			}
		}
	}

	/**
	 * Creates a new Skill that is stored statically in the class
	 * @param input String of format 'Skillname;linkedAptitude;canDefault(true/false);List of categories separated by commas
	 */
	public static void CreateInternalSkill(String input)
	{
		String[] parts = input.split(";");
		if (parts.length != 4 && (parts[2].equalsIgnoreCase("true") || parts[2].equalsIgnoreCase("false")))
		{
			throw new IllegalArgumentException("Invalidly formatted Skill string : " + input);
		}
		
		// every skill has it's parent name as a category (done for sake of grouping Field skills)
		String[] cats = (parts[3]+","+parts[0]).split(",");
		
		Skill temp = new Skill(parts[0],parts[1],"","",-1,Boolean.valueOf(parts[2]), cats);
		Skill.skillList.add(temp);
	}
	
	/**
	 * Returns a copy of a random skill from the master list. Will not have specializations or subtypes
	 * @param rng Initialized SecureRandom object
	 * @param value Value to set the skill at
	 * @return
	 */
	public static Skill getRandomSkill(SecureRandom rng, int value)
	{
		Skill temp = skillList.get(rng.nextInt(skillList.size()));
		
		return new Skill(temp.getName(),temp.getLinkedApt(),"","", value,temp.getCanDefault(),temp.getCategories());
	}
	
	/**
	 * Creates a Skill object by copying one of the static objects with the same skill name
	 * 
	 * @param name The name of the skill (must match one of the static skills created in this class)
	 * @param value Any integer value between 0 and 99
	 * @return Valid Skill object matching the parameters
	 */
	public static Skill CreateSkill(String name, int value)
	{
		return Skill.CreateSkill(name,"","",value);
	}
	
	/**
	 * Checks the list of predefined skills to see if there is one with a name matching the passed parameter
	 * @param name Any valid string using letters, spaces, or an asterisk. Note the asterisk has no special meaning to the code, and is rather included for compatibility with some notation in the EP books
	 * @return
	 */
	public static boolean isSkill(String name)
	{
		String searchName = name.replaceAll("\\*", ""); // we remove any asterisks that may be there because of book notes
		
		// special handling for wildcard skill adding
		if (searchName.matches("\\?[0-9]+\\? [0-9]+")) 
		{
			return true;
		}
					
		
		for (Skill skl : Skill.skillList)
		{
			if (searchName.toLowerCase().startsWith(skl.getName().toLowerCase()) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Creates a Skill object by copying one of the static objects with the same skill name
	 * 
	 * @param name The name of the skill (must match one of the static skills created in this class)
	 * @param subtype The subtype of the skill, such as Walker, for the Pilot skill. Leave as "" if inapplicable. Should not contain ':' 
	 * @param specialization A specialization of the skill, such as Improvised Explosives, for the Demolitions skill. Leave as "" if inapplicable. Should not contain ':' 
	 * @param value Any integer value between 0 and 99
	 * @return
	 */
	public static Skill CreateSkill(String name, String subtype, String specialization, int value)
	{
		Skill temp = null; // basic reference to fill as we search for the correct value
		String searchName = name.replaceAll("\\*", ""); // we remove any asterisks that may be there because of book notes
		
		for (Skill skl : Skill.skillList)
		{
			if (skl.getName().equalsIgnoreCase(searchName))
			{
				temp = skl;
			}
		}
		
		if (temp == null)
		{
			throw new IllegalArgumentException("No skill can be found to match '" + name + "'. Skill must match one of the provided skills");
		}
		
		// we continue error checking
		if (value < 0 || value > 99)
		{
			throw new IllegalArgumentException("Skill value must be between 0 and 99, value passed: " + value);
		}
		
		if (subtype.contains(":"))
		{
			throw new IllegalArgumentException("Subtype cannot contain colons (':').");
		}
		
		if (specialization.contains(":"))
		{
			throw new IllegalArgumentException("Specialization cannot contain colons (':').");
		}
		
		// at this point we've passed all the checks, proceed with the copy
		
		return new Skill(temp.getName(),temp.getLinkedApt(),subtype,specialization, value,temp.getCanDefault(),temp.getCategories());
	}
	
	/**
	 * Does a basic parse to determine whether the input string could be parsed
	 * @param input Input string that hopefully can be parsed into a school
	 * @param uncertainty returns in first index of array the location of any uncertainty (choice options)
	 * @return True if parseable, false if not
	 */
	public static boolean isSkillFormat(String input, String[] uncertainty)
	{
		for (int x = 0; x < formats.length; x++)
		{
			Matcher temp = formats[x].matcher(input);
			
			if (temp.matches())
			{
				if (input.contains("/") || input.contains("?"))
				{
					// only one / is allowed
					if (input.replaceFirst("/", "").contains("/"))
					{
						return false;
					}
					
						if (input.contains("?")) 
						{
							
						// do some analysis
						if (x == 0)
						{
							// basic case
							String test = temp.group(0);
							if (test.contains("/") || test.contains("?"))
							{
								uncertainty[0] = "skill";
							}
						}
						else if (x == 1)
						{
							// subtype
							String test = temp.group(0);
							if (test.contains("/") || test.contains("?"))
							{
								uncertainty[0] = "skill";
							}
							else
							{
								test = temp.group(1);
								if (test.contains("/") || test.contains("?"))
								{
									uncertainty[0] = "subtype";
								}
							}
						}
						else if (x == 2)
						{
							// specialization
							String test = temp.group(0);
							if (test.contains("/") || test.contains("?"))
							{
								uncertainty[0] = "skill";
							}
							else
							{
								test = temp.group(1);
								if (test.contains("/") || test.contains("?"))
								{
									uncertainty[0] = "specialization";
								}
							}
						}
						else if (x == 3)
						{
							// full
							String test = temp.group(0);
							if (test.contains("/") || test.contains("?"))
							{
								uncertainty[0] = "skill";
							}
							else
							{
								test = temp.group(1);
								if (test.contains("/") || test.contains("?"))
								{
									uncertainty[0] = "subtype";
								}
								else
								{
									test = temp.group(2);
									if (test.contains("/") || test.contains("?"))
									{
										uncertainty[0] = "specialization";
									}
								}
							}
						}
						else
						{
							return false; // unaccounted for case
						}
						
					}
				}
					
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Does a basic parse to determine whether the input string could be parsed
	 * @param input Input string that hopefully can be parsed into a school
	 * @return True if parseable, false if not
	 */
	public static boolean isSkillFormat(String input)
	{
		for (int x = 0; x < formats.length; x++)
		{
			if (formats[x].matcher(input).matches())
			{
				// only one / is allowed
				if (input.replaceFirst("/", "").contains("/"))
				{
					return false;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	/** attempts to construct from input string
	 * 
	 * @param input Input string of four acceptable formats (choice notation not supported and will throw an error) :
	 * 				<skillname> <number>
	 * 				<skillname>: <subtype> <number> 
	 * 				<skillname>[<specialization>] <number>
	 * 				<skillname>: <subtype> [<specialization>] <number>
	 * @return A Skill object
	 */ 
	public static Skill CreateSkillFromString(String input)
	{	
		if (input.contains("?"))
		{
			throw new IllegalArgumentException("Choice notation (?1?) is not supported by this method");
		}
		
		input = input.replace("*", "");
		
		// the various patterns we can understand
		Matcher basicMatch = Skill.basicSkill.matcher(input);
		Matcher subtypeMatch = Skill.skillSubtype.matcher(input);
		Matcher specializationMatch = Skill.skillSpecialization.matcher(input);
		Matcher fullFormMatch = Skill.skillFullForm.matcher(input);
		
		if (basicMatch.matches())
		{
			String name = basicMatch.group(1);
			int value = Integer.parseInt(basicMatch.group(2));
			return Skill.CreateSkill(name, value);
			
		}
		else if (subtypeMatch.matches())
		{
			String name = subtypeMatch.group(1);
			String subtypeName = subtypeMatch.group(2);
			int value = Integer.parseInt(subtypeMatch.group(3));
			
			return Skill.CreateSkill(name, subtypeName, "",value);
		}
		else if (specializationMatch.matches())
		{
			String name = specializationMatch.group(1);
			String specializationName = specializationMatch.group(2);
			int value = Integer.parseInt(specializationMatch.group(3));
			
			return Skill.CreateSkill(name, "", specializationName,value);
		}
		else if (fullFormMatch.matches())
		{
			String name = fullFormMatch.group(1);
			String subtypeName = fullFormMatch.group(2);
			String specializationName = fullFormMatch.group(3);
			int value = Integer.parseInt(fullFormMatch.group(4));
			
			return Skill.CreateSkill(name, subtypeName, specializationName,value);
		}
		else
		{
			throw new IllegalArgumentException("Could not parse any matching Skills from the String");
		}
	}

	public String toString()
	{
		String result = "";
		
		result += name;
		
		if (this.subtype.length() > 0)
		{
			result += ": " + subtype + " ";
		}
		
		if (this.specialization.length() > 0)
		{
			result += "[" + specialization + "] ";
		}
		
		result = result.trim();
		
		result += " : " + this.value;
		
		return result;
	}
	
	/**
	 * Returns the full identification for this skill. If it has no subtype, it simply returns the name.
	 * If it has a subtype, it'll return a proper "name: subtype" string
	 * @return Returns full skill identifier as a String
	 */
	public String getFullName()
	{
		if (this.getSubtype().length() > 0)
		{
			return this.getName() + ": " + this.getSubtype();
		}
		else
		{
			return this.getName();
		}
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * @param subtype the subtype to set
	 */
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	/**
	 * @return the specialization
	 */
	public String getSpecialization() {
		return specialization;
	}

	/**
	 * @param specialization the specialization to set
	 */
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
		
		// implement capping
		if (this.value > 99)
		{
			this.value = 99;
		}
		else if (this.value < -1)
		{
			this.value = -1;
		}
	}

	/**
	 * @param value the value to increase by (can be negative)
	 */
	public void addValue(int value) {
		this.setValue(this.value+value);
	}
	
	/**
	 * This form of the method automatically halves any gains to the skill the add bring the skill above 60
	 * 
	 * @param value the value to increase by (can be negative)
	 * @param masteryFlag If true, the value added will only add half gains once the skill would be pushed above 60
	 */
	public void addValue(int value, boolean masteryFlag) {
		if (value < 0 || !masteryFlag || this.value+value <= 60)
		{
			this.addValue(value);
		}
		else
		{
			// calculate how much it goes over 60 and save that value
			int leftover = this.value+value - 60;
			
			// bring value up to 60
			this.value = 60;
			
			// add the rest with calculations factored in
			this.value += leftover/2;
			
		}
		
	}
	
	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @return the isKnowledge
	 */
	public boolean isKnowledge() {
		return isKnowledge;
	}


	/**
	 * @return the linkedApt
	 */
	public String getLinkedApt() {
		return linkedApt;
	}

	/**
	 * @return the canDefault
	 */
	public boolean getCanDefault() {
		return canDefault;
	}

	/**
	 * @return the categories
	 */
	public String[] getCategories() {
		String[] deepCopy = new String[categories.length];
		
		for (int x = 0; x < categories.length; x++)
		{
			deepCopy[x] = categories[x];
		}
		
		return deepCopy;
	}

	/** 
	 * Returns whether the passed variable is one of the Skill's categories
	 * @param name Name of skill, will throw error if not a valid skill
	 * @param cat Category to search for, such as "Technical"
	 * @return
	 */
	public static boolean hasCategory(String name, String cat)
	{		
		for (String str : CreateSkillFromString(name + " 1").categories)
		{
			if (str.equalsIgnoreCase(cat))
			{
				return true;
			}
		}
		
		return false;
	}
	
}
