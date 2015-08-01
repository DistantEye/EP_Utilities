import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class Trait {
	private String name;
	private String description;
	private int level; // default to 1
	
	// stores all the below skills
	public static ArrayList<Trait> traitList = new ArrayList<Trait>();
	
	/**
	 * @param name Name of trait
	 * @param description Human readable description of trait
	 * @param level Level of trait : default is 1
	 */
	private Trait(String name, String description, int level) {
		super();
		this.name = name;
		this.description = description;
		this.level = level;
	}
	
	private Trait(Trait t)
	{
		super();
		this.name = t.name;
		this.description = t.description;
		this.level = t.level;
	}

	/**
	 * Checks the predefined traits to see if one exists with the given name
	 * @param traitName Name of trait to search for
	 * @return True/False as appropriate
	 */
	public static boolean exists(String traitName)
	{
		// loop throught our traits and see if we find a match
		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks the predefined traits to see if one exists with the given name. Will do a partial (startsWith) search
	 * @param traitName Name of trait to search for
	 * @return True/False as appropriate
	 */
	public static boolean existsPartial(String traitName)
	{
		// loop throught our traits and see if we find a match
		for (Trait t : traitList)
		{
			if (t.getName().startsWith(traitName))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Gets a copy of a trait from the predefined list (the level will be mutable, possibly more in the future)
	 * 
	 * @param traitName Name of trait to search for
	 * @param level Level to set the returned trait (if found) to
	 * @return
	 */
	public static Trait getTrait(String traitName, int level)
	{
		for (Trait t : traitList)
		{
			if (t.getName().equalsIgnoreCase(traitName))
			{
				Trait result = new Trait(t);
				result.setLevel(level);
				return result;
			}
		}
		
		return null;
	}
	
	public String toString()
	{
		return this.toStringShort() + " : " + this.description;
	}
	
	public String toStringShort()
	{
		return this.name + " (" + this.level + ")";
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
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	/**
	 * Creates a new Trait that is stored statically in the class
	 * @param input String of format 'TraitName|Description
	 */
	public static void CreateInternalTrait(String input)
	{
		String[] parts = input.split("\\|");
		
		if (parts.length != 2 )
		{
			throw new IllegalArgumentException("Invalidly formatted Trait string : " + input);
		}
		
		Trait temp = new Trait(parts[0],parts[1], 1);
		Trait.traitList.add(temp);
	}
	
		
	
}
