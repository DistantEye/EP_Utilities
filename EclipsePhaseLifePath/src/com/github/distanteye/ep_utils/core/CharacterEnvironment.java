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
	
}
