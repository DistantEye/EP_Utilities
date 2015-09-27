package com.github.distanteye.ep_utils.core;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.github.distanteye.ep_utils.commands.Command;
import com.github.distanteye.ep_utils.commands.CommandBuilder;
import com.github.distanteye.ep_utils.commands.directives.Directive;
import com.github.distanteye.ep_utils.commands.directives.DirectiveBuilder;
// explicit because name ambiguity
import com.github.distanteye.ep_utils.containers.*;
import com.github.distanteye.ep_utils.ui.UI;
/**
 * The class takes in the appropriate information,
 * and then runs the lifepath process to interactively generate a character
 * through various stages,packages, and tables
 * 
 * @author Vigilant
 *
 */
public class EpEnvironment implements CharacterEnvironment {

	private SecureRandom rng = new SecureRandom();
	private EpCharacter playerChar;
	private String characterBackup;
	private UI UIObject;
	private boolean isRolling;
	private String nextEffects; // used to store things between steps
	private boolean hasStarted;
	private boolean hasFinished;
	private String stepSkipTo; // keeps track of if a stepskip was triggered and to what the item to jump to is.
	private boolean noStop;
	private ArrayList<String> choiceEffects; // keeps track of the original state of input choices the player had made incase we need to back stuff out because error
	private boolean effectsNeedReturn; // signifies the need to return from runEffects immediately instead of continuing to loop

	/**
	 * Creates the LifePathGenerator
	 * 
	 * @param characterName Name of the character being genned
	 * @param UIObject_ Valid object that can perform user interaction (prompts, alerts)
	 * @param isRolling Boolean flag for whether dice rolls are rolled, or instead prompts to the user. Not all dice rolls can be manual
	 */
	public EpEnvironment(String characterName, UI UIObject_, boolean isRolling_)
	{
		playerChar = new EpCharacter(characterName,false);
		initNonCharValues(UIObject_,isRolling_);
	}	
	
	/**
	 * Sets most of the default values, except for character. Can be used to reinit this class
	 * when coupled with playerChar.setToDefaults
	 */
	public void initNonCharValues(UI UIObject_, boolean isRolling)
	{
		UIObject = UIObject_;
		this.isRolling = isRolling;
		this.nextEffects = "";
		this.hasStarted = false;
		this.hasFinished = false;
		stepSkipTo = "";
		noStop = false;
		choiceEffects = new ArrayList<String>();
		effectsNeedReturn = false;
	}
	
	public void reset()
	{
		playerChar.setToDefaults();
		initNonCharValues(UIObject, isRolling);
	}
	
	/**
	 * Returns the underlying player's Character object
	 * @return Character object (copy by reference)
	 */
	public EpCharacter getPC() {
		return playerChar;
	}

