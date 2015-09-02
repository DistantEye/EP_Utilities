/**
 * Container meant to encapsulate different linked steps in a generation process
 * Not strictly necessary, but makes intelligent navigation and management possible without forcing it 
 * into a Table or Function
 * 
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
	
	public String getName() {
		return name;
	}
	
	public String getNextStep() {
		return nextStep;
	}

	public String toString()
	{
		return DataProc.effectsToString(this.effects);
	}
	
	/**
	 * Differentiates this class from Table and such, which implement the same interface
	 * @return "step"
	 */
	public String getType()
	{
		return "step";
	}

	/**
	 * Return the effects this Step contains
	 * @return The Step's effects
	 */
	public String getEffects()
	{
		return this.effects;
	}
	
	
}
