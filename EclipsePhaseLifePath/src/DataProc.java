import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 */

/**
 * @author Vigilant
 *
 * The DataFileProcessor class serves to take commands to look up a particular table,
 * and results in the right table (from text files) being called up, and the appropriate
 * action being processed
 *
 */
public class DataProc {
	
	// we keep these incase later versions need to write to datafiles
	private static String lpFileName = ""; // holds packages/tables/etc needed for the lifepath and/or package system
	private static String internalDatFile = ""; // holds things like skills, rep, gear, etc, each setup so that getNextChunk can parse from it
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
				String nextTrait = "";
				int idx = 0;
				int startIdx = 0;
				int endIdx = 0;
				
				// this trait is multiline so we have to rejoin the string array
				String chunkStr = Utils.joinStr(chunk);
				
				do 
				{
					startIdx = chunkStr.indexOf("TRAIT:::", idx); // we find the next valid Trait
					
					// if there wasn't anything found, we're done
					if (startIdx == -1)
					{
						nextTrait = "";
						break;
					}
					
					endIdx = chunkStr.indexOf("TRAIT:::", startIdx+1); // we look a bit ahead to see the one after it to figure out the endpoint
					
					// if we've gotten this far, we have a valid chunk to work on
					if (endIdx != -1)
					{
						// get to the end of the stream
						nextTrait = chunkStr.substring(startIdx, endIdx);
						idx = endIdx;
					}
					else
					{
						// get to the end of the stream
						nextTrait = chunkStr.substring(startIdx);
						idx = idx+1; // this will make it fail to find any more traits
					}
					
					// we do some pre processing before feeding in the nextTrait we just generated
					// this isn't explictly needed but it helps re-emulate the format used in other areas
					// the hope is to go back later and find a nice way to do this from the start
					
					int endFirstLine = nextTrait.indexOf('\n');
					
					// we want to make sure we found a valid index and there's stuff after it
					if (endFirstLine == -1 || nextTrait.length() == (endFirstLine+1))
					{
						throw new IllegalArgumentException("Unknown type of data or incorrect formating for Trait chunk : " + nextTrait);
					}
					
					String processedStr = nextTrait.substring(0, endFirstLine) + "|" + nextTrait.substring(endFirstLine+1);
					
					Trait.CreateInternalTrait(processedStr);
					
				} while ( nextTrait != "");

			}
			else if (chunk[0].equalsIgnoreCase("sleight") && chunk.length > 1)
			{

				addSleight(chunk);
			
			}
			else if (chunk[0].equalsIgnoreCase("step") && chunk.length > 1)
			{

				addStep(chunk);
			
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
			throw new IllegalArgumentException("Incorrect format on package (must contain PP;1 line " + name);
		}
		
		dataStore.put(name, new Package(name,desc,effectsList,motiv,specialNotes));
		
	}
	
	/**
	 * Adds table to the system
	 * Format:
	 * Line 1 : TABLE
	 * Line 2 : The table's name
	 * Line 3 : Integer value, the size of the dice to roll for the table
	 * Line 4 : Rest lines follow format range`description`effects
	 * 
	 * @param lines List of lines comprising the table to be read in
	 */
	private static void addTable(String[] lines)
	{
		// first line just says table, we can ignore
		
		// second line is table name
		String name = lines[1];
		
		// third line is the size dice to roll for the table (integer)
		if (!Utils.isInteger(lines[2]))
		{
			throw new IllegalArgumentException("3rd line of table must be a number : " + name);
		}
		int diceSides = Integer.parseInt(lines[2]);
		
		ArrayList<TableRow> rows = new ArrayList<TableRow>();
		
		for (int x = 3; x < lines.length; x++)
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
		
		dataStore.put(name, new Table(name, diceSides, rows));
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
		
		if (lines.length < 2 || lines[2].length() > 0)
		{
			throw new IllegalArgumentException("Function must have an effects row : " + name);
		}
		
		dataStore.put(name, new Function(name, lines[2]));
	}	
	/**
	 * Adds sleight to the system
	 * Format (by line):
	 * 
	 * SLEIGHT
	 * TYPE // CHI, GAMMA, ETC
	 * TRUE/FALSE  // is exsurgent only?
	 * SLEIGHT_NAME
	 * Active/Passive?
	 * ActionType
	 * Range
	 * Duration
	 * Strain Mod
	 * Skill Used
	 * Description
	 * 
	 * @param lines List of lines comprising the Sleight to be read in
	 */
	private static void addSleight(String[] lines)
	{
		if (lines.length != 11)
		{
			throw new IllegalArgumentException("Sleight chunk must be 11 lines long! (before line:" + fileLineNumber + ")");
		}
		
		int cnt = 1; // so lines can be rearranged easily
		String sleightType = lines[cnt++];
		String isExsurgent = lines[cnt++];
		
		if (!(isExsurgent.equalsIgnoreCase("true") || isExsurgent.equalsIgnoreCase("false")))
		{
			throw new IllegalArgumentException("field isExsurgent for Sleight must be either true or false (before line:" + fileLineNumber + ")");
		}
		
		String sleightName = lines[cnt++];
		String activePassive = lines[cnt++];
		String actionType = lines[cnt++];
		String range = lines[cnt++];
		String duration = lines[cnt++];
		String strainMod = lines[cnt++];
		String skillUsed = lines[cnt++];
		String description = lines[cnt++];
		
		Sleight.CreateInternalsleight(new String[]{sleightType,isExsurgent,sleightName,activePassive,actionType,range,duration,strainMod,skillUsed,description});
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
		if (dataStore == null)
		{
			throw new IllegalStateException("Datastore not built yet!");
		}
		
		String result = "";
		
		String[] effectsArr = effects.toLowerCase().split(";");
		
		for (String effect : effectsArr)
		{
			String errorInfo = "";
			
			String params = Utils.returnStringInParen(effect);
			String commandName = effect.substring(0, effect.indexOf('(')-1);
			// TODO : to comply with older code, we have to insert the command at the beginning of params
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
			
			
			if (Skill.isSkill(effect))
			{
				Skill temp = Skill.CreateSkillFromString(effect);
				result += temp.toString();
			}
			else if (effect.startsWith("inc"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (Skill.isSkill(subparts[1]) && Utils.isInteger(subparts[2]))
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
			else if (effect.startsWith("dec"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3 && subparts.length != 4)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (Skill.isSkill(subparts[1]) && subparts[2].equalsIgnoreCase("all"))
				{
					result += "Remove skill: " + subparts[1];
				}
				else if (Skill.isSkill(subparts[1]) && Utils.isInteger(subparts[2]))
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
			else if (effect.startsWith("sklspec"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (Skill.isSkill(subparts[1]) && subparts[2].length() > 0)
				{
					result += "Add specialization: " + subparts[2] + " to " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("trait"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0)
				{
					result += "Add trait: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("morph"))
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
					result += "Add trait: " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("setapt"))
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
			else if (effect.startsWith("addapt"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && Utils.isInteger(subparts[2]))
				{
					result += "Add " + subparts[2] + " to " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("roll") || effect.startsWith("forceRoll"))
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
			else if (effect.startsWith("rollTable")  || effect.startsWith("forceRollTable"))
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
			else if (effect.startsWith("runTable"))
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
					
					Table temp = (Table)DataProc.getDataObj(subparts[1]);
					
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
			else if (effect.startsWith("mox"))
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
			else if (effect.startsWith("setmox"))
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
			else if (effect.startsWith("gear"))
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
			else if (effect.startsWith("background"))
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
			else if (effect.startsWith("nextpath"))
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
			else if (effect.startsWith("stepskip"))
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
			else if (effect.startsWith("package"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length == 2 && subparts[1].length() > 0)
				{
					result += "Add package : " + subparts[1];
				}
				else if (subparts.length == 3 && subparts[1].length() > 0 && Utils.isInteger(subparts[2]))
				{
					result += "Add package (" + subparts[2] + "PP) : " + subparts[1];
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("rep"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts[1].length() > 0 && Utils.isInteger(subparts[2]))
				{
					result += "Add " + subparts[2] + " to " + subparts[1] + "-rep";
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("credit"))
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
			else if (effect.startsWith("psichi"))
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
			else if (effect.startsWith("psigamma"))
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
			else if (effect.startsWith("psisleight"))
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
			else if (effect.startsWith("extendedChoice"))
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
			else if (effect.startsWith("if"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length < 2 || subparts.length > 3)
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if (subparts.length == 2 && subparts[0].length() > 0 && subparts[1].length() > 1)
				{
					result += "If (" + subparts[0] + ") then " + effectsToString(subparts[1]);
				}
				else if (subparts.length == 3 && subparts[0].length() > 0 && subparts[1].length() > 1 && subparts[1].length() > 2)
				{
					result += "If (" + subparts[0] + ") then " + effectsToString(subparts[1]) + " : ELSE : " + effectsToString(subparts[2]);
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("func"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else
				{
					if (dataStore.containsKey(subparts[1]) && dataStore.get(subparts[1]).getType().equals("function"))
					{
						Function temp = (Function)dataStore.get(subparts[1]);
						return effectsToString(temp.getEffect());
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect, " + subparts[1] + " does not exist or is not a function" + errorInfo);
					}
				}
			}
			else if (effect.startsWith("msgClient"))
			{
				String[] subparts = Utils.splitCommands(params);
				if (subparts.length != 2 )
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				else if ( subparts[1].length() > 0)
				{
					result += "Send message to the client : " + subparts[1];					
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			}
			else if (effect.startsWith("setVar"))
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
			else
			{
				throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
			}
		
			result += ",";
		}
		
		// we give nice words for up to Choose 5, because these are the most common
		result = result.replaceAll("\\?1\\?", "Choose");
		result = result.replaceAll("\\?2\\?", "Choose Two");
		result = result.replaceAll("\\?3\\?", "Choose Three");
		result = result.replaceAll("\\?4\\?", "Choose Four");
		result = result.replaceAll("\\?5\\?", "Choose Five");
		
		//remaining ones get ugly number based notation
		result = result.replaceAll("\\?([0-9]*)\\?", "Choose $1");
		
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
		return input.matches("\\?([0-9]*)\\?");
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
	
	
}
