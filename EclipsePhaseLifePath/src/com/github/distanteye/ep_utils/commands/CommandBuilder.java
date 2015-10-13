/**
 * 
 */
package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.containers.Skill;
import com.github.distanteye.ep_utils.core.Utils;

/**
 * Static only class responsible for building Command objects out of input strings
 * @author Vigilant
 *
 */
public class CommandBuilder {

	/**
	 * Takes in input string, and runs it against list of possible commands,
	 * returning the appropriate Command subclass that best matches
	 * @param input Effects string matching a SINGLE valid command. 
	 * 			Will throw errors if multiple or unknown commands provided
	 * @return Command object matching the input string
	 */
	public static Command getCommand(String input)
	{
		// adding new skills is a special case
		if (Skill.isSkill(input))
		{
			return new NewSkillCommand(input);
		}
		
		String lcEffect = Command.getCommandName(input).toLowerCase();		
		
		if (Utils.splitCommands(input, ";").length > 1)
		{
			return new CommandList(input);
		}
		else if (lcEffect.startsWith("setskl"))
		{
			 return new SetSklCommand(input);
		}
		else if (lcEffect.startsWith("incskl"))
		{
			return new IncSklCommand(input);
		}
		else if (lcEffect.startsWith("decskl"))
		{
			return new DecSklCommand(input);
		}
		else if (lcEffect.startsWith("sklspec"))
		{
			return new SklSpecCommand(input);
		}
		else if (lcEffect.startsWith("trait"))
		{
			return new TraitCommand(input);
		}
		else if (lcEffect.startsWith("morph"))
		{
			return new MorphCommand(input);
		}
		else if (lcEffect.startsWith("setapt"))
		{
			return new SetAptCommand(input);
		}
		else if (lcEffect.startsWith("addapt"))
		{
			return new AddAptCommand(input);
		}
		else if (lcEffect.startsWith("rolltable"))
		{
			return new RollTableCommand(input);	
		}
		else if (lcEffect.startsWith("roll"))
		{
			return new RollCommand(input);					
		}
		else if (lcEffect.startsWith("forcerolltable"))
		{
			Command temp = new RollTableCommand(input);
			temp.setForceRoll(true);
			return temp;
		}
		else if (lcEffect.startsWith("forceroll"))
		{
			Command temp = new RollCommand(input);
			temp.setForceRoll(true);
			return temp;
		}
		else if (lcEffect.startsWith("runtable"))
		{
			return new RunTableCommand(input);
		}
		else if (lcEffect.startsWith("mox"))
		{
			return new MoxCommand(input);
		}
		else if (lcEffect.startsWith("setmox"))
		{
			return new SetMoxCommand(input);
		}
		else if (lcEffect.startsWith("gear"))
		{
			return new GearCommand(input);
		}
		else if (lcEffect.startsWith("background"))
		{
			return new BackgroundCommand(input);
		}
		else if (lcEffect.startsWith("nextpath"))
		{
			return new NextPathCommand(input);
		}
		else if (lcEffect.startsWith("faction"))
		{
			return new FactionCommand(input);
		}
		else if (lcEffect.startsWith("stepskip"))
		{
			return new StepSkipCommand(input);
		}
		else if (lcEffect.startsWith("package"))
		{
			return new PackageCommand(input);
		}
		else if (lcEffect.startsWith("rep"))
		{
			return new RepCommand(input);
		}
		else if (lcEffect.startsWith("credit"))
		{
			return new CreditCommand(input);
		}
		else if (lcEffect.startsWith("psichi"))
		{
			return new PsiChiCommand(input);
		}
		else if (lcEffect.startsWith("psigamma"))
		{
			return new PsiGammaCommand(input);
		}
		else if (lcEffect.startsWith("psisleight"))
		{
			return new PsiSleightCommand(input);
		}
		else if (lcEffect.startsWith("extendedchoice"))
		{
			return new ExtendedChoiceCommand(input);
		}
		else if (lcEffect.startsWith("if"))
		{
			return new IfCommand(input);
		}
		else if (lcEffect.startsWith("func"))
		{
			return new FuncCommand(input);
		}
		else if (lcEffect.startsWith("msgclient"))
		{
			return new MsgClientCommand(input);
		}
		else if (lcEffect.startsWith("setvar"))
		{
			return new SetVarCommand(input);
		}
		else if (lcEffect.startsWith("incvar"))
		{
			return new IncVarCommand(input);
		}
		else if (lcEffect.startsWith("remvar"))
		{
			return new RemVarCommand(input);
		}
		else if (lcEffect.startsWith("stop"))
		{
			return new StopCommand(input);
		}
		else if (lcEffect.startsWith("backupcharacter"))
		{
			return new BackupCharCommand(input);
		}
		else if (lcEffect.startsWith("loadcharbackup"))
		{
			return new LoadCharBackupCommand(input);
		}
		else
		{
			throw new IllegalArgumentException("Poorly formated effect " + input);
		}
	}

}
