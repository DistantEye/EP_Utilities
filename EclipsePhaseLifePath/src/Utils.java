import java.util.HashMap;

/**
 * 
 */

/**
 * @author Vigilant
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
	
	public static String joinStr(String[] arr, String joiner)
	{
		if (arr.length == 0)
		{
			return "";			
		}
		
		String result = arr[0];
		
		for (int x = 1; x < arr.length; x++)
		{
			result += joiner + arr[x];
		}
		
		return result;
	}
	
	public static String joinStr(String[] arr)
	{
		return joinStr(arr,"\n");
	}
		
	/**
	 * Splits a string of commands, making sure to respect nesting if () exist with delimeters inside
	 * This version assumes the delimeter is a comma
	 * 
	 * @param input string to search
	 * @return String[] of the commands, may be a singleton if the delimiter doesn't exist in string
	 */
	public static String[] splitCommands(String input)
	{
		return Utils.splitCommands(input,",");
	}
		
	/**
	 * Splits a string of commands, making sure to respect nesting if () exist with delimeters inside
	 * 
	 * @param input string to search
	 * @return String[] of the commands, may be a singleton if the delimiter doesn't exist in string
	 */
	public static String[] splitCommands(String input, String delimiter)
	{
		// Commands have parentheses, like +morph(randomRoll). We temporarily replace all the () content, then replace the delimeters with something else unique,
		// put things back, then split everything
		
		// this is not the highest performance solution, but this function can easily be replaced with a more efficient solution later
		// prototyping is important and this will get the job done
		
		String modInput = input;
		HashMap<String,String> replaceValues = new HashMap<String,String>();
		
		String key = findUniqueDelimeter(modInput);
		int cnt = 0;
		
		String output = returnStringInParen(modInput);
		
		do
		{
			modInput = modInput.replace(output, key+cnt);
			replaceValues.put(key+cnt, output);
			cnt++;
		} while (output != "");
		
		String newDelimiter = findUniqueDelimeter(input+modInput); // if it isn't unique to both the temporary and the original, this won't work 
		
		modInput = modInput.replaceAll(delimiter, newDelimiter);
		
		for (String k : replaceValues.keySet())
		{
			modInput = modInput.replace(k, replaceValues.get(k));
		}
		
		return modInput.split(newDelimiter);
	}
	
	/**
	 * Searches the string and returns a character or string that isn't present anywhere in the string,
	 * 
	 * @param input
	 * @return
	 */
	public static String findUniqueDelimeter(String input)
	{		
		int cnt = 0;
		String result = "~0~";
		
		while (input.contains(result)) 
		{
			result = "~" + cnt++ + "~";
		} 
		
		return result;
	}
	
	/**
	 * Shortcut method for returnStringInTokensStk. Returns the string inside the most outer parentheses
	 * @param input string to search
	 * @return Matching input string inside outer parenthesis, or "" if there is no match found
	 */
	public static String returnStringInParen(String input)
	{
		return returnStringInTokensStk("(",")",input,0);
	}
	
	/**
	 * Shortcut method for returnStringInTokensStk. Returns the string inside the most outer parentheses
	 * @param input string to search
	 * @param index index to start searching at
	 * @return Matching input string inside outer parenthesis, or "" if there is no match found
	 */
	public static String returnStringInParen(String input, int index)
	{
		return returnStringInTokensStk("(",")",input,index);
	}
	
	/**
	 * Returns the inside of text enclosed by startToken and endToken, e.g returnStringInTokensStk("(",")","ab(de)fghi") => "de"
	 * This will respect nesting, returning the outermost match
	 * 
	 * @param startToken Opening tag to look for
	 * @param endToken Closing tag to look for
	 * @param input Input string
	 * @param startIndex non-negative integer, the index to start looking at
	 * @return Matching input stream inside startToken and endToken, or "" if there is no match found
	 */
	public static String returnStringInTokensStk(String startToken, String endToken, String input, int startIndex) {
//		int inputLen = input.size();
		int startOuter = input.indexOf(startToken,startIndex);
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
	
	/**
	 * Shortcut function of returnStringInTokensStk that assumes an xml type <tag></tag> for start and end tokens
	 * @param tagName Tag to look for, without the <>
	 * @param input Input string
	 * @param startIndex index to start looking at
	 * @return
	 */
	public static String returnStringInTag(String tagName, String input, int startIndex)
	{
		String open = "<" + tagName + ">";
		String close = "</" + tagName + ">";
		return returnStringInTokensStk(open,close,input,startIndex);
	}
}
