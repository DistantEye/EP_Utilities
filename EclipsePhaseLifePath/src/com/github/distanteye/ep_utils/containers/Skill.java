package com.github.distanteye.ep_utils.containers;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.github.distanteye.ep_utils.core.Utils;

/**
 * Container for Eclipse Phase skills.
 * Has an exists method, to validate whether a name is a valid Skill.
 * Skill objects are intended to only be of certain names/descriptions predefined at start
 * 
 * There is generally a rule that Skills cost more points to buy when over a certain value,
 * this is defined by Skill.EXPENSIVE_LEVEL, and there are options available to either apply that
 * adjustment directly to Skill value, or return it via a calculation method 
 * 
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
	private boolean canDefault;
	private ArrayList<String> categories;
	public static int EXPENSIVE_LEVEL = 60; // As defined by Core, adding over this value costs more Rez/CP 	
	public static final int LEVEL_CAP = 99;
	
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
	private static final Pattern skillSpecialization = Pattern.compile(nameReg + optSpace + "\\[" + nameReg +"\\]" + optSpace + nameReg + " " + skillValueReg, Pattern.CASE_INSENSITIVE);
	private static final Pattern skillFullForm =  Pattern.compile(nameReg + ":" + optSpace + nameReg + optSpace + "\\[" + nameReg +"\\]" + optSpace + nameReg + " " + skillValueReg, Pattern.CASE_INSENSITIVE);
	
	public static final Pattern[] formats = {basicSkill,skillSubtype,skillSpecialization,skillFullForm};
	public static final String[] formatTypes = {"basic","subtype","specialization","full"};
	
	// stores all the below skills
	public static ArrayList<Skill> skillList = new ArrayList<Skill>();
	
	/**
	 * @param name Skill name
	 * @param linkedApt What Aptitude this Skill uses (STR,WIL,etc)
	 * @param subtype For field skills, the subtype, such as Ground for the Pilot Skill
	 * @param specialization A special focus for this Skill (often blank)
	 * @param value Skill level/skill points
	 * @param canDefault Whether Characters without this skill can default to their Aptitude value
	 * @param categories Cannot be null, first entry should always be knowledge or active
	 */
	private Skill(String name, String linkedApt, String subtype,
			String specialization, int value,boolean canDefault, String[] categories) {
		this.name = name;
		this.linkedApt = linkedApt;
		this.subtype = subtype;
		this.specialization = specialization;
		this.value = value;
		this.canDefault = canDefault;
		this.categories = new ArrayList<String>();
		this.categories.addAll(Arrays.asList(categories));
		this.categories.add(this.name); // the name of a skill is always one of it's categories
		
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
	 * Copy constructor for Skill
	 * @param skl Valid Skill object
	 */
	public Skill(Skill skl)
	{
		this.name = skl.name;
		this.linkedApt = skl.linkedApt;
		this.subtype = skl.subtype;
		this.specialization = skl.specialization;
		this.value = skl.value;
		this.canDefault = skl.canDefault;
		this.isActive = skl.isActive;
		this.isKnowledge = skl.isKnowledge;
		this.categories = new ArrayList<String>();
		
		for (String str : skl.categories)
		{
			this.categories.add(str);
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
		String[] cats = (parts[3]).split(",");
		
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
		if (name.contains(":") || name.contains("["))
		{
			// if we see signs that it's a more advanced skill type, we have to use the stronger methods
			return Skill.CreateSkillFromString(name + " " + value);
		}
		else
		{
			return Skill.CreateSkill(name,"","",value);
		}
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
		String result = this.getFullName() + " : " + this.value;
		
		return result;
	}
	
	/**
	 * Returns the full identification for this skill. If it has no subtype, it simply returns the name.
	 * If it has a subtype, it'll return a proper "name: subtype" string
	 * @return Returns full skill identifier as a String
	 */
	public String getFullName()
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
		
		return result;
	}
	
	public String getName() {
		return name;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Sets value for skill, capping between 0 and 99
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
		
		// capping
		if (this.value > 99)
		{
			this.value = 99;
		}
		else if (this.value < 0)
		{
			this.value = 0;
		}
	}

	/**
	 * Adds a set value to this Skill's skill points, without any adjustments 
	 * 
	 * @param value the value to increase by (can be negative)
	 */
	public void addValue(int value) {
		this.setValue(this.value+value);
	}
	
	/**
	 * This form of the method automatically halves any gains to the skill the add bring the skill above Skill.EXPENSIVE_LEVEL
	 * 
	 * @param value the value to increase by (can be negative)
	 * @param masteryFlag If true, the value added will only add half gains once the skill would be pushed above Skill.EXPENSIVE_LEVEL
	 */
	public void addValue(int value, boolean masteryFlag) {
		if (value < 0 || !masteryFlag || this.value+value <= Skill.EXPENSIVE_LEVEL)
		{
			this.addValue(value);
		}
		else
		{
			this.setValue(skillAdjustExpensiveCap(this.value+value));
		}
		
	}
	
	/**
	 * After skills go above a certain point they become more expensive to level, this method assumes
	 * those calculations haven't been applied yet and gives an adjusted value to the passed in number 
	 * 
	 * @param val Input to check against the cap
	 * @return Adjusted version of val, which may be lower if it was over Skill.EXPENSIVE_LEVEL
	 */
	protected static int skillAdjustExpensiveCap(int val)
	{
		if (val <= Skill.EXPENSIVE_LEVEL)
		{
			return val;
		}
		else
		{
			// calculate how much it goes over Skill.EXPENSIVE_LEVEL and save that value
			int leftover = val - Skill.EXPENSIVE_LEVEL;
						
			int result = Skill.EXPENSIVE_LEVEL;
						
			// add the rest with calculations factored in
			result += leftover/2;
			return result;
		}
	}
	
	public boolean isActiveSkill() {
		return isActive;
	}

	public boolean isKnowledge() {
		return isKnowledge;
	}

	public String getLinkedApt() {
		return linkedApt;
	}

	public boolean getCanDefault() {
		return canDefault;
	}

	/**
	 * Returns a deepy copy of the Skill categories
	 * @return String[] of Skill's categories
	 */
	public String[] getCategories() {
		String[] deepCopy = new String[categories.size()];
		
		for (int x = 0; x < categories.size(); x++)
		{
			deepCopy[x] = categories.get(x);
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
	
	/**
	 * Checks the predefined skills to see if one exists with the given name
	 * @param skillName Name of skills to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String skillName)
	{
		for (Skill s : skillList)
		{
			if (s.getName().equalsIgnoreCase(skillName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets the linked aptitude for the named skill, if it exists
	 * @param name The name of the skill to search for
	 * @return A string containing the aptitude linked to that skill
	 */
	public static String getSkillApt(String name)
	{
		// we just need the primary skill name, nothing more advanced
		if (name.contains(":"))
		{
			name = name.substring(0, name.indexOf(':')).trim();
		}
		if (name.contains("["))
		{
			name = name.substring(0, name.indexOf('[')).trim();
		}
		
		for (Skill s : skillList)
		{
			if (s.getName().equalsIgnoreCase(name))
			{
				return s.getLinkedApt();
			}
		}
		
		throw new IllegalArgumentException("No such skill exists(" + name + ")!");		
	}
	
	/**
	 * Returns information sufficient to reconstruct this object, encoded in xml
	 * Contains:	 * 
	 * String name, String subtype, String specialization, int value
	 * @return An xml tag <skill><name>name</name><subtype>subtype</subtype><specialization>specialization</specialization><value>value</value></skill>
	 */
	public String toXML()
	{
		Element root = new Element("skill");
		Document doc = new Document(root);
		
		doc.getRootElement().addContent(new Element("name").setText( name ));
		doc.getRootElement().addContent(new Element("subtype").setText( subtype ));
		doc.getRootElement().addContent(new Element("specialization").setText( specialization ));
		doc.getRootElement().addContent(new Element("value").setText( ""+value ));
		
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setOmitDeclaration(true));
		
		return xmlOut.outputString(doc);}
	
	public static Skill fromXML(String xml)
	{
		Document document = Utils.getXMLDoc(xml);
		Element root = document.getRootElement();
		
		Utils.verifyTag(root, "skill");
		Utils.verifyChildren(root, new String[]{"name","subtype","specialization","value"});
		
		String name = root.getChildText("name");
		String subtype = root.getChildText("subtype");
		String specialization = root.getChildText("specialization");
		String valueStr = root.getChildText("value");
		
		int value = -1;
		if (Utils.isInteger(valueStr))
		{
			value = Integer.parseInt(valueStr);
		}
		else
		{
			throw new IllegalArgumentException("Value must be integer!");
		}
		
		return CreateSkill(name,subtype,specialization,value);
	}
}
