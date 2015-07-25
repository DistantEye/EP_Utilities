/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class UnitTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String foo = "a(123(((4)))567)";
		System.out.println(Utils.returnStringInTokensStk("(", ")", foo));
	}

}
