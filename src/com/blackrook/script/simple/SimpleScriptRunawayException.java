/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

/**
 * An exception that is thrown by a SimpleScriptInterpreter
 * if the script is considered a "runaway" script. This protection
 * method is in place in order to prevent scripts from entering an infinite
 * loop and hogging system resources.
 * @author Matthew Tropiano
 */
public class SimpleScriptRunawayException extends RuntimeException
{
	private static final long serialVersionUID = -476664325584400457L;

	/** The script interpreter that this occurred on. */
	private SimpleScriptInterpreter scriptInterpreter;
	
	public SimpleScriptRunawayException()
	{
		super("A runaway script was detected!");
	}

	public SimpleScriptRunawayException(String message, SimpleScriptInterpreter interpreter)
	{
		super(message);
		scriptInterpreter = interpreter;
	}
	
	/**
	 * The script interpreter that this occurred on.
	 */
	public SimpleScriptInterpreter getScriptInterpreter()
	{
		return scriptInterpreter;
	}

}