	/**
	 * Sets a new Character object for the player
	 * @param playerChar Valid character object
	 */
	public void setPC(EpCharacter playerChar) {
		this.playerChar = playerChar;
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
		String modifiedInput = effectInput;		
		
		String[] effects = Utils.splitCommands(modifiedInput, ";");
		
		String pendingEffects = ""; // usually set during the process of rolling a table, sets the logical next step if there's no interrupts							
		
		if (playerChar.getLastStep() != null)
		{
			pendingEffects = playerChar.getLastStep().getNextStep();
		}
		
		ArrayList<String> mainStuff = new ArrayList<String>();
		
		for (String eff : effects)
		{
			String tempEff = eff;
				
			ArrayList<String> buffer = new ArrayList<String>();
			
			boolean didSplit = false;
			
			// this should handle all cases of choice token splitting, no matter how convulted/nested
			// "splitting" refers to making high numbered choice tokens like ?3? into three separate effects of ?1?
			// TODO will this work properly in terms of the while conditional running each time? Make sure.
			while (Pattern.compile("\\?([2-9]+)\\?\\**").matcher(tempEff).find())
			{
				didSplit = true;
				tempEff = Command.splitChoiceTokens(tempEff,buffer);								
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
			
				while (Command.containsChoice(effect))
				{
					choiceEffects.add(effect);
					
					// get user input from UI and replace the choice with a tangible value
					mainStuff.set(i, getChoice(effect, extraContext));					
					effect = mainStuff.get(i);
				}

				// much preprocessing can't be done until the last second because it can be effected by other calls
				while (Directive.containsDirective(effect))
				{
					Directive d = DirectiveBuilder.getDirective(effect);
					String result = d.process(this);
					
					// pattern.quote is needed to avoid problems with control characters
					effect = effect.replaceFirst(Pattern.quote(d.toString()), result);
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
					// this is not a true command so we don't use the infastructure
					Skill temp = Skill.CreateSkillFromString(effect);
					playerChar.addSkill(temp);
				}
				else 
				{
					Command c = CommandBuilder.getCommand(effect);
					String result = c.run(this);
										
					if (this.effectsNeedReturn)
					{
						effectsNeedReturn = false;
						return result;
					}
					
					// we have pending effects that need to run if result isn't 0
					if (result.length() != 0)
					{
						this.runEffect(result, c.getExtraContext());
					}
				}
			}
			catch( Exception e) 
			{
				// do something to prompt the user to fix their error
				boolean response = UIObject.handleError(e.getMessage());
				e.printStackTrace();
				if (response)  // replace with seeing if response says to rollback last effect 
				{
					if (choiceEffects.size() > 0)
					{
						// TODO This doesn't always respond right if the cause of the error wasn't a faulty choice!
						mainStuff.set(i, choiceEffects.get(choiceEffects.size()-1)); // Reset the last choice to it's pre-user input value
																				 // it should always be the last choice that broke things. If not, we tried our best.
					}															 // if it's a problem, can make this more robust later
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
	public int rollDice(int numSides, String rollMessage, boolean forceRoll)
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
	
	/**
	 * Takes an effect String with a choice value inside and prompts the user to fill the wildcard with a valid choice
	 * @param effect Effect/Command string, must contain a choice value or will throw errors
	 * @param extraContext Any additional context to pass to the UI
	 * @return The original effect string with it's first choice Value replaced with user input;
	 */
	protected String getChoice(String effect, String extraContext)
	{
		if (!Command.containsChoice(effect))
		{
			throw new IllegalArgumentException("Effect must contain a choice value!");
		}
		
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
		String promptMsg = Command.effectsToString(effect);
		
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

		return effect.replaceFirst("\\?([0-9]+)\\?[\\*]*", promptRes);
	}
	
	public void backupCharacter()
	{
		characterBackup = getPC().getXML();
	}
	
	public void loadBackup()
	{
		if (characterBackup.length() != 0)
		{
			getUI().loadString(characterBackup);
		}
		else
		{
			throw new IllegalArgumentException("No backup availible to load!");
		}
	}
	
	public boolean isRolling() {
		return isRolling;
	}

	public void setRolling(boolean isRolling) {
		this.isRolling = isRolling;
	}


	public boolean hasStarted() {
		return hasStarted;
	}


	public boolean hasFinished() {
		return hasFinished;
	}

	

	public boolean isHasStarted() {
		return hasStarted;
	}


	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}


	public boolean isHasFinished() {
		return hasFinished;
	}


	public void setHasFinished(boolean hasFinished) {
		this.hasFinished = hasFinished;
	}


	public String getNextEffects() {
		return nextEffects;
	}


	public void setNextEffects(String nextEffects) {
		this.nextEffects = nextEffects;
	}
	
	public SecureRandom getRng() {
		return rng;
	}

	public UI getUI() {
		return UIObject;
	}


	public void setNoStop() {
		this.noStop = true;
	}
	
	public void setStepSkipTo(String stepSkipTo) {
		this.stepSkipTo = stepSkipTo;
	}


	public void setEffectsNeedReturn()
	{
		this.effectsNeedReturn = true; 
	}

}