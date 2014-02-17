/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.commons.list.List;

/**
 * SimpleScript root class. 
 * Contains all necessary elements of a script.
 * @author Matthew Tropiano
 */
public class SimpleScript
{
	/** Label to command index relationship table. */
	protected CaseInsensitiveHashMap<Integer> labelTable;
	/** List of script commands. */
	protected List<Command> commands;
	/** List of script metadata values. */
	protected CaseInsensitiveHashMap<String> metaDataTable;
	
	/**
	 * Creates a new SimpleScript object with no
	 * commands nor labels.
	 */
	public SimpleScript()
	{
		labelTable = new CaseInsensitiveHashMap<Integer>();
		commands = new List<Command>();
		metaDataTable = new CaseInsensitiveHashMap<String>();
	}
	
	/**
	 * Creates a new SimpleScript object with a
	 * set of commands, but no labels. Commands are added in the order
	 * that they appear in the list.
	 */
	public SimpleScript(Command[] commandList)
	{
		labelTable = new CaseInsensitiveHashMap<Integer>();
		commands = new List<Command>(commandList.length);
		for (Command c : commandList)
			commands.add(c);
	}
	
	/**
	 * Returns the command index associated with a label.
	 * @return the associated index or -1 if the label is not defined.
	 */
	public int getIndexByLabel(String label)
	{
		if (!labelTable.containsKey(label))
			return -1;
		return labelTable.get(label);
	}
	
	/**
	 * Sets metadata key and an associated value.
	 * The key is case-insensitive.
	 * If the key is already defined, it is replaced with
	 * the new associated value. If the value is null,
	 * the key is removed.
	 */
	public void setMetaData(String key, String value)
	{
		if (value == null)
			metaDataTable.removeUsingKey(key);
		else
			metaDataTable.put(key, value);
	}
	
	/**
	 * Gets an associated value from a desired key.
	 * The key is case-insensitive.
	 */
	public String getMetaData(String key)
	{
		return metaDataTable.get(key);
	}
	
	/**
	 * Sets a label and its associated command index.
	 * If the label is already defined, it is replaced with
	 * the new associated index.
	 * Labels are NOT case-sensitive.
	 */
	public void setLabel(String label, int index)
	{
		labelTable.put(label, index);
	}
	
	/**
	 * Removes a label and its associated command index.
	 * If the label does not exist, this does nothing.
	 * Labels are NOT case-sensitive.
	 */
	public void clearLabel(String label)
	{
		labelTable.removeUsingKey(label);
	}
	
	/**
	 * Adds a command to the end of this script.
	 */
	public void addCommand(Command command)
	{
		commands.add(command);
	}
	
	/**
	 * Adds a command to a specific index in this script.
	 * This puts the commands at the selected index after
	 * the added command.
	 */
	public void addCommand(int index, Command command)
	{
		commands.add(index, command);
	}
	
	/**
	 * Removes a command from a specific index in this script.
	 */
	public void removeCommand(int index, Command command)
	{
		commands.removeIndex(index);
	}
	
	/**
	 * Gets the command at a specific index in the script. 
	 * @return the command at the desired index or <code>null</code>
	 * 			if no such command at that index.
	 */
	public Command getCommand(int index)
	{
		if (index < 0 || index >= commands.size())
			return null;
		return commands.getByIndex(index);
	}
	
}
