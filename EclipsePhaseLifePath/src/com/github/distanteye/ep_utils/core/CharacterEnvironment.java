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
	
}
