package com.github.distanteye.ep_utils.core;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// explicit because name ambiguity
import com.github.distanteye.ep_utils.containers.*;
import com.github.distanteye.ep_utils.core.Package;
import com.github.distanteye.ep_utils.ui.UI;

/**
 * The class takes in the appropriate information,
 * and then runs the lifepath process to interactively generate a character
 * through various stages,packages, and tables
 * 
 * @author Vigilant
 *
 */
public class LifePathGenerator {

	public static SecureRandom rng = new SecureRandom();
	private PlayerCharacter playerChar;
	private UI UIObject;
	private boolean isRolling;
	private String nextEffects; // used to store things between steps
	private boolean hasStarted;
	private boolean hasFinished;
	private String stepSkipTo; // keeps track of if a stepskip was triggered and to what the item to jump to is.
	private boolean noStop;
	private ArrayList<String> choiceEffects; // keeps track of the original state of input choices the player had made incase we need to back stuff out because error
	
	/**
	 * Creates the LifePathGenerator
	 * 
	 * @param characterName Name of the character being genned
	 * @param UIObject_ Valid object that can perform user interaction (prompts, alerts)
	 * @param isRolling Boolean flag for whether dice rolls are rolled, or instead prompts to the user. Not all dice rolls can be manual
	 */
	public LifePathGenerator(String characterName, UI UIObject_, boolean isRolling)
	{
		playerChar = new PlayerCharacter(characterName,false);
		UIObject = UIObject_;
		this.isRolling = isRolling;
		this.nextEffects = "";
		this.hasStarted = false;
		this.hasFinished = false;
		stepSkipTo = "";
		noStop = false;
		choiceEffects = new ArrayList<String>();
	}	
	
	
	/**
	 * Returns the underlying player's Character object
	 * @return Character object (copy by reference)
	 */
	public PlayerCharacter getPC() {
		return playerChar;
	}

	/**
	 * Sets a new Character object for the player
	 * @param playerChar Valid character object
	 */
	public void setPC(PlayerCharacter playerChar) {
		this.playerChar = playerChar;
	}


