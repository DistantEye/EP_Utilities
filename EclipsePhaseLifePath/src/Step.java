/**
 * Container meant to encapsulate different linked steps in a generation process
 * Not strictly necessary, but makes intelligent navigation and management possible without forcing it into a general table
 */

/**
 * @author Vigilant
 *
 */
public class Step implements UniqueNamedData {
	private String name;
	private String nextStep;
	private String effects; // effects
	
	
	/**
	 * @param name Name of function
	 * @param nextStep the step after this one
	 * @param effects Raw mechanical effect
	 */
	public Step(String name, String nextStep, String effects) {
		super();
		this.name = name;
		this.effects = effects;
		this.nextStep = nextStep;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the nextStep
	 */
	public String getNextStep() {
		return nextStep;
	}

	public String toString()
	{
		return DataProc.effectsToString(this.effects);
	}
	
	/**
	 * Differentiates this class from Table and such, which implement the same interface
	 * @return "function"
	 */
	public String getType()
	{
		return "step";
	}

	/**
	 * Return the effects this function contains
	 * @return The functions effects
	 */
	public String getEffects()
	{
		return this.effects;
	}
	
	
}
