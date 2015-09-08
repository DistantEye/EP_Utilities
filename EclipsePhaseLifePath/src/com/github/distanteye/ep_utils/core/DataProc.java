package com.github.distanteye.ep_utils.core;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// explicit because name ambiguity
import com.github.distanteye.ep_utils.containers.*;
import com.github.distanteye.ep_utils.core.Package;

/**
 * The DataFileProcessor class serves to take commands to look up a particular table,
 * and results in the right table (from text files) being called up, and the appropriate
 * action being processed
 * 
 * @author Vigilant
 *
 */
public class DataProc {
	
	// we keep these incase later versions need to write to datafiles
	private static HashMap<String, UniqueNamedData> dataStore; // local copy of any data built by this app
	private static int fileLineNumber;
	

	/**
	 * Takes data files as parameters and builds the environment needed for char-gen to work
	 * @param lpFileName holds packages/tables/etc needed for the lifepath and/or package system
	 * @param internalDatFile holds things like skills, rep, gear, etc, each setup so that getNextChunk can parse from it
	 */
	public static void init(String lpFileName, String internalDatFile) {
		dataStore = new HashMap<String, UniqueNamedData>();
		readIntData(internalDatFile);
		readLPData(lpFileName);
		fileLineNumber = -1; // reset when we're done
	}

	
	private static void readIntData(String filename)
	{
		Path path = FileSystems.getDefault().getPath("data", filename);
		fileLineNumber = 0;
		
		Charset charset = Charset.forName("UTF-8");
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
		    buildData(reader);
		    reader.close();
		} catch (IOException x) {
		    System.err.format("Could not find file: %s%n", x);
		    System.exit(-1);
		}
	}
	
	
	
	
	// ?1? ?2? ?3? and / has to be coded to read in skills, aptitudes, skill subtypes, everything. try and make it all work
		// throw an error if you can't read something, the reading should be perfect, or we want to know about it if it's not
	
	private static void readLPData(String filename)
	{
		Path path = FileSystems.getDefault().getPath("data", filename);
		fileLineNumber = 0;
		
		Charset charset = Charset.forName("UTF-8");
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
		    buildData(reader);
		    reader.close();
		} catch (IOException x) {
		    System.err.format("Could not find file: %s%n", x);
		    System.exit(-1);
		}
	}

	/**
	 * Takes an appropriately opened reader and builds all necessary lifepath data from it
	 * @param reader Needs to be already set to a file or errors will result
	 * @throws IOException If there are any problems with the reader 
	 */
	private static void buildData(BufferedReader reader) throws IOException 
	{
		// get the next chunk of data (always separated by a long set of dashes)
		String[] chunk = getNextChunk(reader);
		
		while (chunk != null && chunk[0].length() > 0)
		{
			if (chunk[0].equalsIgnoreCase("rep") && chunk.length > 1)
			{
				for (int x = 1; x < chunk.length; x++)
				{
					Rep.addRepType(chunk[x]);
				}
			}
			else if (chunk[0].equalsIgnoreCase("skill") && chunk.length > 1)
			{
				for (int x = 1; x < chunk.length; x++)
				{
					Skill.CreateInternalSkill(chunk[x]);
				}
			}
			else if (chunk[0].equalsIgnoreCase("package") && chunk.length > 1)
			{
				addPackage(chunk);
			}
			else if (chunk[0].equalsIgnoreCase("morph") && chunk.length > 1)
			{
				addMorph(chunk);
			}
			else if (chunk[0].equalsIgnoreCase("table") && chunk.length > 1)
			{
				addTable(chunk);
			}
			else if (chunk[0].equalsIgnoreCase("function") && chunk.length > 1)
			{
				addFunction(chunk);
			}
			else if (chunk[0].equalsIgnoreCase("trait") && chunk.length > 1)
			{
				addTrait(chunk);
			}
			else if (chunk[0].equalsIgnoreCase("sleight") && chunk.length > 1)
			{

				addSleight(chunk);
			
			}
			else if (chunk[0].equalsIgnoreCase("step") && chunk.length > 1)
			{

				addStep(chunk);
			
			}
			else if (chunk[0].equalsIgnoreCase("charvars") && chunk.length > 1)
			{
				for (int i = 1; i< chunk.length; i++)
				{
					String[] parts = chunk[i].split(":");
					if (parts.length == 2)
					{
						EpCharacter.charConstants.put(parts[0], parts[1]);
					}
					else
					{
						throw new IllegalArgumentException("Improperly formated charvar at line : " + fileLineNumber + " (" + chunk[i] +")");
					}
				}
			}
			else
			{
				throw new IllegalArgumentException("Unknown type of data or incorrect formating at line : " + fileLineNumber);
			}
			
			chunk = getNextChunk(reader);
		}
		
	}
	
	/**
	 * Takes an appropriately opened reader and returns the next batch of lines until
	 * a long trail of dashes is read (signifying the end of the dash)
	 * @param reader Needs to be already set to a file or errors will result
	 * @return Either an array of Strings (lines), or null, if no more exists on the file
	 * @throws IOException If there are any problems with the reader
	 */
	private static String[] getNextChunk(BufferedReader reader) throws IOException
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		
		String line = reader.readLine();
		fileLineNumber++;
		// this will always chop off the next ---------- type line found at the end by reading and then discarding it
		// so the next time getNextChunk is called it never sees that line
		while (line != null && !line.startsWith("----------------") )
		{
			if (!line.startsWith("//"))
			{
				lines.add(line);
			}
			
			line = reader.readLine();
			fileLineNumber++;
		}
		
		if (lines.size() < 2 ) // The lines must exist and be at least one line long
		{
			return null;
		}
		
		return lines.toArray(new String[lines.size()]);
	}
	
	/**
	 * Adds Package to the system
	 * Format:
	 * Line 1 : PACKAGE
	 * Line 2 : The package's name
	 * Line 3 : Human readable description of package
	 * Line 4+ : Rest lines follow format PP;NUM>effects  , where NUM is a positive integer and effects is a valid effects string
	 * Line * : A line at the end starting with * can be used to add special contextual notes to the package
	 * @param lines
	 */
	private static void addPackage(String[] lines)
	{
		// first line just says package, we can ignore
		
		// second line is package name
		String name = lines[1];
		
		// third line is human text description (can be empty)
		String desc = lines[2];
		
		// fourth line is the suggested motivations (can be empty)
		String motiv = lines[3];
		
		// remaining lines are the different effects by package PP, some packages only have PP1, others have PP1,PP3, PP5, etc
		int i = 4;
		String line = lines[i];
		HashMap<Integer,String> effectsList = new HashMap<Integer,String>();
		
		while (line.startsWith("PP;"))
		{
			if (!line.matches("PP;[0-9]*>[^>]*"))
			{
				throw new IllegalArgumentException("Incorrect format on effects : " + line);
			}
			
			String[] parts = line.split(">");
			parts[0] = parts[0].replace("PP;", "");
			
			if (!Utils.isInteger(parts[0]))
			{
				throw new IllegalArgumentException("Incorrect format on effects : " + line);
			}
			
			int ppValue = Integer.parseInt(parts[0]);
			
			if (ppValue <= 0)
			{
				throw new IllegalArgumentException("Incorrect format on effects (PP value must be > 0 ): " + line);
			}
			
			String effects = parts[1];
			
			effectsList.put(ppValue, effects);
			
			i++;
			
			if (i+1 > lines.length)
			{
				line = "";
			}
			else
			{
				line = lines[i];
			}
		}
		
		// either this will be blank or it will contain the special notes line
		// because the loop replaces line with "" if it detects the end of the chunk
		String specialNotes = line; 
		
		if (!effectsList.containsKey(1))
		{
			throw new IllegalArgumentException("Incorrect format on package (must contain PP;1 package " + name);
		}
		
		dataStore.put(name, new Package(name,desc,effectsList,motiv,specialNotes));
		
	}
	
	/**
	 * Adds table to the system
	 * Format:
	 * Line 1 : TABLE
	 * Line 2 : The table's name
	 * Line 3 : Integer value, the size of the dice to roll for the table
	 * Line 4 : Suppress printing description <true/false> . When table is run, the default behavior for some UI is to always print description text
	 * 			but this is not always desirable.
	 * Line 5 : Rest lines follow format range`description`effects
	 * 
	 * @param lines List of lines comprising the table to be read in
	 */
	private static void addTable(String[] lines)
	{
		// first line just says table, we can ignore
		
		// second line is table name
		String name = lines[1];
		boolean suppressDescriptions = false; 
		
		// third line is the size dice to roll for the table (integer)
		if (!Utils.isInteger(lines[2]))
		{
			throw new IllegalArgumentException("3rd line of table must be a number : " + name);
		}
		int diceSides = Integer.parseInt(lines[2]);
		
		// prefer case insensitive where-ever possible because users often don't match case
		// check the suppressDescriptions flag for true or false
		if (lines[3].equalsIgnoreCase("true"))
		{
			suppressDescriptions = true;
		}
		else if (lines[3].equalsIgnoreCase("false"))
		{
			suppressDescriptions = false;
		}
		else
		{
			throw new IllegalArgumentException("4th line of table must be either true or false : " + lines[2] );
		}
		
		
		ArrayList<TableRow> rows = new ArrayList<TableRow>();
		
		for (int x = 4; x < lines.length; x++)
		{
			String line = lines[x];
			// range`description`effects
			if (!line.matches("^[0-9]*(-[0-9]*)?`[^`]*`[^`\n]*"))
			{
				throw new IllegalArgumentException("Incorrect format on TableRow : " + line);
			}
			
			String[] parts = line.split("`");
			String[] range = parts[0].split("-");
			int lowRange = Integer.parseInt(range[0]);
			
			int highRange = -1; // placeholder
			
			if (range.length == 2)
			{
				highRange = Integer.parseInt(range[1]);
			}
			else if ( range.length == 1)
			{
				highRange = lowRange;
			}
			else
			{
				throw new IllegalArgumentException("Incorrect format on TableRow (couldn't parse lowRange/HighRange) : " + line);
			}
			
			TableRow temp = new TableRow(lowRange,highRange, parts[1], parts[2]);
			
			rows.add(temp);
		}
		
		if (rows.size() == 0)
		{
			throw new IllegalArgumentException("Table must have at least one row : " + name);
		}
		
		dataStore.put(name, new Table(name, diceSides, rows,suppressDescriptions));
	}
	
	
	/**
	 * Adds table to the system
	 * Format:
	 * Line 1 : FUNCTION
	 * Line 2 : The functions's name
	 * Line 3 : Effects string to sub in wherever the function is called
	 * 
	 * @param lines List of lines comprising the table to be read in
	 */
	private static void addFunction(String[] lines)
	{
		// first line just says table, we can ignore
		
		// second line is table name
		String name = lines[1];
		
		if (lines.length < 3 || lines[2].length() == 0)
		{
			throw new IllegalArgumentException("Function must have an effects row : " + name);
		}
		
		dataStore.put(name, new Function(name, lines[2]));
	}	
	/**
	 * Adds sleights to the system
	 * Format (XML):
	 * 
	 * <sleight>
		<sleightType></sleightType>
		<exsurgentOnly></exsurgentOnly>
		<name></name>
		<activePassive></activePassive>
		<actionType></actionType>
		<range></range>
		<duration></duration>
		<strainMod></strainMod>
		<skillUsed></skillUsed>
		<desc></desc>
		</sleight>
	 * 
	 * @param lines List of lines comprising the Sleight to be read in
	 */
	private static void addSleight(String[] lines)
	{
		int startIdx = 0;
		
		// we rejoin into a single string for XML parsing
		String chunkStr = Utils.joinStr(lines);
		
		while ( true )
		{
			startIdx = chunkStr.indexOf("<sleight>", startIdx); // we find the next valid Trait
			
			if (startIdx == -1)
			{
				break; // no more matches exist
			}
			
			String nextSleight = Utils.returnStringInTag("sleight", chunkStr, startIdx);
			
			String sleightType = Utils.returnStringInTag("sleightType", nextSleight, 0);
			String isExsurgent = Utils.returnStringInTag("exsurgentOnly", nextSleight, 0);
			String sleightName = Utils.returnStringInTag("name", nextSleight, 0);
			String activePassive = Utils.returnStringInTag("activePassive", nextSleight, 0);
			String actionType = Utils.returnStringInTag("actionType", nextSleight, 0);
			String range = Utils.returnStringInTag("range", nextSleight, 0);
			String duration = Utils.returnStringInTag("duration", nextSleight, 0);
			String strainMod = Utils.returnStringInTag("strainMod", nextSleight, 0);
			String skillUsed = Utils.returnStringInTag("skillUsed", nextSleight, 0);
			String description = Utils.returnStringInTag("desc", nextSleight, 0);
			
			Sleight.CreateInternalsleight(new String[]{sleightType,isExsurgent,sleightName,activePassive,actionType,range,duration,strainMod,skillUsed,description});
			
			startIdx++;
		} 
		
		
	}
	
	/**
	 * Adds Trait
	 * Format (XML)
	 * 
	 * <trait>
	 * <name></name>
	 * <cost></cost>
	 * <bonus></bonus>
	 * <description></description>
	 * </trait>
	 * 
	 * @param lines List of lines comprising the Trait to be read in
	 */
	private static void addTrait(String[] lines)
	{
		int startIdx = 0;
		
		// we rejoin into a single string for XML parsing
		String chunkStr = Utils.joinStr(lines);
		
		while ( true )
		{
			startIdx = chunkStr.indexOf("<trait>", startIdx); // we find the next valid Trait
			
			if (startIdx == -1)
			{
				break; // no more matches exist
			}
			
			String nextTrait = Utils.returnStringInTag("trait", chunkStr, startIdx);
			
			String name = Utils.returnStringInTag("name", nextTrait, 0);
			String cost = Utils.returnStringInTag("cost", nextTrait, 0);
			String bonus = Utils.returnStringInTag("bonus", nextTrait, 0);
			String desc = Utils.returnStringInTag("description", nextTrait, 0);

			Trait.CreateInternalTrait(name,desc,cost,bonus,1);
			startIdx++;
		} 
	}
	
	/**
	 * Adds Morph Block.
	 * Format pseudo XML : one line of morph starts the block,
	 * then there is a stream of tags per the below (not all tags are required present)
	 * 
	 * <morph>
	 * <type></type>
	 * <name></name>
	 * <Desc></Desc>
	 * <Implants></Implants>
	 * <Aptitude Maximum></Aptitude Maximum>
	 * <Durability></Durability>
	 * <Wound Threshold></Wound Threshold>
	 * <Advantages></Advantages>
	 * <CP Cost></CP Cost>
	 * <Credit Cost></Credit Cost>
	 * <Notes></Notes>
	 * </morph>
	 * 
	 * @param lines List of lines comprising the Trait to be read in
	 */
	private static void addMorph(String[] lines)
	{
		// we use no divider between lines because it will make the tag parsing actually go slightly easier.
		String lineStream = Utils.joinStr(lines,"");
		int idx = 0;
		
		String nextMorph = Utils.returnStringInTokensStk("<morph>", "</morph>", lineStream, idx);
		String fullMorphBlock = "<morph>" + nextMorph + "</morph>";
		
		// this sets idx to next time start looking right after when the past block ended
		idx = lineStream.indexOf(fullMorphBlock,idx) + fullMorphBlock.length();
		
		while (nextMorph.length() != 0)
		{				
			String name = Utils.returnStringInTag("name",nextMorph,0);
			String morphType = Utils.returnStringInTag("type",nextMorph,0);
			String description = Utils.returnStringInTag("Desc",nextMorph,0);
			String implants = Utils.returnStringInTag("Implants",nextMorph,0);
			String aptitudeMaxStr = Utils.returnStringInTag("Aptitude Maximum",nextMorph,0);			
			String durStr = Utils.returnStringInTag("Durability",nextMorph,0);
			String woundThrStr = Utils.returnStringInTag("Wound Threshold",nextMorph,0);
			String cpStr = Utils.returnStringInTag("CP Cost",nextMorph,0);
			
			// while not strictly necessary, we make sure all of the following values parse into integers, because they should logically be ints.
			// it may cause problems elsewhere otherwise.
			int durability;
			
			if (durStr.length() == 0)
			{
				durability = 0;
			}
			else if (Utils.isInteger(durStr))
			{
				durability = Integer.parseInt(durStr);
			}
			// sometimes we have something like 50 (includes implants), which we want to parse just to 50
			else if (Utils.isInteger(durStr.split(" ")[0]))
			{
				durability = Integer.parseInt(durStr.split(" ")[0]);
			}		
			else
			{
				throw new IllegalArgumentException("Durability isn't parseable as a number for morph: " + name);
			}
			
			int woundThreshold;
			
			if (woundThrStr.length() == 0)
			{
				woundThreshold = 0;
			}
			else if (Utils.isInteger(woundThrStr))
			{
				woundThreshold = Integer.parseInt(woundThrStr);
			}
			// sometimes we have something like 50 (includes implants), which we want to parse just to 50
			else
			{
				throw new IllegalArgumentException("Would Threshold isn't parseable as a number for morph: " + name);
			}
			
			int CP;
			
			if (cpStr.length() == 0)
			{
				CP = 0;
			}
			else if (cpStr.toLowerCase().contains("not available"))
			{
				CP = -1;
			}
			else if (Utils.isInteger(cpStr))
			{
				CP = Integer.parseInt(cpStr);
			}
			// sometimes we have something like 50 (includes implants), which we want to parse just to 50
			else
			{
				throw new IllegalArgumentException("CP("+cpStr+") isn't parseable as a number for morph: " + name);
			}
									
			String creditCost = Utils.returnStringInTag("Credit Cost",nextMorph,0);
			String notes = Utils.returnStringInTag("Notes",nextMorph,0);
			String effects = "";
			
			// now we do some stuff to figure out the aptitude maximums
			ArrayList<String> outputList = new ArrayList<String>();
			
			if (Utils.isInteger(aptitudeMaxStr))
			{
				int max = Integer.parseInt(aptitudeMaxStr);
				for (String apt : Aptitude.TYPES)
				{
					outputList.add(apt+":"+max);
				}
			}
			else
			{
				String[] parts = aptitudeMaxStr.split(",");
				
				// build a list of all stats to cross off as we go
				ArrayList<String> aptList = new ArrayList<String>();
				for (String apt : Aptitude.TYPES)
				{
					aptList.add(apt);
				}			
				
				// we loop over each part of it which will have the different values for some of the stats. Ignore segments that contain "all", but mark that for later
				String defaultMax = "";
				for (String str : parts)
				{
					if (str.toLowerCase().contains("all others") || str.toLowerCase().contains("all else"))
					{
						defaultMax = str;
					}
					else
					{
						int max = 0;
						
						Matcher match = Pattern.compile("[0-9]+").matcher(str);
						
						if (!match.find())
						{
							throw new IllegalArgumentException("No integer value found for aptitude maximum in " + str);
						}
						else
						{
							max = Integer.parseInt(match.group());
						}
						
						for (String apt : Aptitude.TYPES)
						{
							if (str.toUpperCase().contains(apt))
							{
								outputList.add(apt+":"+max);
								aptList.remove(apt);
							}
						}
					}
				}	
						
				// now we process the defaultMax part, throwing an error if it doesn't exist
				if (defaultMax.length() == 0)
				{
					throw new IllegalArgumentException("No default 'all others' value found for aptitude maximum in " + aptitudeMaxStr);
				}
				else
				{
					int max = 0;

					Matcher match = Pattern.compile("[0-9]+").matcher(defaultMax);

					if (!match.find())
					{
						throw new IllegalArgumentException("No integer value found for aptitude maximum in " + defaultMax);
					}
					else
					{
						max = Integer.parseInt(match.group());

						for (String apt : aptList)
						{
							outputList.add(apt+":"+max);
						}
					}
				}
				
			}
			
			String aptMaxArrStr = Utils.joinStr(outputList.toArray(new String[outputList.size()]),";");
			
			String[] toAdd = {name, morphType, description, implants, aptMaxArrStr, ""+durability, ""+woundThreshold, ""+CP, creditCost, effects, notes};
			
			Morph.CreateInternalMorph(toAdd);
			
			nextMorph = Utils.returnStringInTokensStk("<morph>", "</morph>", lineStream, idx);
			fullMorphBlock = "<morph>" + nextMorph + "</morph>";
			
			// this sets idx to next time start looking right after when the past block ended
			idx = lineStream.indexOf(fullMorphBlock,idx) + fullMorphBlock.length();			
		} 
	}
	
	/**
	 * Adds step to the system (these are core 'meta' building blocks of the system that decouple logical flow from table's direct functions
	 *  
	 * Format (by line):
	 * 
	 * STEP
	 * step_name
	 * next default step
	 * effects list (follows ordinary rules)
	 * 
	 * @param lines List of lines comprising the Step to be read in
	 */
	private static void addStep(String[] lines)
	{
		if (lines.length != 4)
		{
			throw new IllegalArgumentException("Step chunk must be 4 lines long! (before line:" + fileLineNumber + ")");
		}
		
		dataStore.put(lines[1], new Step(lines[1],lines[2],lines[3]));
	}
	
	
	/**
	 * Version of 
	 * @param effects Any list of syntax correct commands
	 * @return Human readable version of the string input
	 */
