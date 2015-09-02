import java.util.HashMap;

/**
 * Common effects container. Packages contain a list of effects, keyed to 
 * certain PP values (although some may only have PP=1 defined). 
 * 
 * Packages are intended to be unique and not repeated multiple times per character,
 * although this is not strictly enforced at this time.
 * 
 * @author Vigilant
 *
 */
public class Package implements UniqueNamedData {
	private String name;
	private String description;
	private HashMap<Integer,String> effects; // effects for each PP value
	private String motivationsList; // if any
	private String specialNotes; // if any
	
	
	/**
	 * @param name Name of package
	 * @param effects Raw mechanical effects split by package
	 * @param description human form description of package (can be empty)
	 * @param motivationsList suggested motivations for package (can be empty
	 * @param specialNotes special footnotes about about package
	 */
	public Package(String name, String description, HashMap<Integer, String> effects,
			String motivationsList, String specialNotes) {
		super();
		this.name = name;
		this.description = description;
		this.effects = effects;
		this.motivationsList = motivationsList;
		this.specialNotes = specialNotes;
	}
	
	/**
	 * Returns the effects list for the given PP value
	 * @param PP positive integer matching one of the PP sets for the package
	 * @return Either a String or null if no such set exists
	 */
	public String getEffects(int PP)
	{
		return effects.get(PP);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the motivationsList
	 */
	public String getMotivationsList() {
		return motivationsList;
	}


	/**
	 * @return the specialNotes
	 */
	public String getSpecialNotes() {
		return specialNotes;
	}
	
	/**
	 * Differentiates this class from Table and such, which implement the same interface
	 * @return "package"
	 */
	public String getType()
	{
		return "package";
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the entire 
	 *
	 * @return TreeMap structure of the effects
	 */
	public HashMap<Integer, String> getEffectsTree() {
		return effects;
	}

	/**
	 * @param effects the effects to set
	 */
	public void setEffects(HashMap<Integer, String> effects) {
		this.effects = effects;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param motivationsList the motivationsList to set
	 */
	public void setMotivationsList(String motivationsList) {
		this.motivationsList = motivationsList;
	}

	/**
	 * @param specialNotes the specialNotes to set
	 */
	public void setSpecialNotes(String specialNotes) {
		this.specialNotes = specialNotes;
	}
	
	
	
}
