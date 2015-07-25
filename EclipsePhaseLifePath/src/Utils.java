/**
 * 
 */

/**
 * @author rob
 *
 */
public class Utils {

	public static boolean isInteger(String str) {
	    try {
	        Integer.parseInt(str);
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}
	
	public static String joinStr(String[] arr)
	{
		if (arr.length == 0)
		{
			return "";			
		}
		
		String result = arr[0];
		
		for (int x = 1; x < arr.length; x++)
		{
			result += "\n" + arr[x];
		}
		
		return result;
	}
	
	/**
	 * Returns the inside of text enclosed by startToken and endToken, e.g returnStringInTokensStk("(",")","ab(de)fghi") => "de"
	 * This will respect nesting, returning the outermost match
	 * 
	 * @param startToken Opening tag to look for
	 * @param endToken Closing tag to look for
	 * @param input Input stream
	 * @return Matching input stream inside startToken and endToken, or "" if there is no match found
	 */
	public static String returnStringInTokensStk(String startToken, String endToken, String input) {
//		int inputLen = input.size();
		int startOuter = input.indexOf(startToken);
		int endInner = -1;
		int numEndsToSkip = 0;
		int startTokenLen = startToken.length();
		int endTokenLen = endToken.length();

		if (startOuter == -1)
		{
			return "";
		}
		
		for (int i = startOuter+1; i+(startTokenLen-1) < input.length() && i+(endTokenLen-1) < input.length(); i++) {
			if (input.substring(i,i+startTokenLen).equals(startToken)) {
				numEndsToSkip++;
			}
			else if (input.substring(i,i+endTokenLen).equals(endToken) ) {
				if (numEndsToSkip == 0) {
					endInner = i;
				}
				else {
					numEndsToSkip--;
				}
			}
		}

		// invalid matching of tokens o or endToken otherwise
		if (endInner == -1)
		{
			return "";
		}
		
		int startInner = startOuter + startToken.length();

		
		
		String result = input.substring(startInner,endInner);
		return result;
	}
}
