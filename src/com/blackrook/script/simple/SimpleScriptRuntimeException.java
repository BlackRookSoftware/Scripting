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
 * if an error is encountered during a script execution.
 * @author Matthew Tropiano
 */
public class SimpleScriptRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1753187677482033762L;

	/** The script interpreter that this occurred on. */
	private SimpleScriptInterpreter scriptInterpreter;

	public SimpleScriptRuntimeException()
	{
		super("A script runtime exception has occurred!");
	}

	public SimpleScriptRuntimeException(String message, SimpleScriptInterpreter interpreter)
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
