package com.github.distanteye.ep_utils.commands;

import com.github.distanteye.ep_utils.core.CharacterEnvironment;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.Step;

/**
 * Command of following syntax types:
 * stepskip(<name>)			(immediately skip to step of this name)
 * stepskipNoStop(<name>)			(immediately skip to step of this name, doesn't interrupt the UI)
 * 
 * @author Vigilant
 *
 */
public class StepSkipCommand extends Command {
	private boolean noStop;
	
	/**
	*Creates a command from the given effects string
	* @param input Valid input string, this should be the full String with command name and () still
	*/
	public StepSkipCommand(String input) {
		super(input);

		if (subparts.length != 2)
		{
			throw new IllegalArgumentException("Poorly formated effect (wrong number params) " + input);
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
			
			// special version allows for a clean jump that doesn't interrupt the UI
			if (getCommandName().startsWith("stepskipnostop"))
			{
				noStop = true;
			}
			
			params.set(1, temp);
		}
	}

	public String run(CharacterEnvironment env)
	{
		super.run(env);
		
		if (noStop)
		{
			env.setNoStop();
		}
		
		env.setEffectsNeedReturn();
		
		Step temp = (Step)params.get(1);
		
		return temp.getEffects();
	}
	
	public boolean isNoStop() {
		return noStop;
	}

	public void setNoStop(boolean noStop) {
		this.noStop = noStop;
	}

	public String toString()
	{
		String addendum = "";
		
		if (noStop)
		{
			addendum = " without pausing the UI";
		}
		
		return "Skip to step(" + ((Step)params.get(1)).getName() + ")" + addendum;
	}
	
}
