/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple.control;

import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.script.simple.Command;
import com.blackrook.script.simple.SimpleScript;
import com.blackrook.script.simple.SimpleScriptInterpreter;
import com.blackrook.script.simple.SimpleScriptRuntimeException;
import com.blackrook.script.simple.Command.Argument;

/**
 * A special interpreter that processes a set of control commands.
 * This provides the usual set of script control commands like label jumping
 * and value setting and printing to a stream and all that jazz.
 * <p>
 * This interpreter treats identifiers (non-literal values) like variables.
 * <p>
 * The set of commands that are processed by this: (arguments in <i>italics</i> are optional)
 * <p>
 * <table>
 * <tr><td><b>Command</b></td><td><b>Arguments</b></td><td><b>Description</b></td></tr>
 * <tr><td><b>goto</b></td><td>label</td><td>Jumps to the line in a script denoted by a line label.</td></tr>
 * <tr><td><b>gosub</b></td><td>label</td><td>Jumps to the line in a script denoted by a line label. Calling <b>return</b> will jump back to the line that was next in the script before the <b>gosub</b> call.</td></tr>
 * <tr><td><b>return</b></td><td>&nbsp;</td><td>Jumps back to the line that was next in the script before a call to <b>gosub</b> was made.</td></tr>
 * <tr><td><b>end</b></td><td>&nbsp;</td><td>Terminates the script (sets next line to an invalid line).</td></tr>
 * <tr><td><b>print</b></td><td>message</td><td>Sends a message to standard out. It may be a variable or string.</td></tr>
 * <tr><td><b>println</b></td><td>message</td><td>Sends a message to standard out with a newline character appended to the end. It may be a variable or string.</td></tr>
 * <tr><td><b>set</b></td><td>variable, value</td><td>Sets the value of a variable. <i>Value</i> can be another variable.</td></tr>
 * <tr><td><b>inc</b></td><td>variable</td><td>Adds one to the value of a variable.</td></tr>
 * <tr><td><b>dec</b></td><td>variable</td><td>Subtracts one from the value of a variable.</td></tr>
 * <tr><td><b>goless</b></td><td>value1, value2, label</td><td>Jumps to the line in a script denoted by a line label, if <i>value1</i> is strictly less than <i>value2</i>.</td></tr>
 * <tr><td><b>gogtr</b></td><td>value1, value2, label</td><td>Jumps to the line in a script denoted by a line label, if <i>value1</i> is strictly greater than <i>value2</i>.</td></tr>
 * <tr><td><b>goeq</b></td><td>value1, value2, label</td><td>Jumps to the line in a script denoted by a line label, if <i>value1</i> is equal to <i>value2</i>.</td></tr>
 * <tr><td><b>goneq</b></td><td>value1, value2, label</td><td>Jumps to the line in a script denoted by a line label, if <i>value1</i> is not equal to <i>value2</i>.</td></tr>
 * <tr><td><b>golesseq</b></td><td>value1, value2, label</td><td>Jumps to the line in a script denoted by a line label, if <i>value1</i> is less than or equal to <i>value2</i>.</td></tr>
 * <tr><td><b>gogtreq</b></td><td>value1, value2, label</td><td>Jumps to the line in a script denoted by a line label, if <i>value1</i> is greater than or equal to <i>value2</i>.</td></tr>
 * <tr><td><b>break</b></td><td>&nbsp;</td><td>Tells the interpreter to break execution of this script until the next go() call.</td></tr>
 * <tr><td><b>wait</b></td><td>value</td><td>Sets the wait time (in milliseconds or calls to go()) for this script's execution, suspending execution until the interpreter's wait condition is met.</td></tr>
 * </table>
 * 
 * @author Matthew Tropiano
 */
public class ControlInterpreter extends SimpleScriptInterpreter
{
	protected static final String[] CONTROL_COMMANDS = {
		"goto",
		"gosub",
		"return",
		"end",
		"print",
		"println",
		"set",
		"inc",
		"dec",
		"goless",
		"gogtr",
		"goeq",
		"goneq",
		"golesseq",
		"gogtreq",
		"break",
		"wait"
	};

