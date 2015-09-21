package com.github.distanteye.ep_utils.ui;
/**
 * Defines the basic minimum function LifePathGenerator (and potentially other Generator classes)
 * needs from a user interface
 * 
 * @author Vigilant
 */

public interface UI {
	
	/**
	 * The UI receives a request for input by the back-end, obtains input from the user, and returns it. This is intended to block other actions
	 * @param message The message to display to the user
	 * @param extraContext Any additional footnotes that may be relevant
	 * @return String response from the program, usually an number representing the choice taken
	 */
	String promptUser(String message,String extraContext); // asking the user to make a choice
	
	/**
	 * Report an error to the user
	 * @param message Message to display to the user
	 * @return True if continuing, false otherwise
	 */
	boolean handleError(String message); // messages about a parse error
	
	/**
	 * Informs user of major status updates (additions made to the character and the like)
	 * @param message 
	 */
	void statusUpdate(String message); // messages about what's been done to the character
	
	/**
	 * Ends the application
	 */
	void end();
	
	/**
	 * Updates the state of the UI, running checks as appropriate
	 */
	void update();
	
	void loadString(String xml);
}
