import java.util.ArrayList;

/**
 * Tables store a list of effects, with contiguous number ranges keyed to each row,
 * as well as a max number for the end of the range set.
 * 
 * Table can be "rolled" in this way, providing the diceRolled number to an rng, and returning
 * a TableRow with the effects matching a particular choice.
 * 
 * There is also support for wildcards, when a Table's effect might need some prior context
 * 
 * @author Vigilant
 *
 */
public class Table implements UniqueNamedData {
	public static final String wildCard = "!!X!!";
	
	private String name;
	protected int diceRolled;
	protected ArrayList<TableRow> rows;
	private boolean suppressDescriptions;
	
	/**
	 * @param name The name of the table
	 * @param diceRolled The number of sides on the dice to roll for this table
	 * @param rows The list of rows this table has and their effects
	 * @param suppressDescriptions boolean flag. If false, this table will generally not automatically echo to the UI the description field
	 * 			for rows when rolled/ran
	 */
	public Table(String name, int diceRolled, ArrayList<TableRow> rows, boolean suppressDescriptions) {
		super();
		this.name = name;
		this.diceRolled = diceRolled;
		this.rows = rows;
		this.suppressDescriptions = suppressDescriptions;
	}
	
	/**
	 * Returns a String with the Table name, and all the toString of the rows, containing the lowRange, highRange, and Row effects converted to String
	 */
	public String toString()
	{
		String result = this.getName() + "\n";
		
		for (TableRow row : rows)
		{
			result += row.toString() + "\n";
		}
		
		return result;
	}
	
	/**
	 * Returns a String with the Table name, and all the toString of the rows, containing the lowRange, highRange, and Row description
	 */
	public String toStringDescription()
	{
		String result = this.getName() + "\n";
		
		for (TableRow row : rows)
		{
			result += row.toStringDescription() + "\n";
		}
		
		return result;
	}
	
	/**
	 * Returns whether anything in this table needs a wildcard specified to run 
	 * 
	 * @return True if any TableRow has a wildcard effect, false otherwise
	 */
	public boolean containsWildCards()
	{
		for (TableRow row : rows)
		{
			if (row.getEffects().contains(wildCard))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Searches through all rows in the table and returns the first one that matches the number
	 * @param roll non-negative integer
	 * @return Matching TableRow, or null
	 */
	public TableRow findMatch(int roll)
	{
		for (TableRow row : rows)
		{
			if (row.isMatch(roll)) 
			{
				return row;
			}
		}
		
		return null;
	}
	
	/**
	 * Searches through all rows in the table and returns the first one that matches the number.
	 * Will replace any wildcards with the parameter passed
	 * 
	 * @param roll non-negative integer
	 * @param replaceStr String to replace wildcards with
	 * @return Matching TableRow, or null
	 */
	public TableRow findMatch(int roll, String replaceStr)
	{
		TableRow temp = this.findMatch(roll);
		
		if (temp == null)
		{
			return null;
		}
		else
		{
			return temp.getCopy(wildCard, replaceStr);
		}
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Differentiates this class from Packages and such, which implement the same interface
	 * @return "table"
	 */
	public String getType()
	{
		return "table";
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public int getDiceRolled() {
		return diceRolled;
	}

	public void setDiceRolled(int diceRolled) {
		this.diceRolled = diceRolled;
	}

	public boolean isSuppressDescriptions() {
		return suppressDescriptions;
	}

	public void setSuppressDescriptions(boolean suppressDescriptions) {
		this.suppressDescriptions = suppressDescriptions;
	}
	
	
	
	
}