	protected static final int
	COMMAND_GOTO = 		0,
	COMMAND_GOSUB = 	1,
	COMMAND_RETURN = 	2,
	COMMAND_END = 		3,
	COMMAND_PRINT = 	4,
	COMMAND_PRINTLN = 	5,
	COMMAND_SET = 		6,
	COMMAND_INC = 		7,
	COMMAND_DEC = 		8,
	COMMAND_GOLESS = 	9,
	COMMAND_GOGTR = 	10,
	COMMAND_GOEQ = 		11,
	COMMAND_GONEQ = 	12,
	COMMAND_GOLESSEQ = 	13,
	COMMAND_GOGTREQ = 	14,
	COMMAND_BREAK = 	15,
	COMMAND_WAIT = 		16;
	
	/** Hash table containing the commands. */
	private static final CaseInsensitiveHashMap<Integer> COMMAND_HASH = 
		new CaseInsensitiveHashMap<Integer>()
		{{
			for (int i = 0; i < CONTROL_COMMANDS.length; i++)
				put(CONTROL_COMMANDS[i], i);
		}};

	/** The variable list. */
	protected CaseInsensitiveHashMap<Value> variableHash;
	/** The wait time or length of a break. */
	protected int waitTime;
	/** Is the wait time a break count before the script continues via go()? */
	protected boolean waitTimeIsBreakCount;
	
	/** Break time. */
	protected long breakTime; 
	
	/**
	 * Creates a new interpreter for scripts that use Control commands.
	 * @param script the script to start the interpreter with.
	 */
	public ControlInterpreter(SimpleScript script)
	{
		super(script);
		variableHash = new CaseInsensitiveHashMap<Value>();
		waitTimeIsBreakCount = false;
		waitTime = 0;
		breakTime = -1;
	}
	
	@Override
	public boolean executeCommand(Command command)
	{
		Integer cmdIndex = COMMAND_HASH.get(command.getName());
		if (cmdIndex == null)
			return false;
		
		switch (cmdIndex)
		{
			case COMMAND_GOTO:
				doGoto(command);
				break;
			case COMMAND_GOSUB:
				doGoSub(command);
				break;
			case COMMAND_RETURN:
				doReturn(command);
				break;
			case COMMAND_END:
				doEnd(command);
				break;
			case COMMAND_PRINT:
				doPrint(command);
				break;
			case COMMAND_PRINTLN:
				doPrintln(command);
				break;
			case COMMAND_SET:
				doSet(command);
				break;
			case COMMAND_INC:
				doIncrement(command);
				break;
			case COMMAND_DEC:
				doDecrement(command);
				break;
			case COMMAND_GOLESS:
				doGotoLess(command);
				break;
			case COMMAND_GOGTR:
				doGotoGreater(command);
				break;
			case COMMAND_GOEQ:
				doGotoEqual(command);
				break;
			case COMMAND_GONEQ:
				doGotoNotEqual(command);
				break;
			case COMMAND_GOLESSEQ:
				doGotoLessOrEqual(command);
				break;
			case COMMAND_GOGTREQ:
				doGotoGreaterOrEqual(command);
				break;
			case COMMAND_BREAK:
				waitTime = 1;
				break;
			case COMMAND_WAIT:
				doWait(command);
				break;
		}
		
		return true;
	}

	/**
	 * Returns if the time set by WAIT is the amount of breaks
	 * before the script continues (calls to go()). If false, it
	 * is millisecond based.
	 */
	public boolean waitTimeIsBreakCount()
	{
		return waitTimeIsBreakCount;
	}

	/**
	 * Sets if the time set by WAIT is the amount of breaks
	 * before the script continues (calls to go()). If false, it
	 * is millisecond based.
	 */
	public void setWaitTimeIsBreakCount(boolean waitTimeIsBreakCount)
	{
		this.waitTimeIsBreakCount = waitTimeIsBreakCount;
	}

