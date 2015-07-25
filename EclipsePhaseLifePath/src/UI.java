/**
 * @author Vigilant
 *
 * Defines any class that can use some means to prompt the user to enter a value in response to a question 
 */

public interface UI {
	
	/**
	 * The UI receives a request for input by the back-end, obtains input from the user, and returns it. This is intended to block other actions
	 * @param message The message to display to the user
	 * @param extraContext Any additional footnotes that may be relevant
	 * @return String response from the program, usually an number representing the choice taken
	 */
	public String promptUser(String message,String extraContext); // asking the user to make a choice
	
	/**
	 * Report an error to the user
	 * @param message Message to display to the user
	 * @return True if continuing, false otherwise
	 */
	public boolean handleError(String message); // messages about a parse error
	
	/**
	 * Informs user of major status updates (additions made to the character and the like)
	 * @param message 
	 */
	public void statusUpdate(String message); // messages about what's been done to the character
	
	/**
	 * Ends the application
	 */
	public void end();
}
