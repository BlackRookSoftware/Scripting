/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.lang.Lexer;
import com.blackrook.lang.LexerKernel;
import com.blackrook.lang.Parser;
import com.blackrook.script.simple.Command.Argument;
import com.blackrook.script.simple.Command.Argument.Type;
import com.blackrook.script.simple.SimpleScriptDescriptor.Entry;

/**
 * A class that reads in text data and creates SimpleScript objects.
 * <p>
 * All SimpleScripts are comprised of labels and commands.
 * <p>
 * There are no keywords in SimpleScripts - that is defined by a respective SimpleScriptInterpreter.
 * <p>
 * There is only one set of reserved delimiters in a SimpleScript - the colon (<code>:</code>),
 * the double-slash (<code>&#47;&#47;</code>), the slash-star (<code>&#47;&#42;</code>), 
 * the star-slash (<code>&#42;&#47;</code>), and the exclamation point (<code>!</code>).
 * Double-slashes precede line comments. Slash-stars start multi-line comments, 
 * and star-slashes end them. Exclamation points start a line that contain meta
 * data "key : value" pairs. Colons precede label definitions and separate 
 * key-value pairs. 
 * <p>
 * <pre>
 * ! type: robotscript
 * &#47;&#47; This is a comment.
 * &#47;&#42;
 *     This is a multi 
 *     line comment.
 * &#42;&#47;
 * :thisIsALabel
 * : thisIsALabelToo
 * </pre>
 * <p>
 * Label names MUST be identifiers: text that starts with a 
 * letter or underscore and contains NO delimiter characters.
 * <p>
 * Commands are a series of tokens that start with a textual or notational token
 * and are followed by a series of arguments (text, double-or-single-quote-enclosed strings,
 * or numeric data). The following, for instance, are example commands:
 * <pre>
 * add 5 4
 * read "readme.txt"
 * scroll 3 west slowly
 * end
 * shell "dir *.* /s"
 * </pre>
 * Of course, they mean nothing by themselves - it is up to the interpreter 
 * to figure it out. Sometimes interpreters look at non-string-nor-numeric tokens
 * as if they were variables. Some don't care. Blank lines are skipped by the script reader.
 * <p>
 * Once an interpreter reaches the last command or is given an invalid index,
 * it will stop interpreting.
 * @author Matthew Tropiano
 */
public final class SimpleScriptFactory
{
	// Not instantiable.
	private SimpleScriptFactory() {}

	/**
	 * Reads SimpleScript text data and turns it into a SimpleScript.
	 * This will read until the end of the stream is reached.
	 * Does not close the InputStream at the end of the read.
	 * <p>
	 * If no descriptors are supplied, it will blindly accept anything as a valid command.
	 * If at least one descriptor is supplied, it rigidly checks for valid commands.
	 * @param streamName the name of stream that is being read.
	 * @param in the InputStream to read from.
	 * @param descriptors the command descriptors to use, if any.
	 * @throws IOException if an error occurs during the read.
	 * @throws SimpleScriptParseException if a parsing error occurs.
	 */
	public static SimpleScript readScript(String streamName, InputStream in, SimpleScriptDescriptor ... descriptors) throws IOException
	{
		SLexer lexer = new SLexer(streamName, new InputStreamReader(in, "UTF8"));
		SParser parser = new SParser(lexer, descriptors);
		return parser.getScript();
	}
	
	private static class SKernel extends LexerKernel
	{
		public static final int TYPE_COMMENT = 0;
		public static final int TYPE_COLON = 1;
		public static final int TYPE_EXPOINT = 2;
		
		SKernel()
		{
			addDelimiter(":", TYPE_COLON);
			addDelimiter("!", TYPE_EXPOINT);
			addCommentLineDelimiter("//", TYPE_COMMENT);
			addCommentStartDelimiter("/*", TYPE_COMMENT);
			addCommentEndDelimiter("*/", TYPE_COMMENT);
			addStringDelimiter('\"', '\"');
			addStringDelimiter('\'', '\'');
			setIncludeNewlines(true);
		}
	}
	
	private static final SKernel KERNEL = new SKernel();

	/** The lexer that reads script text. */
	private static class SLexer extends Lexer
	{
		/** Creates a new SLexer for tokenizing a script file. */
		public SLexer(String streamName, Reader reader)
		{
			super(KERNEL, streamName, reader);
		}
	}
	
