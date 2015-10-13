package com.github.distanteye.ep_utils.core;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * General Utility class, mainly focusing on String management. Most entries here are meant to be short consolidations of frequent
 * actions/calls.
 * 
 * @author Vigilant
 *
 */
public class Utils {
	
	/**
	 * Checks whether elem is a tag equal to String name, throwing an error if it isn't
	 * Placed here for code brevity and reuse
	 * @param elem Valid XML element
	 * @param name Name of the tag to match against elem. Case insensitive
	 */
	public static void verifyTag(Element elem, String name)
	{
		if (!elem.getName().equalsIgnoreCase(name))
		{
			throw new IllegalArgumentException("Tag is wrong type! Expected (" + name + "), found (" + elem.getName() + ")");
		}
	}
	
	/**
	 * Checks whether elem has children in the array passed, throwing error if it doesn't.
	 * Placed here for code brevity and reuse
	 * @param elem Valid XML element
	 * @param name children Array of strings equal to the child element names to look for
	 */
	public static void verifyChildren(Element elem, String[] children)
	{
		for (String child : children)
		{
			if (elem.getChild(child) == null)
			{
				throw new IllegalArgumentException("Tag (" + elem.getName() + "), is missing child element (" + child + ")");
			}
		}
	}
	
	/**
	 * Checks whether elem has Attribute of String name, throwing error if it doesn't
	 * Placed here for code brevity and reuse
	 * @param elem Valid XML element
	 * @param name Name of the tag to match against elem. Case insensitive
	 */
	public static void verifyAttr(Element elem, String name)
	{
		if (elem.getAttribute(name) == null)
		{
			throw new IllegalArgumentException("Tag (" + elem.getName() + "), is missing tag attribute (" + name + ")");
		}
	}
	
	/**
	 * Handles the conversion from xml String to JDOM Document, also performing the necessary error catch
	 * @param xml Valid xml String
	 * @return JDOM xml Document.
	 */
	public static Document getXMLDoc(String xml)
	{
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		
		try {
			document = (Document) builder.build(new ByteArrayInputStream(xml.getBytes()));
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		
		return document;
	}
	
	/**
	 * Handles the conversion from xml String to JDOM Document and returns the root element.
	 * Calls getXMLDoc
	 * 
	 * @param xml Valid xml String
	 * @return Root Element for the structure contained in String sml
	 */
	public static Element getRootElement(String xml)
	{
		Element tmp = getXMLDoc(xml).getRootElement();
		tmp.detach();
		
		return tmp;
	}
	
	
	public static String elemToString(Element e)
	{
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(Format.getPrettyFormat().setOmitDeclaration(true));
		
		return xmlOut.outputString(e);
	}

	// Still using this because StringUtils.isNumber returns true for Floats
	public static boolean isInteger(String str) {
	    try {
	        Integer.parseInt(str);
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
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
		
		String output = "(" + stringInParen(modInput) + ")";
		
		while (!output.equals("()")) {
			modInput = modInput.replace(output, key+cnt+"~"); // adding the extra ~ prevents match collisons between ~0~1 and ~0~10 and stuff like that 
			replaceValues.put(key+cnt+"~", output);
			cnt++;
			output = "(" + stringInParen(modInput) + ")";
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
	public static String stringInParen(String input)
	{
		return stringInNestedTokens("(",")",input,0);
	}
	
	/**
	 * Shortcut method for returnStringInTokensStk. Returns the string inside the most outer parentheses
	 * @param input string to search
	 * @param index index to start searching at
	 * @return Matching input string inside outer parenthesis, or "" if there is no match found
	 */
	public static String stringInParen(String input, int index)
	{
		return stringInNestedTokens("(",")",input,index);
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
	public static String stringInNestedTokens(String startToken, String endToken, String input, int startIndex) {
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
	public static String stringInTag(String tagName, String input, int startIndex)
	{
		String open = "<" + tagName + ">";
		String close = "</" + tagName + ">";
		return stringInNestedTokens(open,close,input,startIndex);
	}
}