	/**
	 * Sets a variable on this interpreter instance.
	 * Variable names are case-insensitive.
	 * @param name	the name of the variable.
	 * @param value	the value of the variable.
	 */
	public void setVariable(String name, String value)
	{
		Value v = new Value();
		v.set(value);
		variableHash.put(name, v);
	}
	
	/**
	 * Sets a variable on this interpreter instance.
	 * Variable names are case-insensitive.
	 * @param name	the name of the variable.
	 * @param value	the value of the variable.
	 */
	public void setVariable(String name, double value)
	{
		Value v = new Value();
		v.set(value);
		variableHash.put(name, v);
	}
	
	/**
	 * Sets a variable on this interpreter instance.
	 * Variable names are case-insensitive.
	 * @param name	the name of the variable.
	 * @param value	the value of the variable.
	 */
	public void setVariable(String name, long value)
	{
		Value v = new Value();
		v.set(value);
		variableHash.put(name, v);
	}
	
	/**
	 * Sets a variable on this interpreter instance.
	 * Variable names are case-insensitive.
	 * @param name	the name of the variable.
	 * @param value	the value of the variable.
	 */
	public void setVariable(String name, Value value)
	{
		Value v = new Value();
		v.set(value);
		variableHash.put(name, v);
	}
	
	/**
	 * Gets a variable value on this interpreter instance as a double.
	 * Variable names are case-insensitive. If the variable doesn't exist,
	 * this returns the empty string.
	 * @param name	the name of the variable.
	 */
	public Value getVariable(String name)
	{
		if (!variableHash.containsKey(name))
			return new Value(0d);
		return variableHash.get(name);
	}
	
	/**
	 * Gets the string value of an argument, resolving 
	 * its variable value if it is an identifier.
	 */
	public Value getArgumentValue(Argument argument)
	{
		if (argument.isIdentifier())
			return getVariable(argument.getValue());
		else if (argument.isString())
			return new Value(argument.getValue());
		else if (argument.isInteger())
			return new Value(argument.getInt());
		else
			return new Value(argument.getDouble());
	}
	
