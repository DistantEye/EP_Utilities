/**
 * Container for Aptitudes, a characters primary stat type
 * @author Vigilant
 */
public class Aptitude {
	private String name;
	private int value;
	// APT_MAX normally 30 (EP Core 122), 40 possible with Exceptional Aptitude (EP Core 146)
	public static final int APTITUDE_MAX = 40;
	public static String[] aptitudes = {"COG","COO","INT","REF","SAV","SOM","WIL"};
	
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
	
}
