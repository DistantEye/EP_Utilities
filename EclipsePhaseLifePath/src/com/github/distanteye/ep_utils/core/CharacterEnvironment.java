package com.github.distanteye.ep_utils.core;

import java.security.SecureRandom;

import com.github.distanteye.ep_utils.containers.EpCharacter;
import com.github.distanteye.ep_utils.ui.UI;

/**
 * Interface captures the basic requirements for an Environment that runs commands on a Character.
 * Namely : Providing the character being run on, providing a random number generator, and providing a UI object
 * 
 * @author Vigilant
 *
 */
public interface CharacterEnvironment {

	EpCharacter getPC();
	SecureRandom getRng();
	UI getUI();
	
	/**
	 * Emulates a dice roll
	 * @param numSides upper limit of the dice roll, will return number from 1 to numSides, inclusive
	 * @param rollMessage Prompt/Message relevant to the roll, will display if interactive choice mode triggers
	 * @param forceRoll If true, is always a true random roll, if false, and if isRolling is false, prompts the user to make an interactive choice
	 * @return
	 */
	int rollDice(int numSides, String rollMessage, boolean forceRoll);
	
	/**
	 * Resets the CharacterEnvironment to its default state
	 */
	void reset();
	
	void setHasFinished(boolean hasFinished);
	
	/**
	 * Flags the effects processing engine to notStop after finishing this current step
	 */
	void setNoStop();
	
	/**
	 * Flags the effects processing engine that it needs to return control to UI after finishing
	 * the current step. If setNoStop was called, this will be ignored.
	 */
	void setEffectsNeedReturn();
	
	/**
	 * Flags the effects processing engine to skip to the specified effects String, overriding 
	 * default behavior when moving to the next step
	 * @param stepSkipTo Valid command/effects string
	 */
	void setStepSkipTo(String stepSkipTo);
	
	/**
	 * A "quick save" that pushes to an internal String the current character state.
	 * Can be loaded to character with loadBackup
	 */
	public void backupCharacter();

	/**
	 * Takes the backup from backupCharacter and overwrites Character with it
	 * Will not work if backupCharacter hasn't been called yet
	 */
	public void loadBackup();
}
