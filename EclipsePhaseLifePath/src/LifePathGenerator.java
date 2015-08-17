import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author Vigilant
 *
 * The generator class takes in the appropriate information,
 * and then runs the lifepath process to interactively generate a character
 * through various stages,packages, and tables
 */
public class LifePathGenerator {

	public static SecureRandom rng = new SecureRandom();
	private Character playerChar;
	private UI UIObject;
	private boolean isRolling;
	private String nextEffects; // used to store things between steps
	private boolean hasStarted;
	private boolean hasFinished;
	private String pendingEffects; // usually set during the process of rolling a table, sets the logical next step if there's no interrupts
	
	/**
	 * Creates the LifePathGenerator
	 * 
	 * @param characterName Name of the character being genned
	 * @param UIObject_ Valid object that can perform user interaction (prompts, alerts)
	 * @param isRolling Boolean flag for whether dice rolls are rolled, or instead prompts to the user. Not all dice rolls can be manual
	 */
	public LifePathGenerator(String characterName, UI UIObject_, boolean isRolling)
	{
		playerChar = new Character(characterName);
		UIObject = UIObject_;
		this.isRolling = isRolling;
		this.nextEffects = "";
		this.hasStarted = false;
		this.hasFinished = false;
		pendingEffects = "";
	}	
	
	
	/**
	 * @return the playerChar
	 */
	public Character getPC() {
		return playerChar;
	}

	/**
	 * @param playerChar the playerChar to set
	 */
	public void setPC(Character playerChar) {
		this.playerChar = playerChar;
	}



	protected String splitChoiceTokens(String input, ArrayList<String> buffer)
	{
		Pattern groups = Pattern.compile("\\?([2-9]+)\\?\\**");
		Matcher m = groups.matcher(input);
		int val = Integer.parseInt(m.group(1));
		String match = m.group();
		
		// clone effect for every value over 1, so that ?2? leads to two effects with ?1?
		input = input.replaceFirst(match, match.replaceFirst("\\?" + val + "\\?","?1?"));
		for (int i = 1; i < val; i++)
		{
			buffer.add(input);
		}
		
		ArrayList<String> buffer2 = new ArrayList<String>();
		
		for (String str : buffer)
		{
			splitChoiceTokens(str,buffer2);
		}
		
		buffer.addAll(buffer2);
		
		return input;
	}
	
	/**
	 * Attempts to execute the effects of the passed in string. Whenever a "step changing" event would happen, returns the effects for that step 
	 * 
	 * @param effectInput Properly formated effect string warning : currently not much error checking.
	 * @param extraContext Any additional info that might be worth displaying to the user during a choice prompt. often blank.
	 * @return Any pending effects for the next step
	 */
	private String runEffect(String effectInput, String extraContext)
	{
		// translate steps to actions, as we'll just get a step name sometimes
		if (DataProc.dataObjExists(effectInput) && DataProc.getDataObj(effectInput).getType().equals("step"))
		{
			Step temp = ((Step)DataProc.getDataObj(effectInput));
			effectInput = temp.getEffects();
			playerChar.setLastStep(temp);
		}
		
		// we let users define \, so that commas can be escaped until after the splitting of a comma delimited effects chain
		String modifiedInput = effectInput.replace("\\,", "!!COMMA!!");
		modifiedInput = modifiedInput.replace("\\;", "!!SEMICOLON!!");		
		
		String[] effects = Utils.splitCommands(modifiedInput, ";");
		
		pendingEffects = ""; // usually set during the process of rolling a table, sets the logical next step if there's no interrupts
							 // pendingEffects is global so that recurisve calls to runEffect can change what the next set of effects is
		
		if (playerChar.getLastStep() != null)
		{
			pendingEffects = playerChar.getLastStep().getNextStep();
		}
		
		ArrayList<String> mainStuff = new ArrayList<String>();
		
		for (String eff : effects)
		{
			String tempEff = eff.replace("!!COMMA!!",",");
				
			ArrayList<String> buffer = new ArrayList<String>();
			
			// this should handle all cases of choice token splitting, no matter how convulted/nested
			// "splitting" refers to making high numbered choice tokens like ?3? into three separate effects of ?1?
			while (tempEff.matches("\\?([2-9]+)\\?\\**"))
			{
				tempEff = splitChoiceTokens(tempEff,buffer);								
			}
						
			buffer.add(tempEff);
			
			mainStuff.addAll(buffer);
			
		}
		
		for (int i = 0; i < mainStuff.size(); i++)
		{
			String effect = mainStuff.get(i);
			
			try
			{
			
				while (DataProc.containsChoice(effect))
				{
						String extraInfo = extraContext;
						// grab info from the package special notes if it exists
						// we have to do some redundant preprocessing here
						if (extraInfo == "" && effect.toLowerCase().startsWith("+package"))
						{
							String[] parts = Utils.splitCommands(effect);
							
							if (parts.length == 2 && DataProc.dataObjExists(parts[1]))
							{
								UniqueNamedData dataObject = DataProc.getDataObj(parts[1]);
								if (dataObject.getType().equals("package") && ((Package)dataObject).getSpecialNotes().length() != 0)
								{
									extraInfo = ((Package)dataObject).getSpecialNotes();
								}
							}
						}
					
						// we look for a context bubble (tells user what you might be wanting them to enter)
						if (Pattern.matches("#[^#]+#", effect))
						{							
							extraInfo = Pattern.compile("#[^#]+#").matcher(effect).group(1);
							
							// once done, we remove this from the effect
							effect = effect.replaceFirst("#[^#]+#", "");
						}
						
						// will make assumption user knows that choices are resolved left to right
						// will also remove any asterisks that appeared after since they'll probably interfere
						mainStuff.set(i, effect.replaceFirst("\\?([0-9]+)\\?[\\*]*", UIObject.promptUser(DataProc.effectsToString(effect),extraContext+"\n"+extraInfo)));
						effect = mainStuff.get(i);
				}
				
				// big wall of cases follow.
				String errorInfo = ": " + effect;

				
				
				// much preprocessing can't be done until the last second because it can be effected by other calls
				while (effect.contains("getVar("))
				{
					int idx = effect.indexOf("getVar(");
					
					String insides = Utils.returnStringInParen(effect,idx);
					
					String oldStr = "getVar(" + insides + ")";
					
					if (playerChar.hasVar(insides))
					{
						String newStr = playerChar.getVar(insides);
						effect = effect.replace(oldStr, newStr);
					}
					else
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for a variable that doesn't exist : ");
					}
					
				}
				// handle preprocessing
				while(effect.contains("!RANDSKILL!"))
				{
					if (playerChar.getNumSkills() > 0)
					{
						String randSkill = playerChar.getRandSkill();
						effect = effect.replace("!RANDSKILL!", randSkill);
					}
					else
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for random skill but the character has no skills!");
					}
				}
				while(effect.contains("!RANDAPT!"))
				{
					effect = effect.replace("!RANDAPT!",playerChar.getRandApt());
				}
				while(effect.contains("!RAND_DER!"))
				{
					effect = effect.replace("!RAND_DER!",Trait.getRandomDerangement(rng).getName());
				}
				while (effect.contains("rollDice("))
				{
					int idx = effect.indexOf("rollDice(");
					
					String insides = Utils.returnStringInParen(effect,idx);
					
					String oldStr = "rollDice(" + insides + ")";

					String[] subParts = Utils.splitCommands(insides);
					
					if (subParts.length != 2 || !Utils.isInteger(subParts[0]))
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for rollDice but lacks the correct format");
					}
					
					int diceSides = Integer.parseInt(subParts[0]);
					String message = subParts[1];
					
					String newStr = "" + this.rollDice(diceSides, message, false);
					effect = effect.replace(oldStr, newStr);

					
				}
				
