/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

/**
 * A listener interface for SimpleScriptInterpreters.
 * This class provides a means for listening on command
 * execution stepping, starting, and runtime errors.
 * @author Matthew Tropiano
 */
public interface SimpleScriptInterpreterListener
{
	/**
	 * Called when an interpreter starts interpreting a script.
	 * @param interpreter	the interpreter that performed this.
	 */
	public void startedInterpret(SimpleScriptInterpreter interpreter);

	/**
	 * Called when an interpreter ends interpreting a script.
	 * @param interpreter	the interpreter that performed this.
	 */
	public void endedInterpret(SimpleScriptInterpreter interpreter);

	/**
	 * Called when an interpreter steps forward on a command.
	 * @param interpreter	the interpreter that performed this.
	 */
	public void steppedForward(SimpleScriptInterpreter interpreter);

	/**
	 * Called when an interpreter breaks on interpreting a script.
	 * @param interpreter	the interpreter that performed this.
	 */
	public void breakInterpret(SimpleScriptInterpreter interpreter);

}
