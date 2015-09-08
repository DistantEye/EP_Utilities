package com.github.distanteye.ep_utils.commands;

/**
 * Command of following syntax types:
 * rollTable(<tableName>)						(replace semicolon, spaces and periods in table name with underscore, e.g. Table_6_5)
 * 										forceRoll and forceRollTable can be used to make sure a user in interactive mode still rolls these 
 * rollTable(<tableName>,<replaceValue>) 	(as before, but <replaceValue will sub in for any wildcards in the table) (wildcard is !!X!!)
 * 
 * @author Vigilant
 *
 */
public class RollTableCommand extends Command {

	/**
	*Creates a command from the given effects string
	* @param input Valid formatted command effect string
	*/
	public RollTableCommand(String input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

}