				// this version does multiple dice and is forced (player cannot choose)
				while (effect.contains("simpRollDice("))
				{
					int idx = effect.indexOf("simpRollDice(");
					
					String insides = Utils.returnStringInParen(effect,idx);
					
					String oldStr = "simpRollDice(" + insides + ")";

					String[] subParts = Utils.splitCommands(insides);
					
					if (subParts.length != 2 || !Utils.isInteger(subParts[0]) || !Utils.isInteger(subParts[1]))
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for simpRollDice but lacks the correct format");
					}
					
					int result = 0;
					int numDice = Integer.parseInt(subParts[0]);
					int numSides = Integer.parseInt(subParts[1]);
					
					for (int x = 0; x < numDice; x++)
					{
						result += rollDice(numSides,"",true);
					}
					
					String newStr = ""+result;
					effect = effect.replace(oldStr, newStr);
				}
				
				
				// for best results this should be one of the last (or the last) preprocessing effect run
				while (effect.contains("concat("))
				{
					int idx = effect.indexOf("concat(");
					
					String insides = Utils.returnStringInParen(effect,idx);
					
					String oldStr = "concat(" + insides + ")";

					String[] subParts = Utils.splitCommands(insides);
					
					if (subParts.length != 2)
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for concat but lacks the correct format");
					}
					
					String newStr = subParts[0] + subParts[1];
					effect = effect.replace(oldStr, newStr);

					
				}
				
				// same with this, this needs to go near the end
				while (effect.contains("mult("))
				{
					int idx = effect.indexOf("mult(");
					
					String insides = Utils.returnStringInParen(effect,idx);
					
					String oldStr = "mult(" + insides + ")";

					String[] subParts = Utils.splitCommands(insides);
					
					if (subParts.length != 2  || !Utils.isInteger(subParts[0]) || !Utils.isInteger(subParts[1]))
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for mult but lacks the correct format");
					}
					
					int p1 = Integer.parseInt(subParts[0]);
					int p2 = Integer.parseInt(subParts[1]);
					
					String newStr = ""+(p1*p2);
					effect = effect.replace(oldStr, newStr);

					
				}
				
				
				// note for some of these, we sacrifice performance by making the if conditions a bit more error aware up front, and leaving
				// the code a bit simpler. This is probably for the best since even with that the app will have reasonable performance,
				// and the code is meant to be readable for others to look at
				
				// An additional consideration is that thrown exceptions are caught and passed to the UI, so sending descriptive messages is a
				// good idea
								
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
				params = commandName + "," + params;
				
				if (Skill.isSkill(effect))
				{
					Skill temp = Skill.CreateSkillFromString(effect);
					playerChar.addSkill(temp);
				}
				else if (effect.startsWith("setSkl"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3 && subparts.length != 4)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (Skill.isSkill(subparts[1]))
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (subparts.length == 4)
						{	
							if (!this.resolveConditional(subparts[3], subparts))
							{
								throw new IllegalArgumentException("Poorly formated effect, conditional is not true : " + subparts[3]);
							}
						}
						
						// executes the add, throwing error if the skill didn't exist
						if (! playerChar.setSkill(subparts[1], Integer.parseInt(subparts[2])) )
						{
							throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + subparts[1]);
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					
				}
				else if (effect.startsWith("incSkl"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3 && subparts.length != 4)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (Skill.isSkill(subparts[1]))
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (subparts.length == 4)
						{	
							if (!this.resolveConditional(subparts[3], subparts))
							{
								throw new IllegalArgumentException("Poorly formated effect, conditional is not true : " + subparts[3]);
							}
						}
						
						// executes the add, throwing error if the skill didn't exist
						if (! playerChar.incSkill(subparts[1], Integer.parseInt(subparts[2])) )
						{
							throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + subparts[1]);
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					
				}
				else if (effect.startsWith("decSkl"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3 && subparts.length != 4)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (Skill.isSkill(subparts[1]) && subparts[2].equalsIgnoreCase("all"))
					{
						if (! playerChar.removeSkill(subparts[1]) )
						{
							throw new IllegalArgumentException("Poorly formated effect, skill does not exist : " + subparts[1]);
						}
						
					}
					else if (Skill.isSkill(subparts[1]) )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (subparts.length == 4)
						{	
							if (!this.resolveConditional(subparts[3], subparts))
							{
								throw new IllegalArgumentException("Poorly formated effect, conditional is not true : " + subparts[3]);
							}
						}
						
						if (! playerChar.incSkill(subparts[1], Integer.parseInt(subparts[2])) )
						{
							throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + errorInfo);
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
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
						if (! playerChar.addSkillSpec(subparts[1], subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + errorInfo);
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("trait"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length < 2 || subparts.length > 3)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts[1].length() == 2 && Trait.exists(subparts[1]))
					{
						playerChar.addTrait(Trait.getTrait(subparts[1], 1));
					}
					else if (subparts[1].length() == 2 && Trait.existsPartial(subparts[1]) )
					{
						Trait t = Trait.getTraitFromPartial(subparts[1], 1);
						
						playerChar.addTrait(t);
					}
					else if (subparts[1].length() == 3 && Trait.exists(subparts[1]) )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						playerChar.addTrait(Trait.getTrait(subparts[1], Integer.parseInt(subparts[2])));
					}
					else if (subparts[1].length() == 3 && Trait.existsPartial(subparts[1]) )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						Trait t = Trait.getTraitFromPartial(subparts[1], Integer.parseInt(subparts[2]));
						
						playerChar.addTrait(t);
					}
					else
					{
						throw new IllegalArgumentException("Trait " + subparts[1] + " does not exist, or other formating problem: ("+ errorInfo + ")");
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
						// no need to return anything, this is just to get a new morph
						this.runEffect("roll;CHOOSING_A_MORPH","");
					}
					else if (subparts[1].length() > 0)
					{
						if (Morph.exists(subparts[1]))
						{
							playerChar.setCurrentMorph(Morph.createMorph(subparts[1]));
						}
						else
						{
							throw new IllegalArgumentException("Morph does not exist : " + subparts[1]);
						}
						
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("setApt"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts[1].length() > 0 )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (! playerChar.isValidAptitude(subparts[1]))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a valid aptitude");
						}
						
						playerChar.setAptitude(subparts[1], Integer.parseInt(subparts[2]));
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("addapt"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3 && subparts.length != 4 )
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts[1].length() > 0 )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (! playerChar.isValidAptitude(subparts[1]))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a valid aptitude");
						}
						
						if (subparts.length == 4)
						{	
							if (!this.resolveConditional(subparts[3], subparts))
							{
								throw new IllegalArgumentException("Poorly formated effect, conditional is not true : " + subparts[3]);
							}
						}
						
						playerChar.incAptitude(subparts[2], Integer.parseInt(subparts[2]));
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("rollTable"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRollTable(effect, errorInfo, false);					
				}
				else if (effect.startsWith("roll"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRoll(effect, errorInfo, false);					
				}
				else if (effect.startsWith("forceRollTable"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRollTable(effect, errorInfo, true);					
				}
				else if (effect.startsWith("forceRoll"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRoll(effect, errorInfo, true);					
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
							TableRow tempRow = temp.findMatch(Integer.parseInt(subparts[2]),subparts[3]);
							this.runEffect(tempRow.getEffects(), extraContext);
						}
						else
						{
							TableRow tempRow = temp.findMatch(Integer.parseInt(subparts[2]));
							this.runEffect(tempRow.getEffects(), extraContext);
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
						playerChar.incMox(Integer.parseInt(subparts[1]));
					}
					else
					{						
						throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");											
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
						playerChar.setMox(Integer.parseInt(subparts[1]));
					}
					else
					{
						throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");
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
						playerChar.addGear(subparts[1]);
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
						playerChar.setBackground(subparts[1]);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("nextPath"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 2)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts[1].length() > 0)
					{
						playerChar.setVar("{path}",subparts[1]);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("faction"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 2)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts[1].length() > 0)
					{
						playerChar.setVar("{faction}",subparts[1]);
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
						if (! DataProc.dataObjExists(subparts[1]))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
						}
						
						if (! DataProc.getDataObj(subparts[1]).getType().equals("step"))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a step");
						}
						
						Step temp = (Step)DataProc.getDataObj(subparts[1]);
						playerChar.setLastStep(temp);
						
						// special version allows for a clean jump that doesn't interrupt the UI
						if (effect.toLowerCase().startsWith("stepskipnostop"))
						{
							return this.runEffect(temp.getEffects(), extraContext);
						}
						{
							return temp.getEffects();
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("package"))
				{
					String[] subparts = Utils.splitCommands(params);
					
					// checks for package being valid
					if (subparts.length == 2 || subparts.length == 3)
					{
						if ( subparts[1].length() == 0 )
						{
							throw new IllegalArgumentException("Poorly formatted effect (" +  errorInfo + "): no package name found");
						}
						
						if (! DataProc.dataObjExists(subparts[1]))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
						}
						
						if (! DataProc.getDataObj(subparts[1]).getType().equals("package"))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a package");
						}
					}
					
					Package temp = (Package)DataProc.getDataObj(subparts[1]); 
					
					// we default to PP1 if the specified PP doesn't exist
					if (subparts.length == 2 || !temp.getEffectsTree().containsKey(subparts[1]))
					{												
						String pkgEffect = temp.getEffects(1);						
						
						UIObject.statusUpdate("Package added (PP1): " + temp.getName() + " : " + temp.getDescription());
												
						playerChar.incVar("packageVal", 1);
						this.runEffect(pkgEffect, temp.getSpecialNotes());
					}
					else if (subparts.length == 3 )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						int PP = Integer.parseInt(subparts[2]);
						
						String pkgEffect = temp.getEffects(PP);
						
						UIObject.statusUpdate("Package added (PP" + PP + "): " + temp.getName() + " : " + temp.getDescription());
						
						playerChar.incVar("packageVal", PP);
						this.runEffect(pkgEffect, temp.getSpecialNotes());
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("rep"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3 && subparts.length != 4)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts[1].length() > 0 )
					{
						if (! Rep.exists(subparts[1]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect Rep (" + subparts[1] + ") does not exist");
						}
						
						
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (subparts.length == 4)
						{	
							if (!this.resolveConditional(subparts[3], subparts))
							{
								throw new IllegalArgumentException("Poorly formated effect, conditional is not true : " + subparts[3]);
							}
						}
						
						playerChar.incRepValue(subparts[1], Integer.parseInt(subparts[2]));
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
						playerChar.incCredits(Integer.parseInt(subparts[1]));
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
					else if (subparts[1].length() > 0 )
					{
						if (! Sleight.exists(subparts[1] ) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
						}
						
						if (! Sleight.sleightList.get(subparts[1]).getSleightType().equals("chi") )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " is not a Psi Chi sleight");
						}

						playerChar.addSleight(Sleight.sleightList.get(subparts[1]));
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
					else if (subparts[1].length() > 0 )
					{
						if (! Sleight.exists(subparts[1] ) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
						}
						
						if (! Sleight.sleightList.get(subparts[1]).getSleightType().equals("gamma") )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " is not a Psi Gamma sleight");
						}
						
						playerChar.addSleight(Sleight.sleightList.get(subparts[1]));
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
					else if (subparts[1].length() > 0 )
					{
						if (! Sleight.exists(subparts[1] ) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
						}
						
						playerChar.addSleight(Sleight.sleightList.get(subparts[1]));
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
						String response = UIObject.promptUser(subparts[1], ""); // response should be an integer
						
						
						if (Utils.isInteger(response))
						{
							int choice = Integer.parseInt(response);
							
							String[] choiceEffects = subparts[2].split("/");
							
							if (choice <= 0 || choice > choiceEffects.length)
							{
								throw new RuntimeException("Response : " + choice + " is less than one or greater than number of choices");
							}
							else
							{
								this.runEffect(choiceEffects[choice-1].split("=")[1], subparts[1]);
							}
						}
						else
						{
							throw new RuntimeException("Unexpected non-integer response from UI");
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("if"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 3 && subparts.length != 4)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts.length == 3 && subparts[1].length() > 0 && subparts[2].length() > 0)
					{
						boolean ifResult = this.resolveConditional(subparts[1],subparts);
						
						if (ifResult)
						{
							this.runEffect(subparts[2], "");
						}
					}
					else if (subparts.length == 4 && subparts[1].length() > 0 && subparts[2].length() > 0 && subparts[3].length() > 0)
					{
						boolean ifResult = this.resolveConditional(subparts[1],subparts);
						
						if (ifResult)
						{
							this.runEffect(subparts[2], "");
						}
						else
						{
							this.runEffect(subparts[3], "");
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("func"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length < 2 )
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else
					{
						if (DataProc.dataObjExists(subparts[1]) && DataProc.getDataObj(subparts[1]).getType().equals("function"))
						{
							Function temp = (Function)DataProc.getDataObj(subparts[1]);
							
							String effectStr = temp.getEffect();
							
							for (int x = 2; x < subparts.length; x++)
							{
								int idx = x-1; // we start with looking to replace &1&
								effectStr = effectStr.replaceAll("&" + idx + "&", subparts[x]);
							}
							
							this.runEffect(effectStr, "");
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
						UIObject.statusUpdate(subparts[1]);						
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
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
						UIObject.statusUpdate(subparts[1]);						
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
						playerChar.setVar(subparts[1], subparts[2]);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("incVar"))
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
						playerChar.incVar(subparts[1],subparts[2]);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("remVar"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length != 2 )
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if ( subparts[1].length() > 0)
					{
						playerChar.removeVar(subparts[1]);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (effect.startsWith("stop"))
				{
					this.hasFinished = true;
					UIObject.end();
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
			
				// still finishing up adding effects
				
				/**
				 * 
				 * Any of the below will add a skill if it doesn't exist, or add to it if it's already there
	<skillname> <number>
	<skillname>: <subtype> <number> 
	<skillname>[<specialization>] <number>
	<skillname>: <subtype> [<specialization>] <number>

	Preprocessing Commands (ran before others):
	!RANDSKILL! => pick random valid skill character has
	!RANDAPT! => pick random valid Aptitude character has  
	!RAND_DER! => pick random Derangement
	concat(<value1>,<value2>) (appends value2 to the end of value1)
	getVar(<name>)			(returns data stored for this var) (some character fields can be accessed via {}, like {nextPath})
	rollDice(<sides>,<message>)			players can choose the result of this if choose mode is on
	simpRollDice(<numDice>,<sides>)		players cannot choose the result of this (always forceRoll true)
	
	?1? can be used to prompt the user to make a choice (will open a text prompt)
		?2? and ?3? and etc are used as shortcuts which cause the command to split into multiple commands, each with ?1?, a single choice
		The prompt will automatically use things like tablerow descriptions and package descriptions in the prompt
		You can manually specify information to appear in the prompt by placing text enclosed in ## . This text will be removed after being read, so doesn't effect
			normal processing
		ex: incSkl(?1?#Choose Fray or Climbing#,10,?equals($1,Fray)||?equals($1,Climbing))
	

	\, can be used to escape commas so they're not counted until after the initial split of a command chain, and can be chained as many times as needed
	\; is often similarly used for nested commands

	Rest of commands:
	incSkl(<skill>,<value>)
	incSkl(<skill>,<value>,<conditional>)
	setSkl(<skill>,<value>,<conditional>)
	decSkl(<skill>,<value/all>)					(decSkl all will set two variables {lastRemSkl} {lastRemSklVal}, equal to what was removed)
	decSkl(<skill>,<value/all>,<conditional>)	' the three parameter versions throw an error if the conditional isn't true		
		
	SklSpec(<skill>,<specializationName>)
	trait(<trait>)
	trait(<trait>,level)
	morph(<morphname>)
	morph(randomRoll)
	setApt(<aptitudeName>,<value>)
	addApt(<aptitudeName>,<value>)					(can also be used to subtract with a negative value)
	addApt(<aptitudeName>,<value>,<conditional)		(the three parameter version throw an error if the conditional isn't true)
	mox(<value>)
	gear(<gearName>)
	roll(<dieNumber>,#-#=effect/#-#=effect)  (list can be as long as needed)		(ex, roll(1-6=morph,splicer/7-10=morph(bouncer)) 
	rollTable(<tableName>)						(replace semicolon, spaces and periods in table name with underscore, e.g. Table_6_5)
												forceRoll and forceRollTable can be used to make sure a user in interactive mode still rolls these 
	rollTable(<tableName>,<replaceValue>) 	(as before, but <replaceValue will sub in for any wildcards in the table) (wildcard is !!X!!)
	runTable(<tableName>,<number>)
	runTable(<tableName>,<number>,<wildCardReplace>) (similar to rollTable Except you specify what the number is)
	background(<name>)
	nextPath(<name>)
	faction(<name>)
	stepskip(<name>)			(immediately skip to step of this name)
	stepskipNoStop(<name>)			(immediately skip to step of this name, doesn't interrupt the UI)
	package(<name>)				(add package -- assume 1 PP if it needs a value)
	package(<name>,<value>)		(add package of a certain PP value)
	rep(<type>,<value>)
	rep(<type>,<value>,<conditional>)	(as with others, conditional must be true for the command to work)
	credit(<value>)
	psichi(<name>)				(can use ?1?,?2?, etc)
	psigamma(<name>)
	psisleight(<name>)
	extendedChoice(Text,0=effect/1=effect/2=effect/etc)   (this allows us a bit more freedom when a choice is complicated)
	if(<condition>,<effectWhenTrue>,<effectWhenFalse>)		(The latter can be blank)
	msgClient(<message>)					(says something to the UI about character changes)
	setVar(<name>,<value>)
	func(<name>)
	func(<name>,<param1>,<param2>,<...etc>)  (any params passed after name will substitute in for <1>,<2>, etc, in the function 
	stop()			(marks character generation as over)
	
	Conditions:
	?hastrait(trait)
	?hasSkill(skill)
	?skillIsType(skill,type)  (skill is name of skill, type is a type you want it to be, like Technical
	?hasBackground
	?hasHadBackground
	?hasRolled(number)
	?equals(string1,string2)
	?hasVar(varname)
	?between(input,lower,upper)

	$0,$1,$2,$3, etc when inside conditionals references the subparams of the effect containing the conditional, so 
	inc(<skill>,<number>,<conditional>) leads to $0 accessing inc, $1 accessing <skill> and so on
	
	|| and && are partially supported
	
	replacing ? with ! is for boolean not. so !hasTrait;trait => not having that trait
	
				 * 
				 */

			}
			catch( Exception e) 
			{
				// do something to prompt the user to fix their error
				boolean response = UIObject.handleError(e.getMessage());
				System.out.println(e.getMessage());
				e.printStackTrace();
				if (response)  // replace with seeing if response says to rollback last effect 
				{
					i--;
					continue;
				}
				else
				{
					return effectInput; // we spit back out what was put in, so it can be retried later
				}
			}
		}
		
		return pendingEffects;
	}
	
	/**
	 * Takes a conditional effect string and evaluates it against the character
	 * 
	 * Boolean || and && are allowed, but keep in mind that the implementation currently doesn't allow for ()
	 * and will evaluate those statements in a strict left to right order.
	 * 
	 * @param condition Condition string to be evaluated
	 * @param effectParams Parameters of the command holding this conditional
	 * @return True/False as appropriate to the condition
	 * @throws IllegalArgumentException if condition is not a recognized condition
	 */
	protected boolean resolveConditional(String condition, String[] effectParams)
	{
		if (condition.length() < 1)
		{
			throw new IllegalArgumentException("Condition is invalid : " + condition + " is less than 2 characters long");
		}
		
		// preprocessing
		for (int i = 0; i < effectParams.length; i++)
		{
			condition = condition.replaceAll("\\$"+i, effectParams[i]);
		}
		
		
		// split for ||
		if (condition.contains("||"))
		{
			String part1, part2;
			
			part1 = condition.substring(0, condition.indexOf("||"));
			part2 = condition.substring(condition.indexOf("||")+2);
			
			return this.resolveConditional(part1,effectParams) || this.resolveConditional(part2,effectParams);
		}
		
		if (condition.contains("&&"))
		{
			String part1, part2;
			
			part1 = condition.substring(0, condition.indexOf("||"));
			part2 = condition.substring(condition.indexOf("||")+2);
			
			return this.resolveConditional(part1,effectParams) && this.resolveConditional(part2,effectParams);
		}
		
		String condNoPrefix = condition.substring(1); // the starting character can be either ? or !, so we're careful of this.
		char firstChar = condition.charAt(0);
		
		if (firstChar == '!')
		{
			return !this.resolveConditional("?" + condNoPrefix,effectParams);
		}
		
		/*
		 *  ?hasTrait;trait
	?hasSkill;skill
	?hasRolled;number
	?hasBackground
		 */
		
		String params = Utils.returnStringInParen(condNoPrefix);
		String commandName = condNoPrefix.substring(0, condNoPrefix.indexOf('('));
		// TODO : to comply with older code, we have to insert the command at the beginning of params
		params = commandName + "," + params;
		
		String[] parts = Utils.splitCommands(params, ",");
			
		if (condNoPrefix.startsWith("hasTrait"))
		{
			if (parts.length != 2)
			{
				throw new IllegalArgumentException("Invalidly formatted condition " + condition + ")");
			}
			
			
			if (Trait.exists(parts[1]))
			{
				return playerChar.hasTrait(parts[1]);
			}
			else
			{
				throw new IllegalArgumentException("Trait : " + parts[1] + " does not exist!");
			}
		}
		else if (condNoPrefix.startsWith("hasSkill"))
		{
			if (parts.length != 2)
			{
				throw new IllegalArgumentException("Invalidly formatted condition " + condition + ")");
			}
				
			if (Skill.isSkill(parts[1]))
			{
				return playerChar.hasSkill(parts[1]);
			}
			else
			{
				throw new IllegalArgumentException("Skill : " + parts[1] + " does not exist!");
			}
		}
		else if (condNoPrefix.startsWith("skillIsType"))
		{
			if (parts.length != 3)
			{
				throw new IllegalArgumentException("Invalidly formatted condition " + condition + ")");
			}
				
			if (Skill.isSkill(parts[1]))
			{
				return Skill.hasCategory(parts[1], parts[2]);			
			}
			else
			{
				throw new IllegalArgumentException("Skill : " + parts[1] + " does not exist!");
			}
		}
		else if (condNoPrefix.startsWith("hasBackground"))
		{			
			if (parts.length != 2)
			{
				throw new IllegalArgumentException("Invalidly formatted condition " + condition + ")");
			}
			
			return playerChar.getBackground().equalsIgnoreCase(parts[1]); 
		}
		else if (condNoPrefix.startsWith("hasHadBackground"))
		{			
			if (parts.length != 2)
			{
				throw new IllegalArgumentException("Invalidly formatted condition " + condition + ")");
			}
			
			return playerChar.hasHadBackground(parts[1]);			
		}
		else if (condNoPrefix.startsWith("hasRoll"))
		{			
			if (parts.length != 2)
			{
				throw new IllegalArgumentException("Invalidly formatted condition " + condition + ")");
			}
			
			if (!Utils.isInteger(parts[1]))
			{
				throw new IllegalArgumentException(condition + " does not specify a number!");
			}
			
			return playerChar.rollsContain(Integer.parseInt(parts[1])); 
		}
		else if (condNoPrefix.startsWith("equals"))
		{				
			if (parts.length != 3)
			{
				throw new IllegalArgumentException("Invalidly formatted Equals condition (wrong number of parts " + condition + ")");
			}
			
			for (int i = 1; i < parts.length; i++)
			{
				// TODO This may no longer be needed due to preprocessing
				
				if (parts[i].startsWith("getVar"))
				{
					String name = Utils.returnStringInParen(parts[i]);
					
					if (playerChar.hasVar(name))
					{
						parts[i] = playerChar.getVar(name);
					}
				}
			}
			
			return parts[1].equals(parts[2]);
			
		}
		else if (condNoPrefix.startsWith("hasVar"))
		{
			if (parts.length != 2 || parts[1].length() == 0)
			{
				throw new IllegalArgumentException("Invalidly formatted Equals condition (wrong number of parts " + condition + ")");
			}
			
			return playerChar.hasVar(parts[1]);
			
		}
		else if (condNoPrefix.startsWith("between"))
		{				
			if (parts.length != 4)
			{
				throw new IllegalArgumentException("Invalidly formatted between condition (wrong number of parts " + condition + ")");
			}
			
			// check all conditions for being valid numbers
			for (int i = 1; i < parts.length; i++)
			{
				if (!Utils.isInteger(parts[i]))
				{
					throw new IllegalArgumentException("Invalidly formatted between condition ( " + parts[i] + ") is not an number");
				}
			}
			
			int input = Integer.parseInt(parts[1]);
			int lower = Integer.parseInt(parts[2]);
			int upper = Integer.parseInt(parts[3]);
			
			return (input >= lower && input <= upper);
		}
		
		
		return false;
	}
	
	/**
	 * Runs the next logical effect in the process, storing the effect after it into nextEffects
	 * 
	 * Will default to STEP_1 if hasStarted is false
	 * Will do nothing if hasFinished is true
	 */
	public void step()
	{
		if (hasStarted && nextEffects != null && nextEffects.length() == 0)
		{
			hasFinished = true; // don't attempt to run steps that aren't there
		}
		
		if (hasFinished)
		{
			// do nothing
			return;
		}
		
		if (!hasStarted)
		{
			hasStarted = true;
			Step start = (Step)DataProc.getDataObj("STEP_1");
			playerChar.setLastStep(start);
			nextEffects = this.runEffect(start.getEffects(), "");
			System.out.println("Next effects : " + nextEffects);
		}
		else
		{
			nextEffects = this.runEffect(nextEffects, "");
			System.out.println("Next effects : " + nextEffects);
		}
	}
	
	/**
	 * Emulates a dice roll
	 * @param numSides upper limit of the dice roll, will return number from 1 to numSides, inclusive
	 * @param rollMessage Prompt/Message relevant to the roll, will display if interactive choice mode triggers
	 * @param forceRoll If true, is always a true random roll, if false, and if isRolling is false, prompts the user to make an interactive choice
	 * @return
	 */
	protected int rollDice(int numSides, String rollMessage, boolean forceRoll)
	{
		int roll = -1;
		
		// interactive mode if both true
		if (!forceRoll && !this.isRolling)
		{
			String result = "start";
			
			// this won't check for out of range, but we'll implement that later
			while ( !Utils.isInteger(result) && (Integer.parseInt(result) < 0 || Integer.parseInt(result) > numSides) ) 
			{
				result = UIObject.promptUser("Choose a result (valid number for 1d"+numSides+"):", rollMessage);
			}
			
			roll = Integer.parseInt(result);
			
		}
		else
		{
			int bonus = 0;
			
			if (playerChar.hasVar("diceBonus"))
			{
				String diceB = playerChar.getVar("diceBonus");
				if (Utils.isInteger(diceB))
				{
					bonus = Integer.parseInt(diceB);
					
					// we assume we never want to go above the upper numSides, so bonus has to be stopped from being >= numSides
					if (bonus >= numSides)
					{
						throw new IllegalArgumentException("diceBonus can't be larger than the dice being rolled!");
					}
				}
				
				// generally we want diceBonus to expire as soon as it's used unless permanent flag is there
				if (!playerChar.hasVar("diceBonusPerm"))
				{
					playerChar.removeVar("diceBonus");
				}
			}
			
			int adjustment = 1 + bonus; // we start lower bound at 1, not zero, by default, bonus pushes it upwards, guaranteeing lower values never hit
			int adjNumSides = numSides - bonus; // keeps us from overflowing the upper bound
			
			roll = rng.nextInt(adjNumSides)+adjustment;
		}
		
		playerChar.addLastRoll(roll);
		
		return (roll);
	}
	
	/**
	 * Handles the roll command : where a list of short results is picked from based on player's roll.
	 * 
	 * @param effect The effect to process
	 * @param errorInfo Current run context of the command in case it needs to throw errors to console
	 * @param forceRoll If true, this will make it always an RNG roll, even if player would normally be choosing the result
	 */
	protected void handleRoll(String effect, String errorInfo, boolean forceRoll)
	{
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
		params = commandName + "," + params;
		
		String[] subparts = Utils.splitCommands(params);
		if (subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
		}
		else if (subparts[1].length() > 0  && subparts[2].length() > 0)
		{
			
			if (! Utils.isInteger(subparts[1]) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");
			}
			
			if (! subparts[2].substring(0,subparts[2].indexOf('=')).matches("[0-9]+-[0-9]+") )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " does not start with a proper roll range");
			}
			
			if (! (subparts[2].split("/").length > 1) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " does not have more than one effect");
			}							
			
			int numDie = Integer.parseInt(subparts[1]);
			String[] effectList = subparts[2].split("/");
			
			// we make a fake table to hold the results since this process functions like an inline table
			ArrayList<TableRow> rows = new ArrayList<TableRow>();
			
			for (int x = 0; x < effectList.length; x++)
			{
				String currEffect = effectList[x];
				String range = currEffect.substring(0, currEffect.indexOf('='));
				String[] bounds = range.split("-");
				
				int low = 0;
				int high = 0;
				
				if (bounds.length == 1)
				{
					low = Integer.parseInt(bounds[0]);
					high = Integer.parseInt(bounds[0]);
				}
				else if (bounds.length == 2)
				{
					low = Integer.parseInt(bounds[0]);
					high = Integer.parseInt(bounds[1]);
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}
				
				// this gets everything after the =
				rows.add(new TableRow(low,high,"",currEffect.substring(currEffect.indexOf('=')+1))); 							
			}
			
			Table temp = new Table("temp",numDie,rows);
			
			int result = this.rollDice(numDie, temp.toString(), forceRoll);
			TableRow rowReturned = temp.findMatch(result);
			String tableEffects = rowReturned.getEffects();
			this.runEffect(tableEffects, rowReturned.getDescription());
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
		}
	}
	
	/**
	 * Rolls a random choice from a table specified in effect
	 * 
	 * @param effect Effect string containing a rollTable command
	 * @param errorInfo Current run context of the command in case it needs to throw errors to console
	 * @param forceRoll If true, this will make it always an RNG roll, even if player would normally be choosing the result
	 */
	protected void handleRollTable(String effect, String errorInfo, boolean forceRoll)
	{
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
		params = commandName + "," + params;
		
		String[] subparts = Utils.splitCommands(params);
		if (subparts.length != 2 && subparts.length != 3)
		{
			throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
		}
		else if (subparts[1].length() > 0 )
		{
			if (! DataProc.dataObjExists(subparts[1]))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " does not exist");
			}
			
			if (! DataProc.getDataObj(subparts[1]).getType().equals("table"))
			{
				throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a table");
			}
			
			playerChar.setCurrentTable(subparts[1]); // this is sometimes needed for conditionals
			
			Table temp = (Table)DataProc.getDataObj(subparts[1]);
			
			if (temp.containsWildCards() && (subparts.length != 3 || subparts[2].length() == 0) )
			{
				throw new IllegalArgumentException("Poorly formatted effect, Table " + subparts[1] + 
													" has wildcards but no wildcard value was specified for this call");
			}
			
			int numDie = temp.getDiceRolled();
			int result = this.rollDice(numDie, temp.toString(),forceRoll);
			TableRow rowReturned = null;
			
			// handle wildcards if one was specified
			if (subparts.length == 3 && subparts[2].length() > 0)
			{
				rowReturned = temp.findMatch(result, subparts[2]);
			}
			else
			{
				rowReturned = temp.findMatch(result);
			}
			
			if (rowReturned == null)
			{
				throw new IllegalStateException("Was unable to procure a TableRow for rollTable("+result+") : " + temp.getName());
			}
			
			String tableEffects = rowReturned.getEffects();
			
			// give the description to the client
			this.UIObject.statusUpdate(rowReturned.getDescription());
			
			this.runEffect(tableEffects, rowReturned.getDescription());
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
		}
	}

}