//	public static String effectsToStringSingle(String effects)
//	{
//		DataFileProcessor temp = new DataFileProcessor();
//		return temp.effectsToString(effects);
//	}
	
	/**
	 * Takes a list of commands and attempts to parse them into a human friendly description
	 * @param effects Any list of syntax correct commands
	 * @return Human readable version of the string input
	 * @throw IllegalStateException if the datastore isn't intialized
	 */
	public static String effectsToString(String effects)
	{
		effects = effects.replaceAll("#[^#]+#", ""); // this tags are mostly unparseable, ignore them
		
		if (dataStore == null)
		{
			throw new IllegalStateException("Datastore not built yet!");
		}		 
		
		String result = "";
		
		String[] effectsArr = Utils.splitCommands(effects, ";"); 
		
		for (String effect : effectsArr)
		{
			String errorInfo = "";
			
			String params = Utils.returnStringInParen(effect);
			
			String commandName = "";
			if (effect.indexOf('(') > 0 )
			{
				commandName = effect.substring(0, effect.indexOf('('));
			}
			else
			{
				commandName = effect;
			}
			
			// TODO : to comply with older code, we have to insert the command at the beginning of params
			// can probably redo this at some point
			params = commandName + "," + params;
			
			// means we have no useful line info
			if (fileLineNumber == -1)
			{
				errorInfo = ": " + effect;
			}
			else
			{
				errorInfo = "at line : " + fileLineNumber;
			}
			
			String lcEffect = effect.toLowerCase();
			
			if (Skill.isSkill(effect))
			{
				result += ("Add skill : " + effect).replace("?1?", "(Choose One Skl)");;
			}
			else if (lcEffect.startsWith("incskl"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ((Skill.isSkill(subparts[1]) || DataProc.containsUncertainty(subparts[1])) && Utils.isInteger(subparts[2]))
				{
					result += "Add " + subparts[2] + " to " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				
				if (subparts.length == 4)
				{
					result += ", Conditional must be true: " + subparts[3];
				}
			}
			else if (lcEffect.startsWith("setskl"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ((Skill.isSkill(subparts[1]) || DataProc.containsUncertainty(subparts[1])) && Utils.isInteger(subparts[2]))
				{
					result += "Set skl " + subparts[2] + " to " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				
				if (subparts.length == 4)
				{
					result += ", Conditional must be true: " + subparts[3];
				}
			}
			else if (lcEffect.startsWith("decskl"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ((Skill.isSkill(subparts[1]) || DataProc.containsUncertainty(subparts[1])) && subparts[2].equalsIgnoreCase("all"))
				{
					result += "Remove skill: " + subparts[1];
				}
				else if ((Skill.isSkill(subparts[1]) || DataProc.containsUncertainty(subparts[1])) && Utils.isInteger(subparts[2]))
				{
					result += "Subtract " + subparts[2] + " to " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				
				if (subparts.length == 4)
				{
					result += ", Conditional must be true: " + subparts[3];
				}
			}
			else if (lcEffect.startsWith("sklspec"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ( (Skill.isSkill(subparts[1])|| DataProc.containsUncertainty(subparts[1])) && subparts[2].length() > 0)
				{
					result += "Add specialization(" + subparts[2] + ") to skill " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("trait"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2 && subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add trait: " + subparts[1];
					
					if (subparts.length == 3)
					{
						result += " at level("+subparts[2]+")";
					}
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("morph"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].equalsIgnoreCase("randomroll"))
				{
					result += "Roll a new random morph";
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add morph: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("setapt"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && Utils.isInteger(subparts[2]))
				{
					result += "Set aptitude: " + subparts[1] + " to " + subparts[2];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("addapt"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && Utils.isInteger(subparts[2]))
				{
					result += "Add " + subparts[2] + " to " + subparts[1];
					
					if (subparts.length == 4)
					{
						result += ", Conditional must be true: " + subparts[3];
					}
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("rolltable")  || lcEffect.startsWith("forcerolltable"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2 && subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 )
				{
					result += "Roll dice an effect from table : " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("roll") || lcEffect.startsWith("forceroll"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && Utils.isInteger(subparts[1]) && subparts[2].substring(0,subparts[2].indexOf('=')).matches("[0-9]+-[0-9]+"))
				{
					result += "Roll " + subparts[1] + " sided dice, with effects for each range : " + subparts[2];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("runtable"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (Utils.isInteger(subparts[2]))
				{
					if (! DataProc.dataObjExists(subparts[1]))
					{
						throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
					}
					
					if (! DataProc.getDataObj(subparts[1]).getType().equals("table"))
					{
						throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a table");
					}
					
					if (subparts.length == 4)
					{
						result += "Get result from table : " + subparts[1] + ", matching the number/result : " 
										+ subparts[2] + ", with the wildcard substitution" + subparts[3];
					}
					else
					{
						result += "Get result from table : " + subparts[1] + ", matching the number/result : " 
								+ subparts[2];
					}
				}
				else
				{						
					throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");											
				}			
			}
			else if (lcEffect.startsWith("mox"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (Utils.isInteger(subparts[1]))
				{
					result += "Add MOX: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("setmox"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (Utils.isInteger(subparts[1]))
				{
					result += "Set MOX: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("gear"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add gear: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("background"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add background: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("nextpath"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Set next path: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("faction"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Set faction: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("stepskip"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Skip to step: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("package"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length == 2 && subparts[1].length() > 0)
				{
					result += "Add package : " + subparts[1];
				}
				else if (subparts.length == 3 && subparts[1].length() > 0 && (Utils.isInteger(subparts[2]) || subparts[2].contains(Table.wildCard)))
				{
					result += "Add package (" + subparts[2] + "PP) : " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("rep"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && Utils.isInteger(subparts[2]))
				{
					result += "Add " + subparts[2] + " to " + subparts[1] + "-rep";
					
					if (subparts.length == 4)
					{
						result += " where condition is true : " + subparts[3];
					}
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("credit"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add credits: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("psichi"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add psi-chi sleight: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("psigamma"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add psi-gamma sleight: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("psisleight"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add psi sleight: " + subparts[1];
				}
				
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("extendedchoice"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && subparts[2].length() > 0)
				{
					result += "Player makes choice according to text : " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("if"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length < 3 || subparts.length > 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts.length == 3 && subparts[1].length() > 0 && subparts[2].length() > 0)
				{
					result += "If (" + subparts[1] + ") then " + effectsToString(subparts[2]);
				}
				else if (subparts.length == 4 && subparts[1].length() > 0 && subparts[2].length() > 0 && subparts[3].length() > 0)
				{
					result += "If (" + subparts[1] + ") then " + effectsToString(subparts[2]) + " : ELSE : " + effectsToString(subparts[3]);
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("func"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length < 2 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else
				{
					if (dataStore.containsKey(subparts[1]) && dataStore.get(subparts[1]).getType().equals("function"))
					{
						Function temp = (Function)dataStore.get(subparts[1]);
						String effectsFunc = temp.getEffect();
						
						// replace any effect parameters for this function with the passed effects
						for (int x = 2; x < subparts.length; x++)
						{
							int idx = x-1; // we start with looking to replace &1&
							effectsFunc = effectsFunc.replace("&" + idx + "&", subparts[x]);
						}
						
						if (effectsFunc.contains("func("+subparts[1]+")"))
						{
							effectsFunc = effectsFunc.replace("func("+subparts[1]+")", "dummyFunc("+subparts[1]+")");
						}
						
						return effectsToString(effectsFunc);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect, " + subparts[1] + " does not exist or is not a function" + errorInfo);
					}
				}
			}
			else if (lcEffect.startsWith("dummyfunc"))
			{	
				result += "call function: " + effect;
			}
			else if (lcEffect.startsWith("msgclient"))
			{			
				String[] subparts = Utils.splitOnce(params,",");
				if (subparts.length != 2 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ( subparts[1].length() > 0)
				{
					result += "Send message to the client : " + subparts[1].replace("\\,", ",");					
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("setvar"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ( subparts[1].length() > 0 && subparts[2].length() > 0)
				{
					result += "Set variable(" + subparts[1] + ") = " + subparts[2];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("incvar"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (!Utils.isInteger(subparts[2]))
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo + ", " + subparts[2] + " is not a number");
				}
				else if ( subparts[1].length() > 0 && subparts[2].length() > 0)
				{
					result += "Increment variable(" + subparts[1] + ") with " + subparts[2];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (lcEffect.startsWith("remvar"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ( subparts[1].length() > 0)
				{
					result += "Remove variable(" + subparts[1] + ")";
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else
			{
				throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
			}
		
			result += ",";
		}
		
		// we give nice words for up to Choose 5, because these are the most common
		result = result.replaceAll("\\?1\\?", "(Choose One)");
		result = result.replaceAll("\\?2\\?", "(Choose Two");
		result = result.replaceAll("\\?3\\?", "(Choose Three)");
		result = result.replaceAll("\\?4\\?", "(Choose Four)");
		result = result.replaceAll("\\?5\\?", "(Choose Five)");
		
		//remaining ones get ugly number based notation
		result = result.replaceAll("\\?([0-9]*)\\?", "(Choose $1)");
		
		return result;
	}
	
	/**
	 * Checks to see if the sample input contains any choice prompts, that is any symbols like ?1?, that prompt the user
	 * to enter a choice for the effect
	 * 
	 * @param input Sample effects string to check for User choice prompts
	 * @return True if the appropriate symbols are in the input, false otherwise
	 */
	public static boolean containsChoice(String input)
	{
		return Pattern.compile("\\?([0-9]+)\\?").matcher(input).find();
	}
	
	/**
	 * Like containsChoice, but returns true if input contains a choice wildcard, as well as any other wildcard or special symbol like !RANDSKILL!
	 * @param input
	 * @return True or false as appropriate
	 */
	public static boolean containsUncertainty(String input)
	{
		if (containsChoice(input))
		{
			return true;
		}
		else
		{
			String[] specialStrs = {"!RANDSKILL!","!RANDAPT!","!RAND_DER!"};
			
			for (String str : specialStrs)
			{
				if (input.contains(str))
				{
					return true;
				}
			}
			
			return false;
		}
	}

	/**
	 * Returns whether the data store contains a particular object
	 * 
	 * @param input Name of an potential object in data store
	 * @return True/False as appropriate
	 * @throw IllegalStateException if the datastore isn't intialized
	 */
	public static boolean dataObjExists(String input)
	{
		if (dataStore == null)
		{
			throw new IllegalStateException("Datastore not built yet!");
		}
		
		return dataStore.containsKey(input);
	}
	
	/**
	 * Gets object from data store
	 * @param name Name of object to attempt to return
	 * @return The object matching the name specified, or null if no object exists
	 * @throw IllegalStateException if the datastore isn't intialized
	 */
	public static UniqueNamedData getDataObj(String name)
	{
		if (dataStore == null)
		{
			throw new IllegalStateException("Datastore not built yet!");
		}
		
		return dataStore.get(name);
	}
	
	/**
	 * Multifunction means of parsing an effect string into messages and information about what kind of prompt it is, when the user has a choice Prompt
	 * For instance, it can determine a message is prompt is for choosing a field skill, and give the information related to that, which can be used to provide a list
	 * of valid answers to default to.
	 * 
	 * @param message The message the user recieves (usually a translated effect string)
	 * @param extra Extra contextual info read from if * is detected in String Message
	 * @return String[] of format {TypePrompt,UIMessage,any other params, etc}
	 */
	public static String[] getExtraPromptOptions(String message,String extra)
	{
		if (message.contains("*"))
		{
			message = message + "\n" + extra;
		}
		
		Matcher fieldSkill = Pattern.compile("Add skill : ([a-zA-Z]+):[ ]*\\(Choose One Skl\\) [0-9]+").matcher(message);
		Matcher fullSkill = Pattern.compile("Add skill : \\(Choose One Skl\\) [0-9]+").matcher(message);
		Matcher fullSkillNonPsi = Pattern.compile("(Choose )?(Any|One|Two|Three|Four|Five|Six|Seven|Eight) Non Psi(-| )Skill").matcher(message);
		Matcher fullSkillPsi = Pattern.compile("(Choose )?(Any|One|Two|Three|Four|Five|Six|Seven|Eight) Psi(-| )Skill").matcher(message);
		Matcher repChoice = Pattern.compile("Add [\\-]?[0-9]+ to \\(Choose One\\)-rep").matcher(message);
		
		if (fieldSkill.find())
		{
			return new String[]{"field","\n Entering nothing will attempt to choose a random value for Field Skills",fieldSkill.group(1)};
		}
		else if (fullSkillNonPsi.find())
		{
			return new String[]{"skillNoPsi","\n Entering nothing will attempt to choose a random valid Non-Psi skill"};
		}
		else if (fullSkillPsi.find())
		{
			return new String[]{"skillPsi","\n Entering nothing will attempt to choose a random valid Psi skill"};
		}
		else if (fullSkill.find())
		{
			return new String[]{"skill","\n Entering nothing will attempt to choose a random valid skill"};
		}
		else if (message.contains("Add psi sleight:"))
		{
			return new String[]{"sleight","\n Entering nothing will attempt to choose a random valid sleight"};
		}
		else if (message.contains("Add psi-chi sleight:"))
		{
			return new String[]{"sleightChi","\n Entering nothing will attempt to choose a random valid psi-chi sleight"};
		}
		else if (message.contains("Add psi-gamma sleight:"))
		{
			return new String[]{"sleightGamma","\n Entering nothing will attempt to choose a random valid psi-gamma sleight"};
		}
		else if (repChoice.find())
		{
			return new String[]{"rep","\n Entering nothing will attempt to choose a random valid rep category"};
		}
		else if (message.contains("Add trait: Mental Disorder ((Choose One))"))
		{
			return new String[]{"mentDisorder","\n  Entering nothing will attempt to choose a random valid mental disorder"};
		}
		
		return null;
	}
	
}
