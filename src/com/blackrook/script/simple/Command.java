/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

/**
 * Command class for a single SimpleScript command.
 * @author Matthew Tropiano
 */
public class Command
{
	/** Source line of the command. */
	private String line;
	/** Source line number of the command. */
	private int lineNumber;
	/** Name of the command. */
	private String name;
	/** List of arguments. */
	private Argument[] arguments;
	
	/**
	 * Creates a new Command encapsulation.
	 * @param name			the command name.
	 * @param arguments		the command's arguments.
	 * @param line			the original line of the command.
	 * @param lineNumber	the original line number.
	 */
	public Command(String name, Argument[] arguments, String line, int lineNumber)
	{
		this.line = line;
		this.lineNumber = lineNumber;
		this.name = name;
		this.arguments = arguments;
	}
	
	/** Returns this command's original line as it appears in the script. */
	public String getLine()
	{
		return line;
	}

	/** Returns this command's original line number in the script. */
	public int getLineNumber()
	{
		return lineNumber;
	}

	/** Gets this command's name. */
	public String getName()
	{
		return name;
	}

	/** Gets this command's argument list. */
	public Argument[] getArguments()
	{
		return arguments;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(line.substring(0, line.length()-1));
		sb.append('\t');
		sb.append("// ");
		sb.append(name);
		sb.append(' ');
		for (Argument a : arguments)
		{
			sb.append(a.type);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	/**
	 * Command argument.
	 */
	public static class Argument
	{
		/** Enumeration of argument types. */
		public enum Type
		{
			IDENTIFIER,
			INTEGER,
			NUMBER,
			STRING
		}
		
		/** Argument value/lexeme. */
		private String value;
		/** Argument type. */
		private Type type;
		
		/**
		 * Creates a new Argument.
		 */
		public Argument(String value, Type type)
		{
			this.value = value;
			this.type = type;
		}
		
		/**
		 * Returns the string value of this argument.
		 */
		public String getValue()
		{
			return value;
		}
		
		/**
		 * Returns the double value of this argument,
		 * if its type is numeric. If not, this returns <code>Double.NaN</code>. 
		 */
		public double getDouble()
		{
			if (!isNumber())
				return Double.NaN;
			try { return Double.parseDouble(value); }
			catch (NumberFormatException e) {return Double.NaN;}
		}
		
		/**
		 * Returns the integer value of this argument,
		 * if its type is numeric. If not, this returns 0. 
		 */
		public int getInt()
		{
			if (!isNumber())
				return 0;
			try { 
				if (value.contains("x"))
					return Integer.parseInt(value.substring(value.indexOf("x")+1), 16);
				return Integer.parseInt(value); 
			} catch (NumberFormatException e) {return 0;}
		}
		
		/**
		 * Returns the float value of this argument,
		 * if its type is numeric. If not, this returns <code>Float.NaN</code>. 
		 */
		public float getFloat()
		{
			if (!isNumber())
				return Float.NaN;
			try { return Float.parseFloat(value); }
			catch (NumberFormatException e) {return Float.NaN;}
		}
		
		/**
		 * Returns true if this is an identifier.
		 */
		public boolean isIdentifier()
		{
			return type == Type.IDENTIFIER;
		}
		
		/**
		 * Returns true if this is a string literal.
		 */
		public boolean isString()
		{
			return type == Type.STRING;
		}
		
		/**
		 * Returns true if this is a numeric literal.
		 * @since 1.1.0
		 */
		public boolean isNumber()
		{
			return type == Type.NUMBER || type == Type.INTEGER;
		}
		
		/**
		 * Returns true if this is an integer literal.
		 * @since 1.1.0
		 */
		public boolean isInteger()
		{
			return type == Type.INTEGER;
		}
		
		/**
		 * Returns true if this is a numeric literal.
		 * @since 1.1.0
		 */
		public boolean isFloat()
		{
			return type == Type.NUMBER;
		}
		
		@Override
		public String toString()
		{
			if (type == Type.STRING)
				return "\""+value+"\"";
			return value;
		}
		
	}
}