	/** The parser that is used for parsing SimpleScripts. */
	private static class SParser extends Parser
	{
		private static final int STATE_START = 0;
		private static final int STATE_LABEL = 1;
		private static final int STATE_LABEL_END = 2;
		private static final int STATE_COMMAND = 3;
		private static final int STATE_METADATA = 4;
		
		/** The SimpleScript that is generated by this parser. */
		private SimpleScript script;
		/** The current command index. */
		private int currentIndex;
		
		/** Current parser state. */
		private int currentState;
		/** Current parsed command. */
		private String currentCommand;
		/** Current parsed command entry. */
		private Entry currentCommandEntry;
		/** Current parsed command. */
		private Queue<Argument> currentArguments;
		
		/** List of script descriptors. */
		private SimpleScriptDescriptor[] descriptors;
		
		/** Creates a new SParser for SimpleScripts. */
		public SParser(SLexer lexer, SimpleScriptDescriptor ... descriptors)
		{
			super(lexer);
			this.descriptors = descriptors;
			
			script = new SimpleScript();
			currentIndex = 0;
			currentArguments = new Queue<Argument>();

			readScript();
			
			String[] errors = getErrorMessages();
			if (errors.length > 0)
			{
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < errors.length; i++)
				{
					sb.append(errors[i]);
					if (i < errors.length-1)
						sb.append('\n');
				}
				throw new SimpleScriptParseException(sb.toString());
			}
		}
		
		/**
		 * Gets the script created by parsing the script data.
		 */
		public SimpleScript getScript()
		{
			return script;
		}
		
		/** Starts the reading of the script data. */
		protected void readScript()
		{
			currentState = STATE_START;
			nextToken();	// read first token.

			while (currentToken() != null)
			{
				switch (currentState)
				{
					case STATE_START:
						if(!StateStart()) return;
						break;
					case STATE_LABEL:
						if(!StateLabel()) return;
						break;
					case STATE_LABEL_END:
						if(!StateLabelEnd()) return;
						break;
					case STATE_COMMAND:
						if(!StateCommand()) return;
						break;
					case STATE_METADATA:
						if(!StateMetaData()) return;
						break;
				}
			}	
		}

		/** Called in the starting state. */
		protected boolean StateStart()
		{
			if (matchType(SKernel.TYPE_COLON))
			{
				currentState = STATE_LABEL;
				return true;
			}
			else if (matchType(SKernel.TYPE_EXPOINT))
			{
				currentState = STATE_METADATA;
				return true;
			}
			else if (matchType(SKernel.TYPE_DELIM_NEWLINE))
			{
				return true;
			}
			else if (!currentType(SKernel.TYPE_ILLEGAL, SKernel.TYPE_NUMBER, SKernel.TYPE_STRING))
			{
				currentState = STATE_COMMAND;
				currentCommand = currentToken().getLexeme();
				currentArguments.clear();
				if (descriptors.length > 0)
				{
					Entry entry = null;
					for (SimpleScriptDescriptor desc : descriptors)
					{
						entry = desc.getCommandEntry(currentCommand);
						if (entry != null)
							break;
					}
					if (entry == null)
					{
						addErrorMessage("Expected valid command.");
						return false;
					}
					currentCommandEntry = entry;
				}
				nextToken();
				return true;
			}
			
			addErrorMessage("Expected command or label declaration.");
			return false;
		}
		
		/** Called in the label state: saw colon, need label. */
		protected boolean StateLabel()
		{
			if (currentType(SKernel.TYPE_IDENTIFIER))
			{
				script.setLabel(currentToken().getLexeme(), currentIndex);
				currentState = STATE_LABEL_END;
				nextToken();
				return true;
			}

			addErrorMessage("Expected identifier type for label declaration.");
			return false;
		}
		
		/** Called in the label state: saw colon and label, need newline. */
		protected boolean StateLabelEnd()
		{
			if (matchType(SKernel.TYPE_DELIM_NEWLINE))
			{
				currentState = STATE_START;
				return true;
			}
			
			addErrorMessage("Expected end-of-line after label.");
			return false;
		}
		
