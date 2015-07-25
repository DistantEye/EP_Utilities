import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class Table implements UniqueNamedData {
	public static final String wildCard = "!!X!!";
	
	private String name;
	protected int diceRolled;
	protected ArrayList<TableRow> rows;
	
	/**
	 * @param name The name of the table
	 * @param diceRolled The number of sides on the dice to roll for this table
	 * @param rows The list of rows this table has and their effects
	 */
	public Table(String name, int diceRolled, ArrayList<TableRow> rows) {
		super();
		this.name = name;
		this.diceRolled = diceRolled;
		this.rows = rows;
	}
	
	public String toString()
	{
		String result = "name\n";
		
		for (TableRow row : rows)
		{
			result += row.toString() + "\n";
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
	
	/**
	 * @return the name
	 */
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
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	/**
	 * @return the rows
	 */
	public ArrayList<TableRow> getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	public void setRows(ArrayList<TableRow> rows) {
		this.rows = rows;
	}

	/**
	 * @return the diceRolled
	 */
	public int getDiceRolled() {
		return diceRolled;
	}

	/**
	 * @param diceRolled the diceRolled to set
	 */
	public void setDiceRolled(int diceRolled) {
		this.diceRolled = diceRolled;
	}
	
	
	
	
}