	@Override
	public boolean shouldBreak()
	{
		if (waitTime > 0)
		{
			if (breakTime == -1)
				breakTime = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	@Override
	public void resetBreak()
	{
		if (waitTimeIsBreakCount)
			waitTime--;
		else
		{
			long bt = System.currentTimeMillis();
			int time = (int)(bt - breakTime);
			waitTime -= time;
			if (waitTime <= 0) bt = -1;
			breakTime = bt;
		}
	}

	/** 
	 * Checks for the correct amount of necessary arguments.
	 * Throws a runtime exception otherwise. 
	 */
	protected void argumentLengthCheck(int expected, Argument[] arguments)
	{
		if (expected > arguments.length)
			throw new SimpleScriptRuntimeException("Expected "+expected+" arguments for command, got "+arguments.length, this);
	}
	
	/** 
	 * Gets the target index from a label.
	 * Throws exceptions if the argument is not an identifier nor a valid label. 
	 */
	protected int indexLabelCheck(Argument argument)
	{
		String label = argument.getValue();
		if (!argument.isIdentifier())
			throw new SimpleScriptRuntimeException("Argument is not an identifier: '"+label+"'", this);
		int index = getCommandIndexByLabel(argument.getValue());
		if (index == -1)
			throw new SimpleScriptRuntimeException("Invalid label requested by script: '"+label+"'", this);
		return index;
	}
	
	/** Performs the GOTO command. */
	protected void doGoto(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		int index = indexLabelCheck(command.getArguments()[0]);
		setNextCommandIndex(index);
	}
	
	/** Performs the GOSUB command. */
	protected void doGoSub(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		int index = indexLabelCheck(command.getArguments()[0]);
		pushSubroutine(index);
	}
	
	/** Performs the RETURN command. */
	protected void doReturn(Command command)
	{
		if (!popContext())
			throw new SimpleScriptRuntimeException("RETURN without GOSUB.", this);
	}
	
	/** Performs the END command. */
	protected void doEnd(Command command)
	{
		setNextCommandIndex(-1);
	}
	
	/** Performs the PRINT command. */
	protected void doPrint(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		System.out.print(getArgumentValue(command.getArguments()[0]));
	}
	
	/** Performs the PRINTLN command. */
	protected void doPrintln(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		System.out.println(getArgumentValue(command.getArguments()[0]));
	}
	
	/** Performs the SET command. */
	protected void doSet(Command command)
	{
		argumentLengthCheck(2, command.getArguments());
		Argument var = command.getArguments()[0];
		Argument var2 = command.getArguments()[1];
		if (!var.isIdentifier())
			throw new SimpleScriptRuntimeException("Attempted SET on a non-variable.", this);
		else
			setVariable(var.getValue(), getArgumentValue(var2));
	}
	
	/** Performs the INC command. */
	protected void doIncrement(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		Argument var = command.getArguments()[0];
		if (!var.isIdentifier())
			throw new SimpleScriptRuntimeException("Attempted INC on a non-variable.", this);
		else
		{
			if (!variableHash.containsKey(var.getValue()))
				setVariable(var.getValue(), 1);
			else
			{
				Value v = variableHash.get(var.getValue());
				if (v.type == Value.TYPE_INTEGER)
					v.add(1);
				else
					v.add(1d);
			}
		}
	}
	
	/** Performs the DEC command. */
	protected void doDecrement(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		Argument var = command.getArguments()[0];
		if (!var.isIdentifier())
			throw new SimpleScriptRuntimeException("Attempted DEC on a non-variable.", this);
		else
		{
			if (!variableHash.containsKey(var.getValue()))
				setVariable(var.getValue(), -1);
			else
			{
				Value v = variableHash.get(var.getValue());
				if (v.type == Value.TYPE_INTEGER)
					v.add(-1);
				else
					v.add(-1d);
			}
		}
	}

	/** Performs the GOLESS command. */
	protected void doGotoLess(Command command)
	{
		Argument[] args = command.getArguments();
		argumentLengthCheck(3, args);
		int index = indexLabelCheck(args[2]);
		Value v1 = getArgumentValue(args[0]);
		Value v2 = getArgumentValue(args[1]);
		if (v1.compareTo(v2) < 0)
			setNextCommandIndex(index);
	}
	
	/** Performs the GOGTR command. */
	protected void doGotoGreater(Command command)
	{
		Argument[] args = command.getArguments();
		argumentLengthCheck(3, args);
		int index = indexLabelCheck(args[2]);
		Value v1 = getArgumentValue(args[0]);
		Value v2 = getArgumentValue(args[1]);
		if (v1.compareTo(v2) > 0)
			setNextCommandIndex(index);
	}
	
	/** Performs the GOEQ command. */
	protected void doGotoEqual(Command command)
	{
		Argument[] args = command.getArguments();
		argumentLengthCheck(3, args);
		int index = indexLabelCheck(args[2]);
		Value v1 = getArgumentValue(args[0]);
		Value v2 = getArgumentValue(args[1]);
		if (v1.compareTo(v2) == 0)
			setNextCommandIndex(index);
	}
	
	/** Performs the GOLESSEQ command. */
	protected void doGotoLessOrEqual(Command command)
	{
		Argument[] args = command.getArguments();
		argumentLengthCheck(3, args);
		int index = indexLabelCheck(args[2]);
		Value v1 = getArgumentValue(args[0]);
		Value v2 = getArgumentValue(args[1]);
		if (v1.compareTo(v2) <= 0)
			setNextCommandIndex(index);
	}
	
	/** Performs the GOGTREQ command. */
	protected void doGotoGreaterOrEqual(Command command)
	{
		Argument[] args = command.getArguments();
		argumentLengthCheck(3, args);
		int index = indexLabelCheck(args[2]);
		Value v1 = getArgumentValue(args[0]);
		Value v2 = getArgumentValue(args[1]);
		if (v1.compareTo(v2) >= 0)
			setNextCommandIndex(index);
	}
	
	/** Performs the GONEQ command. */
	protected void doGotoNotEqual(Command command)
	{
		Argument[] args = command.getArguments();
		argumentLengthCheck(3, args);
		int index = indexLabelCheck(args[2]);
		Value v1 = getArgumentValue(args[0]);
		Value v2 = getArgumentValue(args[1]);
		if (v1.compareTo(v2) != 0)
			setNextCommandIndex(index);
	}
	
	/** Performs the WAIT command. */
	protected void doWait(Command command)
	{
		argumentLengthCheck(1, command.getArguments());
		waitTime = (int)getArgumentValue(command.getArguments()[0]).toDouble();
	}
	
	/** Value class. */
	public static class Value implements Comparable<Value>
	{
		public static final byte
		TYPE_STRING = 0,
		TYPE_FLOAT = 1,
		TYPE_INTEGER = 2;
		
		protected String value;
		protected byte type;

		/** Value constructor for ControlInterpreters. */
		Value()
		{
			value = "0";
			type = TYPE_INTEGER;
		}
		
		/** 
		 * Value constructor for ControlInterpreters.
		 * @since 1.1.0 
		 */
		Value(long n)
		{
			this();
			set(n);
		}

		/** Value constructor for ControlInterpreters. */
		Value(double d)
		{
			this();
			set(d);
		}
		
		/** Value constructor for ControlInterpreters. */
		Value(String s)
		{
			this();
			set(s);
		}
		
		/** Sets this value using another value. */
		public void set(Value v)
		{
			type = v.type;
			value = v.value;
		}

		/** 
		 * Sets this value using a long. Sets internal type to INTEGER.
		 * @since 1.1.0 
		 */
		public void set(long v)
		{
			type = TYPE_INTEGER;
			value = String.valueOf(v);
		}

		/** Sets this value using a double. Sets internal type to FLOAT. */
		public void set(double v)
		{
			type = TYPE_FLOAT;
			value = String.valueOf(v);
		}

		/** Sets this value using a String. Sets internal type to STRING. */
		public void set(String v)
		{
			type = TYPE_STRING;
			value = v;
		}

		/** 
		 * Adds a value to this one using an integer. Does not change internal type.
		 * @since 1.1.0 
		 */
		public void add(long v)
		{
			if (type == TYPE_INTEGER)
				value = String.valueOf(toLong() + v);
			else if (type == TYPE_FLOAT)
				value = String.valueOf(toDouble() + v);
			else
				value += v;
		}

		/** 
		 * Adds a value to this one using a double. 
		 * Changes internal type to FLOAT if it was presently an INTEGER. 
		 */
		public void add(double v)
		{
			if (type == TYPE_INTEGER)
			{
				value = String.valueOf(toDouble() + v);
				type = TYPE_FLOAT;
			}
			else if (type == TYPE_FLOAT)
				value = String.valueOf(toDouble() + v);
			else
				value += v;
		}

		/** 
		 * Adds a value to this one using a String. 
		 * Changes internal type to STRING. 
		 */
		public void add(String v)
		{
			type = TYPE_STRING;
			value += v;
		}

		/** 
		 * Converts this to a long, chopping a double's mantissa.
		 * If this is non-numeric, it will return 0. 
		 * @since 1.1.0 
		 */
		public long toLong()
		{
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				double d = toDouble();
				if (Double.isNaN(d))
					return 0;
				return (long)d;
			}
		}
		
		/** Converts this to a double. */
		public double toDouble()
		{
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}
		
		@Override
		public String toString()
		{
			return value;
		}

		@Override
		public int compareTo(Value v)
		{
			if ((type == TYPE_FLOAT || type == TYPE_INTEGER) && (v.type == TYPE_FLOAT || v.type == TYPE_INTEGER))
			{
				double d1 = Double.parseDouble(value);
				double d2 = Double.parseDouble(v.value);
				return d1 == d2 ? 0 : d1 < d2 ? -1 : 1;
			}
			else
				return value.compareTo(v.value);
		}
		
	}
	
}
