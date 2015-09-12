package com.github.distanteye.ep_utils.core;
import java.util.HashMap;

/**
 * General Utility class, mainly focusing on String management. It's under investigation
 * whether some or all of this might be replaced by a common library
 * 
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
	
	/**
	 * Returns the number of tabs specified
	 * @param num Positive integer
	 * @return Returns "\t" repeated num times.
	 */
	public static String tab(int num)
	{
		String result = "";
		for (int i = 0; i < num; i++)
		{
			result += "\t";
		}
		
		return result;
	}
	
	/**
	 * Version of joinStr that takes a plain Object[], calling the toString on each
	 * entry to make it into a String[], then passes it to joinStr. This is slower.
	 * @param arr Object[], where the objects have a meaningful toString() conversion
	 * @param joiner Delimeter to separate entries in the array for the String 
	 * @return Will return "" if empty, else, a single String joining all of arr's values together between the joiner passed
	 */
	public static String joinStrObjArr(Object[] arr, String joiner)
	{
		String[] tempArr = new String[arr.length];
		
		int idx = 0;
		for (Object o : arr)
		{
			// not all values in array would need to be initialized, careful with that.
			if (o != null)
			{
				tempArr[idx] = o.toString();
			}
			
			idx++;
		}
		
		return joinStr(tempArr, joiner);
	}
	
	/**
	 * Joins String[] into single String, separated by String joiner
	 * @param arr valid String[]
	 * @param joiner Delimeter to separate entries in the array for the String
	 * @return Will return "" if empty, else, a single String joining all of arr's values together between the joiner passed
	 */
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
	
	/**
	 * Joins String[] into single String, separated by newlines 
	 * @param arr valid String[]
	 * @return Will return "" if empty, else, a single String joining all of arr's values together between newlines
	 */
	public static String joinStr(String[] arr)
	{
		return joinStr(arr,"\n");
	}
		
	/**
	 * Like split, but will only split around the first deliminter found, so splitOnce("a,b,c,d",",") = {"a","b,c,d"}
	 * @param input Input string to split on
	 * @param delimiter delimiter to search for
	 * @return String[] of the split Strings. May be a length 0 array if the delimeter does not exist at all
	 */
	public static String[] splitOnce(String input, String delimiter)
	{
		if (input.contains(delimiter))
		{
			int idx = input.indexOf(delimiter);
			return new String[] {input.substring(0, idx), input.substring(idx+delimiter.length())};
		}
		else
		{
			return new String[] {input};
		}
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
	 * @param delimiter delimiter to search for
	 * @return String[] of the commands, may be a singleton if the delimiter doesn't exist in string
	 */
	public static String[] splitCommands(String input, String delimiter)
	{	
		String modInput = input;
		String escapeCharacter = "\\"+delimiter;
		String escapeReplace = findUniqueDelimiter(modInput);
		boolean usingEscapes = false;
		
		if (modInput.contains(escapeCharacter))
		{
			// this causes odd behavior if we do it when there's no escape characters
			usingEscapes = true;
			modInput = modInput.replace(escapeCharacter, escapeReplace); // specially hide away escape chars for right now			
		}		
		
		// Commands have parentheses, like +morph(randomRoll). We temporarily replace all the () content, then replace the delimeters with something else unique,
		// put things back, then split everything
		
		// this is not the highest performance solution, but this function can easily be replaced with a more efficient solution later
		// prototyping is important and this will get the job done
				
		
		HashMap<String,String> replaceValues = new HashMap<String,String>();
		
		String key = findUniqueDelimiter(modInput);
		int cnt = 0;
		
		String output = "(" + returnStringInParen(modInput) + ")";
		
		while (!output.equals("()")) {
			modInput = modInput.replace(output, key+cnt+"~"); // adding the extra ~ prevents match collisons between ~0~1 and ~0~10 and stuff like that 
			replaceValues.put(key+cnt+"~", output);
			cnt++;
			output = "(" + returnStringInParen(modInput) + ")";
		} 
		
		String newDelimiter = findUniqueDelimiter(input+modInput); // if it isn't unique to both the temporary and the original, this won't work 
		
		modInput = modInput.replaceAll(delimiter, newDelimiter);
		
		for (String k : replaceValues.keySet())
		{
			modInput = modInput.replace(k, replaceValues.get(k));
		}
		
		// the below causes odd behavior if it's done unnecessarily
		if (usingEscapes)
		{
			modInput = modInput.replace(escapeReplace, delimiter); // this puts back the escapeCharacters we stashed away
		}
		
		return modInput.split(newDelimiter);
	}
	
	/**
	 * Searches the string and returns a character or string that isn't present anywhere in the string,
	 * 
	 * @param input
	 * @return
	 */
	public static String findUniqueDelimiter(String input)
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
			
			// we have to put the end condition first because in cases where endToken==startToken, it'd resolve poorly otherwise
			if (input.substring(i,i+endTokenLen).equals(endToken) ) {
				if (numEndsToSkip == 0) {
					endInner = i;
					break;
				}
				else {
					numEndsToSkip--;
				}
			}
			else if (input.substring(i,i+startTokenLen).equals(startToken)) {
				numEndsToSkip++;
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
