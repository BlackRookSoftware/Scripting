/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

/**
 * An exception that is thrown by a SimpleScriptFactory
 * if an error is encountered during a script parse.
 * @author Matthew Tropiano
 */
public class SimpleScriptParseException extends RuntimeException
{
	private static final long serialVersionUID = 6539325698925904984L;

	public SimpleScriptParseException()
	{
		super("A script parsing exception has occurred!");
	}

	public SimpleScriptParseException(String message)
	{
		super(message);
	}

}