		/** Called in the command state: saw command, reading arguments. */
		protected boolean StateCommand()
		{
			if (currentType(SKernel.TYPE_NUMBER))
			{
				if (!checkArgument())
					return false;
				
				String lexeme = currentToken().getLexeme();
				try{
					Long.parseLong(lexeme);
					currentArguments.add(new Argument(currentToken().getLexeme(), Argument.Type.INTEGER));
				} catch (NumberFormatException e) {
					currentArguments.add(new Argument(currentToken().getLexeme(), Argument.Type.NUMBER));
				}
				
				nextToken();
				return true;
			}
			else if (currentType(SKernel.TYPE_STRING))
			{
				if (!checkArgument())
					return false;
				currentArguments.add(new Argument(currentToken().getLexeme(), Argument.Type.STRING));
				nextToken();
				return true;
			}
			else if (currentType(SKernel.TYPE_IDENTIFIER))
			{
				if (!checkArgument())
					return false;
				currentArguments.add(new Argument(currentToken().getLexeme(), Argument.Type.IDENTIFIER));
				nextToken();
				return true;
			}
			else if (currentType(SKernel.TYPE_DELIM_NEWLINE))
			{
				if (currentCommandEntry != null)
				{
					boolean str = currentCommandEntry.isStrict();
					int len = currentCommandEntry.getArgumentLength();
					int args = currentArguments.size();
					if ((str && len != args) || (!str && len < args))
					{
						if (str)
							addErrorMessage("Expected "+len+" arguments for command '"+currentCommand+"'.");
						else
							addErrorMessage("Expected at least "+len+" arguments for command '"+currentCommand+"'.");
						return false;
					}
				}
				
				Argument[] args = new Argument[currentArguments.size()];
				currentArguments.toArray(args);
				script.addCommand(new Command(currentCommand, args, 
						currentToken().getLineText(), currentToken().getLine()));
				currentIndex++;
				currentState = STATE_START;
				nextToken();
				return true;
			}
			
			addErrorMessage("Expected valid argument token.");
			return false;
		}
		
		/** Called in the meta data state: saw EXCLAMATION POINT, need KEY, COLON, VALUE. */
		protected boolean StateMetaData()
		{
			if (!currentType(SKernel.TYPE_IDENTIFIER))
			{
				addErrorMessage("Expected identifier for key.");
				return false;
			}
			
			String key = currentToken().getLexeme();
			nextToken();
			
			if (!matchType(SKernel.TYPE_COLON))
			{
				addErrorMessage("Expected ':' after key.");
				return false;
			}
			
			if (!currentType(SKernel.TYPE_IDENTIFIER, SKernel.TYPE_STRING, SKernel.TYPE_NUMBER, SKernel.TYPE_FLOAT))
			{
				addErrorMessage("Expected identifier, string, or numeric value.");
				return false;
			}
			
			String value = currentToken().getLexeme();
			nextToken();

			if (!matchType(SKernel.TYPE_DELIM_NEWLINE))
			{
				addErrorMessage("Expected end-of-line.");
				return false;
			}
			
			script.setMetaData(key, value);
			currentState = STATE_START;
			return true;
		}
		
		/** Checks the argument type. */
		protected boolean checkArgument()
		{
			int type = currentToken().getType();
			int aIndex = currentArguments.size();
			Type[] aTypes = currentCommandEntry.getArgumentTypes();
			Type checkType = null;
			if (aIndex < aTypes.length)
				checkType = aTypes[aIndex];
			
			if (checkType != null) switch (checkType)
			{
				case INTEGER:
					if (type != SKernel.TYPE_NUMBER)
						addErrorMessage("Expected integer numeric argument for command '"+currentCommand+"'.");
					else
					{
						try {
							Long.parseLong(currentToken().getLexeme());
						} catch (NumberFormatException e) {
							addErrorMessage("Expected integer numeric argument for command '"+currentCommand+"'.");
						}
					}
					break;
				case NUMBER:
					if (type != SKernel.TYPE_NUMBER)
						addErrorMessage("Expected numeric argument for command '"+currentCommand+"'.");
					break;
				case IDENTIFIER:
					if (type != SKernel.TYPE_IDENTIFIER)
						addErrorMessage("Expected identifier argument for command '"+currentCommand+"'.");
					break;
				case STRING:
					if (type != SKernel.TYPE_STRING)
						addErrorMessage("Expected string argument for command '"+currentCommand+"'.");
					break;
				default:
					addErrorMessage("An internal error has occurred. You should not even be seeing this.");					
					return false;
			}
			return true;
		}
		
	}
	
}
