import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class Morph {
	private String name;
	private String description;
	private String morphType;
	private String effects;
	
	
	// stores all the below skills
	public static ArrayList<Morph> morphList = new ArrayList<Morph>();
	
	/**
	 * @param name Name of morph
	 * @param description Human readable description of morph
	 * @param morphType The type of the morph Biomorph, Infomorph, Synthmorph, Pod
	 * @param effects Effects string that models the effects caused by possessing the morph 
	 */
	private Morph(String name, String description, String morphType, String effects) {
		super();
		this.name = name;
		this.description = description;
		this.morphType = morphType;
		this.effects = effects;		
	}
	
	private Morph(Morph t)
	{
		super();
		this.name = t.name;
		this.description = t.description;
		this.morphType = t.morphType;
		this.effects = t.effects;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
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
	 * @return the morphType
	 */
	public String getMorphType() {
		return morphType;
	}

	/**
	 * @param morphType the morphType to set
	 */
	public void setMorphType(String morphType) {
		this.morphType = morphType;
	}

	/**
	 * Creates a new Morph that is stored statically in the class.
	 * 
	 * isSynth is a true/false field
	 * @param input String of format 'MorphName|Description|MorphType|Effects
	 */
	public static void CreateInternalMorph(String input)
	{
		String[] parts = input.split("\\|");
		
		if (parts.length != 4 )
		{
			throw new IllegalArgumentException("Invalidly formatted Morph string : " + input);
		}
		
		
		Morph temp = new Morph(parts[0],parts[1], parts[2],parts[3]);
		Morph.morphList.add(temp);
	}
	
	
}
