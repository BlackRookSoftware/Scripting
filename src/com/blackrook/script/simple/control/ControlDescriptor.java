/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple.control;

import com.blackrook.script.simple.SimpleScriptDescriptor;
import com.blackrook.script.simple.Command.Argument.Type;
import static com.blackrook.script.simple.control.ControlInterpreter.*;

/**
 * The descriptor to use for ControlInterpreter scripts.
 * @author Matthew Tropiano
 */
public class ControlDescriptor extends SimpleScriptDescriptor
{
	/** Creates a new control script descriptor. */
	public ControlDescriptor()
	{
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOTO], 1, true, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOSUB], 1, true, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_RETURN], 0, true);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_END], 0, true);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_PRINT], 1, false, (Type)null);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_PRINTLN], 1, false, (Type)null);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_SET], 2, true, Type.IDENTIFIER, (Type)null);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_INC], 1, true, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_DEC], 1, true, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOLESS], 3, true, (Type)null, (Type)null, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOGTR], 3, true, (Type)null, (Type)null, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOEQ], 3, true, (Type)null, (Type)null, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GONEQ], 3, true, (Type)null, (Type)null, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOLESSEQ], 3, true, (Type)null, (Type)null, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_GOGTREQ], 3, true, (Type)null, (Type)null, Type.IDENTIFIER);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_BREAK], 0, true);
		setCommandEntry(CONTROL_COMMANDS[COMMAND_WAIT], 1, true);
	}
	
}
