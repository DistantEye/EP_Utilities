import java.util.ArrayList;
import java.util.HashMap;

/**
 * Container for Morphs, holding all possible Morph information.
 * Has static methods for validating whether a Morph exists, and only
 * accepts creating copies of a list of morphs made from CreateInternalMorph
 * 
 * @author Vigilant
 *
 */
public class Morph {
	private String name;
	private String morphType;
	private String description;
	private String implants;
	private HashMap<String,Integer> aptitudeMaximums;
	private int durability;
	private int woundThreshold;
	private int CP;
	private String creditCost;
	private String effects;
	private String notes;
	
	
	// stores all the below skills
	public static ArrayList<Morph> morphList = new ArrayList<Morph>();
	
	/**
	 * @param name Name of morph
	 * @param morphType The type of the morph Biomorph, Infomorph, Synth, Pod
	 * @param description Human readable description of morph
	 * @param implants String containing list of implants for the morph
	 * @param aptitudeMaxStr String of aptitude maximums. Can be a single value for all or a single default value with caveats
	 * @param durability int. Durability value of morph (this may include bonuses from implants)
	 * @param woundThreshold int. Wound threshold for morph
	 * @param CP int. CP cost for morph
	 * @param creditCost String containing the cost class and/or minimum credit value for the morph
	 * @param effects Effects string that models the effects caused by possessing the morph 
	 * @param Notes Any remaining notes about the morph
	 */
	private Morph(String name, String morphType, String description, String implants, String aptitudeMaxStr, int durability, int woundThreshold, int CP,
					String creditCost, String effects, String notes) {
		super();
		this.name = name;
		this.morphType = morphType;
		this.description = description;
		this.implants = implants;
		
		this.aptitudeMaximums = new HashMap<String,Integer>();
		
		String[] tempAptMax = aptitudeMaxStr.split(";");
		
		for (String pair : tempAptMax)
		{
			String[] parts = pair.split(":");
			// this should always be safe below since it comes from a controlled source
			this.aptitudeMaximums.put(parts[0], Integer.parseInt(parts[1]));
		}
		
		this.durability = durability;
		this.woundThreshold = woundThreshold;
		this.CP = CP;
		this.creditCost = creditCost;
		this.effects = effects;
		this.notes = notes;
	}
	
	private Morph(Morph t)
	{
		super();
		this.name = t.name;
		this.morphType = t.morphType;
		this.description = t.description;
		this.implants = t.implants;
		
		this.aptitudeMaximums = new HashMap<String,Integer>();
		for (String key : t.aptitudeMaximums.keySet())
		{
			this.aptitudeMaximums.put(key, t.aptitudeMaximums.get(key));
		}
		
		this.durability = t.durability;
		this.woundThreshold = t.woundThreshold;
		this.CP = t.CP;
		this.creditCost = t.creditCost;
		this.effects = t.effects;
		this.notes = t.notes;
	}
	
	/**
	 * @return the implants
	 */
	public String getImplants() {
		return implants;
	}

	/**
	 * @param implants the implants to set
	 */
	public void setImplants(String implants) {
		this.implants = implants;
	}

	/**
	 * @return the effects
	 */
	public String getEffects() {
		return effects;
	}

	/**
	 * @param effects the effects to set
	 */
	public void setEffects(String effects) {
		this.effects = effects;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the morphType
	 */
	public String getMorphType() {
		return morphType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the aptitudeMaximums
	 */
	public HashMap<String, Integer> getAptitudeMaximums() {
		return aptitudeMaximums;
	}

	/**
	 * @return the durability
	 */
	public int getDurability() {
		return durability;
	}

	/**
	 * @return the woundThreshold
	 */
	public int getWoundThreshold() {
		return woundThreshold;
	}

	/**
	 * @return the cP
	 */
	public int getCP() {
		return CP;
	}

	/**
	 * @return the creditCost
	 */
	public String getCreditCost() {
		return creditCost;
	}	
	
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Checks the predefined morphs to see if one exists with the given name
	 * @param morphName Name of morph to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String morphName)
	{
		// loop throught our morphs and see if we find a match
		for (Morph m : morphList)
		{
			if (m.getName().equalsIgnoreCase(morphName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the predefined morphs to see if one exists with the given name. Will do a partial (startsWith) search
	 * @param morphName Name of morph to search for
	 * @return True/False as appropriate
	 */
	public static boolean existsPartial(String morphName)
	{
		// loop throught our morphs and see if we find a match
		for (Morph t : morphList)
		{
			if (t.getName().startsWith(morphName))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns whether name is a valid morph
	 * @param name String containing name of a Morph
	 * @return True/False as appropriate
	 */
	public boolean itemExists(String name)
	{
		return Morph.exists(name);
	}
	
	/**
	 * Gets a copy of a morph from the predefined list (the level will be mutable, possibly more in the future)
	 * 
	 * @param morphName Name of morph to search for
	 * @return
	 */
	public static Morph createMorph(String morphName)
	{
		for (Morph t : morphList)
		{
			if (t.getName().equalsIgnoreCase(morphName))
			{
				Morph result = new Morph(t);
				return result;
			}
		}
		
		return null;
	}
	
	public String toString()
	{
		return this.toStringShort() + " : " + this.description + ";" + this.effects;
	}
	
	public String toStringShort()
	{
		return this.name;
	}

	/**
	 * Creates a new Morph that is stored statically in the class.
	 * 
	 * isSynth is a true/false field
	 * @param input String[] {name, morphType, description, implants, aptitudeMaxStr, durability, woundThreshold, CP, creditCost, effects, notes}
	 */
	public static void CreateInternalMorph(String[] parts)
	{
		if (parts.length != 11 || !Utils.isInteger(parts[5]) || !Utils.isInteger(parts[6]) || !Utils.isInteger(parts[7]))
		{
			throw new IllegalArgumentException("Invalidly formatted Morph string[] : " + Utils.joinStr(parts,","));
		}
		
		
		Morph temp = new Morph(parts[0],parts[1], parts[2],parts[3],parts[4],
				Integer.parseInt(parts[5]),Integer.parseInt(parts[6]),Integer.parseInt(parts[7]),parts[8],parts[9], parts[10]);
		Morph.morphList.add(temp);
	}
	
	
}