	/**
	 * Recursive function that splits effects with a high numbered choice parameter like ?3? into single choice parameter effects
	 * trait(Mental Disorder (?3?)) would become trait(Mental Disorder (?1?));trait(Mental Disorder (?1?));trait(Mental Disorder (?1?)) 
	 * @param input
	 * @param buffer Initiall blank list used to store 
	 * @return The split effect String, or "" if nothing could be split
	 */
	protected String splitChoiceTokens(String input, ArrayList<String> buffer)
	{
		Pattern groups = Pattern.compile("\\?([2-9]+)\\?\\**");		
		Matcher m = groups.matcher(input);
		if(!m.find())
		{
			return "";
		}
		int val = Integer.parseInt(m.group(1));
		
		// clone effect for every value over 1, so that ?2? leads to two effects with ?1?
		input = input.replaceFirst("\\?" + val + "\\?","?1?");
		for (int i = 0; i < val; i++)
		{
			buffer.add(input);
		}
		
		ArrayList<String> buffer2 = new ArrayList<String>();
		
		for (String str : buffer)
		{
			splitChoiceTokens(str,buffer2);
		}
		
		buffer.addAll(buffer2);
		
		String result = "";
		
		if (buffer.size() == 0)
		{
			return "";
		}
		else
		{
			result = buffer.get(0);
			
			for (int i = 1; i < buffer.size(); i++)
			{
				result += ";" + buffer.get(i);
			}
		}
		
		return result;
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
		
		String pendingEffects = ""; // usually set during the process of rolling a table, sets the logical next step if there's no interrupts							
		
		if (playerChar.getLastStep() != null)
		{
			pendingEffects = playerChar.getLastStep().getNextStep();
		}
		
		ArrayList<String> mainStuff = new ArrayList<String>();
		
		for (String eff : effects)
		{
			String tempEff = eff.replace("!!COMMA!!",",");
				
			ArrayList<String> buffer = new ArrayList<String>();
			
			boolean didSplit = false;
			
			// this should handle all cases of choice token splitting, no matter how convulted/nested
			// "splitting" refers to making high numbered choice tokens like ?3? into three separate effects of ?1?
			// TODO will this work properly in terms of the while conditional running each time? Make sure.
			while (Pattern.compile("\\?([2-9]+)\\?\\**").matcher(tempEff).find())
			{
				didSplit = true;
				tempEff = splitChoiceTokens(tempEff,buffer);								
			}
				
			// if there were no choices split, we have to add the normal effect to the buffer
			if (!didSplit)
			{
				buffer.add(tempEff);
			}
			
			mainStuff.addAll(buffer);
			
		}
		
		for (int i = 0; i < mainStuff.size(); i++)
		{
			String effect = mainStuff.get(i).trim(); // we trim whitespace to make our lives a little more typo-free
			
			try
			{
			
				while (DataProc.containsChoice(effect))
				{
					choiceEffects.add(effect);
						String extraInfo = extraContext;											
					
						// we look for a context bubble (tells user what you might be wanting them to enter)
						if (Pattern.matches("#[^#]+#", effect))
						{							
							extraInfo = Pattern.compile("#[^#]+#").matcher(effect).group(1);
							
							// once done, we remove this from the effect
							effect = effect.replaceFirst("#[^#]+#", "");
						}
						
						// will make assumption user knows that choices are resolved left to right
						// will also remove any asterisks that appeared after since they'll probably interfere
						String promptMsg = DataProc.effectsToString(effect);
						
						// don't use extra info for * type notes if * is not in the String
						if (extraInfo.startsWith("*") && !effect.contains("*")) 
						{
							extraInfo = "";
						}
						
						String promptRes = UIObject.promptUser(promptMsg,extraInfo);
						
						// if they entered blank, we attempt to pick a random (but valid answer)
						if (promptRes.equals(""))
						{
							// if the right stuff are in the prompt, we can do some extra actions
							// check for a few things that lets us provide extra info
							String[] result = DataProc.getExtraPromptOptions(promptMsg,extraInfo);
							if (result != null && result[0].equals("field"))
							{
								String sklName = result[2];
								promptRes = getSkillField(sklName);
							}
							else if (result != null && result[0].equals("skill"))
							{
								Skill temp = Skill.getRandomSkill(rng, 0);
								
								if (Skill.hasCategory(temp.getName(), "Field"))
								{
									temp.setSubtype(getSkillField(temp.getName()));
								}
								
								promptRes = temp.getFullName();
							}
							
							else if (result != null && result[0].equals("skillNoPsi"))
							{
								Skill temp = Skill.getRandomSkill(rng, 0);
								
								while (Skill.hasCategory(temp.getName(), "Psi"))
								{
									temp = Skill.getRandomSkill(rng, 0);
								}
								
								if (Skill.hasCategory(temp.getName(), "Field"))
								{
									temp.setSubtype(getSkillField(temp.getName()));
								}
								
								promptRes = temp.getFullName();
							}
							else if (result != null && result[0].equals("skillPsi"))
							{
								Skill temp = Skill.getRandomSkill(rng, 0);
								
								// TODO probably should make a function that only returns possibilities with a particular category
								while (!Skill.hasCategory(temp.getName(), "Psi"))
								{
									temp = Skill.getRandomSkill(rng, 0);
								}
								
								if (Skill.hasCategory(temp.getName(), "Field"))
								{
									temp.setSubtype(getSkillField(temp.getName()));
								}
								
								promptRes = temp.getFullName();
							}
							else if (result != null && result[0].equals("sleight"))
							{
								ArrayList<Sleight> options = new ArrayList<Sleight>();
								options.addAll(Sleight.sleightList.values());
								
								Sleight temp = options.get(rng.nextInt(options.size()));
								promptRes = temp.getName();
							}
							else if (result != null && result[0].equals("sleightChi"))
							{
								ArrayList<Sleight> options = new ArrayList<Sleight>();
								ArrayList<Sleight> optionsFinal = new ArrayList<Sleight>();
								options.addAll(Sleight.sleightList.values());
								
								
								// remove all not chi sleights
								for (Sleight s : options)
								{
									if (s.getSleightType()==Sleight.SleightType.CHI)
									{
										optionsFinal.add(s);
									}
								}
								
								Sleight temp = optionsFinal.get(rng.nextInt(optionsFinal.size()));
								promptRes = temp.getName();
							}
							else if (result != null && result[0].equals("sleightGamma"))
							{
								ArrayList<Sleight> options = new ArrayList<Sleight>();
								options.addAll(Sleight.sleightList.values());
								ArrayList<Sleight> optionsFinal = new ArrayList<Sleight>();
								
								// remove all not gamma sleights
								for (Sleight s : options)
								{
									if (s.getSleightType()==Sleight.SleightType.GAMMA)
									{
										optionsFinal.add(s);
									}
								}
								
								Sleight temp = optionsFinal.get(rng.nextInt(optionsFinal.size()));
								promptRes = temp.getName();
							}
							else if (result != null && result[0].equals("rep"))
							{
								ArrayList<Rep> options = new ArrayList<Rep>();
								options.addAll(Rep.repTypes.values());
								
								Rep temp = options.get(rng.nextInt(options.size()));
								promptRes = temp.getName();
							}
							else if (result != null && result[0].equals("mentDisorder"))
							{
								Trait t = Trait.getTrait("Mental Disorder", 1);
								
								// we grab the text right after the part in the description where "*Possibilities:" appears
								int len = "*Possibilities: ".length();
								String possibilities = t.getDescription().substring(t.getDescription().indexOf("*Possibilities:")+len).trim();
								String[] options = possibilities.split(",");
								
								promptRes = options[rng.nextInt(options.length)];
							}
						}

						mainStuff.set(i, effect.replaceFirst("\\?([0-9]+)\\?[\\*]*", promptRes));
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
						String randSkill = playerChar.getRandSkill(rng);
						effect = effect.replace("!RANDSKILL!", randSkill);
					}
					else
					{
						throw new IllegalArgumentException("Effect : " + effect + " calls for random skill but the character has no skills!");
					}
				}
				while(effect.contains("!RANDAPT!"))
				{
					effect = effect.replace("!RANDAPT!",playerChar.aptitudes().getRand(rng).getName());
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
				
				String lcEffect = effect.toLowerCase();
				
				if (Skill.isSkill(effect))
				{
					Skill temp = Skill.CreateSkillFromString(effect);
					playerChar.addSkill(temp);
				}
				else if (lcEffect.startsWith("setskl"))
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
				else if (lcEffect.startsWith("incskl"))
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
				else if (lcEffect.startsWith("decskl"))
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
						
						if (! playerChar.incSkill(subparts[1], Integer.parseInt(subparts[2]) * -1) )
						{
							throw new IllegalArgumentException("Poorly formated effect, skill does not exist " + errorInfo);
						}
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					
				}
				else if (lcEffect.startsWith("sklspec"))
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
				else if (lcEffect.startsWith("trait"))
				{
					String[] subparts = Utils.splitCommands(params);
					if (subparts.length < 2 || subparts.length > 3)
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
					else if (subparts.length == 2 && Trait.exists(subparts[1]))
					{
						Trait t = Trait.getTrait(subparts[1], 1);
						playerChar.traits().put(t.getName(),t);
					}
					else if (subparts.length == 2 && Trait.existsPartial(subparts[1]) )
					{
						Trait t = Trait.getTraitFromPartial(subparts[1], 1);
						
						playerChar.traits().put(t.getName(),t);
					}
					else if (subparts.length == 3 && Trait.exists(subparts[1]) )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						Trait t = Trait.getTrait(subparts[1], Integer.parseInt(subparts[2]));
						playerChar.traits().put(t.getName(), t);
					}
					else if (subparts.length == 3 && Trait.existsPartial(subparts[1]) )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						Trait t = Trait.getTraitFromPartial(subparts[1], Integer.parseInt(subparts[2]));
						
						playerChar.traits().put(t.getName(), t);
					}
					else
					{
						throw new IllegalArgumentException("Trait " + subparts[1] + " does not exist, or other formating problem: ("+ errorInfo + ")");
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
						// no need to return anything, this is just to get a new morph
						this.runEffect("rollTable(CHOOSING_A_MORPH)","");
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
				else if (lcEffect.startsWith("setapt"))
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
						
						if (! Aptitude.exists(subparts[1]))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a valid aptitude");
						}
						
						playerChar.aptitudes().get(subparts[1]).setValue(Integer.parseInt(subparts[2]));
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (lcEffect.startsWith("addapt"))
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
						
						if (! Aptitude.exists(subparts[1]))
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
						
						playerChar.incAptitude(subparts[1], Integer.parseInt(subparts[2]));
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (lcEffect.startsWith("rolltable"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRollTable(effect, errorInfo, false);					
				}
				else if (lcEffect.startsWith("roll"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRoll(effect, errorInfo, false);					
				}
				else if (lcEffect.startsWith("forcerolltable"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRollTable(effect, errorInfo, true);					
				}
				else if (lcEffect.startsWith("forceroll"))
				{
					// code moved to function since this is called again for the force roll version
					this.handleRoll(effect, errorInfo, true);					
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
						
						Table temp = (Table)DataProc.getDataObj(subparts[1]);
						
						if (subparts.length == 4)
						{
							TableRow tempRow = temp.findMatch(Integer.parseInt(subparts[2]),subparts[3]);
							// give the description to the client
							this.UIObject.statusUpdate(tempRow.getDescription());
							
							this.runEffect(tempRow.getEffects(), extraContext);
						}
						else
						{
							TableRow tempRow = temp.findMatch(Integer.parseInt(subparts[2]));
							this.UIObject.statusUpdate(tempRow.getDescription());
							this.runEffect(tempRow.getEffects(), extraContext);
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
						playerChar.incMox(Integer.parseInt(subparts[1]));
					}
					else
					{						
						throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");											
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
						playerChar.setMox(Integer.parseInt(subparts[1]));
					}
					else
					{
						throw new IllegalArgumentException("Poorly formatted effect, " + subparts[1] + " is not a number");
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
						playerChar.addGear(subparts[1]);
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
						playerChar.setBackground(subparts[1]);
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
						playerChar.setVar("{path}",subparts[1]);
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
						playerChar.setVar("{faction}",subparts[1]);
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
						if (lcEffect.startsWith("stepskipnostop"))
						{
							noStop = true;
						}
						
						stepSkipTo = temp.getEffects();
						return temp.getEffects();
						
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (lcEffect.startsWith("package"))
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
					if (subparts.length == 2)
					{												
						String pkgEffect = temp.getEffects(1);						
						
						UIObject.statusUpdate("Package added (PP1): " + temp.getName() + " : " + temp.getDescription());
												
						playerChar.incVar("packageVal", 1);
						playerChar.addPackage(new String[]{temp.getName(),"1"});
						this.runEffect(pkgEffect, temp.getSpecialNotes());
					}
					else if (subparts.length == 3 )
					{
						if (! Utils.isInteger(subparts[2]) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is not a number");
						}
						
						if (!temp.getAllEffects().containsKey(Integer.parseInt(subparts[2])))
						{
							throw new IllegalArgumentException("Poorly formatted effect, " + subparts[2] + " is a listed PP for package " + subparts[1]);
						}
						
						int PP = Integer.parseInt(subparts[2]);
						
						String pkgEffect = temp.getEffects(PP);
						
						UIObject.statusUpdate("Package added (PP" + PP + "): " + temp.getName() + " : " + temp.getDescription());
						
						playerChar.incVar("packageVal", PP);
						playerChar.addPackage(new String[]{temp.getName(),""+PP});
						this.runEffect(pkgEffect, temp.getSpecialNotes());
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
				else if (lcEffect.startsWith("credit"))
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
				else if (lcEffect.startsWith("psichi"))
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
						
						if ( Sleight.sleightList.get(subparts[1]).getSleightType()!=Sleight.SleightType.CHI )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " is not a Psi Chi sleight");
						}

						Sleight s = Sleight.sleightList.get(subparts[1]);
						
						playerChar.sleights().put(s.getName(), s);
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
					else if (subparts[1].length() > 0 )
					{
						if (! Sleight.exists(subparts[1] ) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
						}
						
						if (Sleight.sleightList.get(subparts[1]).getSleightType()!=Sleight.SleightType.GAMMA )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " is not a Psi Gamma sleight");
						}
						
						Sleight s = Sleight.sleightList.get(subparts[1]);
						
						playerChar.sleights().put(s.getName(), s);
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
					else if (subparts[1].length() > 0 )
					{
						if (! Sleight.exists(subparts[1] ) )
						{
							throw new IllegalArgumentException("Poorly formatted effect, sleight " + subparts[1] + " does not exist");
						}
						
						Sleight s = Sleight.sleightList.get(subparts[1]);
						
						playerChar.sleights().put(s.getName(), s);
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
						String response = UIObject.promptUser(subparts[1], ""); // response should be an integer
						
						int choice = Integer.parseInt(response);
						
						String[] choiceEffects = subparts[2].split("/");
						
						while (!Utils.isInteger(response) || Integer.parseInt(response) <= 0 || Integer.parseInt(response) > choiceEffects.length)
						{
							response = UIObject.promptUser(subparts[1], ""); 
							
							// response should be an integer
							if (Utils.isInteger(response))
							{
								// only do these when we at least get a number
								choice = Integer.parseInt(response);
								choiceEffects = subparts[2].split("/");
							}																					
						}
							
							this.runEffect(choiceEffects[choice-1].split("=")[1], subparts[1]);
							
						

					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (lcEffect.startsWith("if"))
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
				else if (lcEffect.startsWith("func"))
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
				else if (lcEffect.startsWith("msgclient"))
				{
					String[] subparts = Utils.splitOnce(params, ",");
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
				else if (lcEffect.startsWith("msgclient"))
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
				else if (lcEffect.startsWith("setvar"))
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
						playerChar.incVar(subparts[1],subparts[2]);
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
						playerChar.removeVar(subparts[1]);
					}
					else
					{
						throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
					}
				}
				else if (lcEffect.startsWith("stop"))
				{
					this.hasFinished = true;
					UIObject.end();
				}
				else
				{
					throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
				}	

			}
			catch( Exception e) 
			{
				// do something to prompt the user to fix their error
				boolean response = UIObject.handleError(e.getMessage());
				if (response)  // replace with seeing if response says to rollback last effect 
				{
					mainStuff.set(i, choiceEffects.get(choiceEffects.size()-1)); // Reset the last choice to it's pre-user input value
																				 // it should always be the last choice that broke things. If not, we tried our best.
																				 // if it's a problem, can make this more robust later
					i--;
					
					continue;
				}
				else
				{
					return effectInput; // we spit back out what was put in, so it can be retried later
				}
			}
		}
		
		// this overrides any pendingEffects
		if (stepSkipTo.length() > 0)
		{
			pendingEffects = stepSkipTo;
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
			
			part1 = condition.substring(0, condition.indexOf("&&"));
			part2 = condition.substring(condition.indexOf("&&")+2);
			
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
				return playerChar.traits().containsKeyIgnoreCase(parts[1]);
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
		
		// Reset both of these each step
		noStop = false;
		stepSkipTo = "";
		
		if (!hasStarted)
		{
			hasStarted = true;
			Step start = (Step)DataProc.getDataObj("STEP_1");
			playerChar.setLastStep(start);
			nextEffects = this.runEffect(start.getEffects(), "");
		}
		else
		{
			nextEffects = this.runEffect(nextEffects, "");
		}
		
		// keep going if directed to
		if (noStop)
		{
			step();
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
			while ( !Utils.isInteger(result) || (Integer.parseInt(result) < 0 || Integer.parseInt(result) > numSides) ) 
			{
				result = UIObject.promptUser("Choose a result (valid number for 1d"+numSides+", blank for random):", rollMessage);
				
				if (result.length() == 0)
				{
					result = "" + (rng.nextInt(numSides)+1);
				}
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
			
			Table temp = new Table("temp",numDie,rows, false);
			
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
			int result = this.rollDice(numDie, temp.toStringDescription(),forceRoll);
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
			
			// we wannt to reroll instead  if this would add a package already present
			if (containsDuplicatePackage(tableEffects))
			{
				this.UIObject.statusUpdate("Roll would add an already present package: rerolling!");
				this.handleRollTable(effect, errorInfo, forceRoll);				
				return;
			}
			
			// give the description to the client, unless the suppress flag is true		
			if (!temp.isSuppressDescriptions())
			{
				this.UIObject.statusUpdate(rowReturned.getDescription());
			}
			
			
			this.runEffect(tableEffects, rowReturned.getDescription());
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + errorInfo);
		}
	}

	/**
	 * Does a "quick parse" to see whether the effects would seem to add a package the character already has 
	 * @param effects Valid effects String to search
	 * @return true/false as appropriate
	 */
	protected boolean containsDuplicatePackage(String effects)
	{
		String[] effectsArr = Utils.splitCommands(effects, ";");
		
		for (String eff : effectsArr)
		{
			String effLC = eff.toLowerCase();
			
			if (effLC.startsWith("package"))
			{
				String params = Utils.returnStringInParen(eff);
				
				String[] subparts = Utils.splitCommands(params);
				
				return getPC().hasPackage(subparts[0]);
			}
		}
		
		return false;
	}
	
	/**
	 * Tries to match skillName against a Fields table and return a valid Field name for the skill
	 * @param skillName Skill to search for
	 * @return String Valid FieldName : only the fieldname, not the full 'skill: Field'
	 */
	protected String getSkillField(String skillName)
	{
		String sklName = skillName;
		String sklTable = sklName.toUpperCase()+"_FIELDS";
		if (DataProc.dataObjExists(sklTable) && DataProc.getDataObj(sklTable).getType().equals("table"))
		{
			Table temp = (Table)DataProc.getDataObj(sklTable);
			String res = temp.findMatch(rng.nextInt(temp.getDiceRolled())+1, "").getEffects();  // by replacing with "" we blank the Skill level, we don't want that
			res = res.replace(sklName + ": ", "").trim(); // remove parent skill name, any spaces, etc							
			
			return res;
		}
		else
		{
			throw new IllegalArgumentException("No _FIELDS table defined for " + skillName);
		}
	}	
	
	
	public boolean isRolling() {
		return isRolling;
	}

	public void setRolling(boolean isRolling) {
		this.isRolling = isRolling;
	}

}
