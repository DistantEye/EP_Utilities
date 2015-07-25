/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class Aptitude {
	private String name;
	private int value;
	public static final int APTITUDE_MAX = 40;
	
	/**
	 * @param name
	 * @param value
	 */
	public Aptitude(String name, int value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String toString()
	{
		return this.name + " " + this.value;
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
	}
	
	public void addValue(int value) {
		this.value += value;
	}
	
	public void subValue(int value) {
		this.value -= value;
	}
	
	
}
