/**
 * 
 */

/**
 * @author Vigilant
 *
 * Make sure anything in the DataFileProcessor has getName and getType
 */
public interface UniqueNamedData {
	public String getName();
	public String getType(); // returns Table, Package, etc
}
