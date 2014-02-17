/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.script.simple.Command.Argument.Type;

/**
 * A script descriptor for catching pre-runtime errors
 * when the script is read. 
 * @author Matthew Tropiano
 */
public class SimpleScriptDescriptor
{
	/** The command to entry table. */
	private CaseInsensitiveHashMap<Entry> entryTable;
	
	/**
	 * Creates a new ScriptDescriptor.
	 */
	public SimpleScriptDescriptor()
	{
		entryTable = new CaseInsensitiveHashMap<Entry>();
	}
	
	/**
	 * Adds/replaces a command entry in this ScriptDescriptor.
	 * Commands are NOT case-sensitive.
	 * You can also provide a list of argument types. If so,
	 * the script reader will attempt to match the provided type that
	 * corresponds to the proper argument. If any of the types are null,
	 * it accepts anything in that place.
	 * @param command	the command to add a descriptor for.
	 * @param arguments the amount of arguments in this entry.
	 * @param strict	if this entry REQUIRES the EXACT amount of arguments, and not AT LEAST the amount.
	 * @param argTypes	the list of argument types.
	 */
	public void setCommandEntry(String command, int arguments, boolean strict, Type ... argTypes)
	{
		entryTable.put(command, new Entry(arguments, strict, argTypes));
	}
	
	/**
	 * Removes a command entry from this ScriptDescriptor.
	 * Commands are NOT case-sensitive.
	 * @param command	the command to remove the descriptor for.
	 */
	public void removeCommandEntry(String command)
	{
		entryTable.removeUsingKey(command);
	}
	
	/**
	 * Gets a command entry from this ScriptDescriptor.
	 * Commands are NOT case-sensitive.
	 * @param command	the command to get the descriptor for.
	 * @return the entry for this command or null if the command is not found.
	 */
	public Entry getCommandEntry(String command)
	{
		return entryTable.get(command);
	}
	
	/**
	 * A descriptor entry.
	 */
	public static class Entry
	{
		/** Sets how many arguments belong on this command. */
		private int argumentLength;
		/** Should the command have exactly the amount of specified arguments? */ 
		private boolean strict;
		/** Argument types list for strict types */
		private Type[] argTypes;
		
		/** Creates a new Entry. */
		public Entry(int argumentLength, boolean strict, Type ... argTypes)
		{
			this.argumentLength = argumentLength;
			this.strict = strict;
			this.argTypes = argTypes;
		}
		
		/**
		 * Gets the amount of arguments required for this command.
		 */
		public int getArgumentLength()
		{
			return argumentLength;
		}
		
		/**
		 * Returns true if the parser should accept the argument length
		 * AT the amount, false if AT LEAST the amount.
		 */
		public boolean isStrict()
		{
			return strict;
		}
		
		/**
		 * Returns the list of argument types.
		 */
		public Type[] getArgumentTypes()
		{
			return argTypes;
		}
		
	}
	
}
