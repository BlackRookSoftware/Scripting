/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

import com.blackrook.commons.linkedlist.Stack;
import com.blackrook.commons.list.List;

/**
 * Abstract class for all interpreters of SimpleScripts.
 * This class contains a means for facilitating command
 * stepping and execution. Interpreters are designed to run
 * in one thread ONLY and are NOT thread-safe.
 * @author Matthew Tropiano
 */
public abstract class SimpleScriptInterpreter
{
	/** Starting script macro. */
	protected static final int SCRIPT_START = -1;
	
	/** The context stack for this interpreter. */
	protected Stack<Context> contextStack;
	/** Runaway limit. */
	private int runawayLimit;
	/** Current amount of commands executed. */
	private int commandCount;
	/** Should this break on next step? */
	private boolean doBreak;
	
	/** List of interpreter listeners. */
	private List<SimpleScriptInterpreterListener> listeners;
	
	/**
	 * Default constructor for all SimpleScriptInterpreters.
	 * The provided SimpleScript will be pushed onto the context stack
	 * with a starting command index -1 and next command index of 0.
	 * This will effectively start the interpreter at the first command
	 * once stepForward() is called.
	 * @since 1.2.0 
	 */
	public SimpleScriptInterpreter()
	{
		listeners = new List<SimpleScriptInterpreterListener>(2);
	}
	
	/**
	 * Constructor for SimpleScriptInterpreter that takes a script.
	 * The provided SimpleScript will be pushed onto the context stack
	 * with a starting command index -1 and next command index of 0.
	 * This will effectively start the interpreter at the first command
	 * once stepForward() is called.
	 * @param script the script to place on the context stack initially. 
	 */
	public SimpleScriptInterpreter(SimpleScript script)
	{
		this();
		setScript(script);
	}
	
	/**
	 * Resets the interpreter's context stack and clears the break flag.
	 * Does not push a new context onto the stack.
	 * @since 1.2.0
	 */
	protected void initialize()
	{
		resetBreak();
		resetCommandCount();
		contextStack = new Stack<Context>();
	}
	
	/**
	 * Sets what script this interpreter is supposed to be iterating through,
	 * and initializes the context stack to start at the beginning of it.
	 * @param script the script to interpret.
	 * @since 1.2.0
	 */
	public void setScript(SimpleScript script)
	{
		setScript(script, 0);
	}

	/**
	 * Sets what script this interpreter is supposed to be iterating through,
	 * and initializes the context stack to start at the specified label.
	 * @param script the script to interpret.
	 * @param startingLabel the starting label.
	 * @since 1.2.0
	 */
	public void setScript(SimpleScript script, String startingLabel)
	{
		setScript(script, script.getIndexByLabel(startingLabel));
	}

	/**
	 * Sets what script this interpreter is supposed to be iterating through,
	 * and initializes the context stack to start at the specified command index.
	 * @param script the script to interpret.
	 * @param index the starting command index.
	 * @since 1.2.0
	 */
	public void setScript(SimpleScript script, int index)
	{
		initialize();
		pushContext(script, SCRIPT_START, index);
	}

	/**
	 * Adds a {@link SimpleScriptInterpreterListener} to this interpreter.
	 */
	public void addListener(SimpleScriptInterpreterListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes a {@link SimpleScriptInterpreterListener} from this interpreter.
	 */
	public void removeListener(SimpleScriptInterpreterListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Pushes a context onto the context stack.
	 * @param script		the script to interpret.
	 * @param startIndex	the starting command index.
	 * @param nextIndex		the next command index.
	 */
	public void pushContext(SimpleScript script, int startIndex, int nextIndex)
	{
		Context context = new Context(script, startIndex, nextIndex);
		contextStack.push(context);
	}
	
	/**
	 * Pushes a new context onto the context stack, copying the reference
	 * of the topmost (current) context's script. The new context's next index is
	 * the provided index.
	 * @param startIndex	the starting command index.
	 */
	public void pushSubroutine(int startIndex)
	{
		Context context = contextStack.peek();
		pushContext(context.script, 0, startIndex);
	}
	
	/**
	 * Pops a context off of the context stack.
	 * @return true if successful, false if the topmost context 
	 * 			is the ONLY context, and no pop was performed.
	 */
	public boolean popContext()
	{
		if (contextStack.size() == 1)
			return false;
		contextStack.pop();
		return true;
	}
	
	/**
	 * Sets the next command index for the current context.
	 * @param index	the next index.
	 */
	public void setNextCommandIndex(int index)
	{
		contextStack.peek().nextIndex = index;
	}
	
	/**
	 * Sets the next command index for the current context,
	 * using a current context's script's label.
	 * @param label the label of the next command index. 
	 * @throws SimpleScriptRuntimeException if the provided label does not exist.
	 */
	public void setNextCommandIndexByLabel(String label)
	{
		int index = getCommandIndexByLabel(label);
		if (index == -1)
			throw new SimpleScriptRuntimeException("Invalid label requested by script: '"+label+"'", this);
		contextStack.peek().nextIndex = index;
	}
	
	/**
	 * Returns the corresponding index of the current context's script's label.
	 * Returns -1 if not found.
	 */
	public int getCommandIndexByLabel(String label)
	{
		Context context = contextStack.peek();
		return context.script.getIndexByLabel(label);
	}
	
	/**
	 * Returns the topmost (active) context in this interpreter.
	 */
	public Context getCurrentContext()
	{
		return contextStack.peek();
	}

	/**
	 * Sets the runaway limit for this interpreter.
	 * The runaway limit defines how many commands this
	 * interpreter can execute before it believes that
	 * an infinite loop may be occurring. Once this limit
	 * is breached on a stepForward() call, a 
	 * {@link SimpleScriptRunawayException} will be thrown.
	 * Setting this to 0 or less implies no limit.
	 * @param limit the new limit (0 or less is no limit).
	 */
	public void setRunawayLimit(int limit)
	{
		runawayLimit = limit;
	}
	
	/**
	 * Resets the command count for this interpreter.
	 * The command count is the amount of commands that this
	 * interpreter has executed.
	 */
	public void resetCommandCount()
	{
		commandCount = 0;
	}

	/**
	 * Sets if the interpreter should break on the next stepForward().
	 */
	public void setBreak()
	{
		doBreak = true;
	}
	
	/**
	 * Returns true if this interpreter should break on next
	 * stepForward(), or false otherwise.
	 */
	public boolean shouldBreak()
	{
		return doBreak;
	}

	/**
	 * Resets the breaking trigger.
	 * <p>
	 * The reason why this is abstracted as a function is that an interpreter could keep multiple break
	 * conditions or a variable break condition. This is called when the interpreter figures out that it needs 
	 * to break using shouldBreak() and before it calls fireBreakInterpret().
	 */
	public void resetBreak()
	{
		doBreak = false;
	}

	/**
	 * Interprets the next command, performing this set of tasks:
	 * <ul>
	 * <li>Tests if we should break. If so, call resetBreak() and return false.</li>
	 * <li>Check if we are at the runaway limit. If so, throw exception.</li>
	 * <li>Set, in the current context, the next index to the current one.</li>
	 * <li>Increments, in the current context, the next index.</li>
	 * <li>Calls executeCommand() with the current command (if any exists, else return false).</li>
	 * <li>Increments the command count.</li>
	 * </ul>
	 * @return true upon successful step. false if interpretation should stop (by break or end is reached). 
	 */
	public boolean stepForward()
	{
		if (contextStack.isEmpty())
		{
			fireEndedInterpret();
			return false;
		}
		
		if (shouldBreak())
		{
			resetBreak();
			fireBreakInterpret();
			return false;
		}
		
		if (runawayLimit > 0 && commandCount >= runawayLimit)
			throw new SimpleScriptRunawayException("Caught runaway script after "+runawayLimit+" steps.", this);
		
		Context context = contextStack.peek();
		if (context.currentIndex == SCRIPT_START)
			fireStartedInterpret();
		
		context.currentIndex = context.nextIndex;
		context.nextIndex++;
		fireSteppedForward();
		
		Command command = context.script.getCommand(context.currentIndex);
		if (command == null)
		{
			fireEndedInterpret();
			return false;
		}
		
		if (!executeCommand(command))
			throw new SimpleScriptRuntimeException("Unknown or unsupported command '"+command.getName()+"'.", this);
		commandCount++;
		return true;
	}
	
	/**
	 * Returns true if this interpreter is on a valid command,
	 * or false if the interpretation of the script is over. 
	 */
	public boolean isActive()
	{
		Context context = contextStack.peek();
		return context.script.getCommand(context.currentIndex) != null;
	}

	/**
	 * Tells this interpreter to keep stepping forward until the end of this script is reached.
	 * This is basically a convenience method for:<br><br>
	 * <code>resetCommandCount(); while (stepForward()) ;</code><br>
	 */
	public void go()
	{
		resetCommandCount();
		while (stepForward()) ;
	}
	
	/**
	 * Called by stepForward() when the next command needs interpreting.
	 * May throw SimpleScriptRuntimeException if something HORRIBLE happens.
	 * If this returns false, and it propagates up to the stepForward() method, 
	 * it will throw a SimpleScriptRuntimeException about an unsupported command.
	 * @param command	the command to interpret.
	 * @return true if this interpreted the command properly or false otherwise.
	 */
	public abstract boolean executeCommand(Command command);
	
	/**
	 * Calls the startedInterpret() method on all bound listeners.
	 */
	protected void fireStartedInterpret()
	{
		for (SimpleScriptInterpreterListener l : listeners)
			l.startedInterpret(this);
	}

	/**
	 * Calls the endedInterpret() method on all bound listeners.
	 */
	protected void fireEndedInterpret()
	{
		for (SimpleScriptInterpreterListener l : listeners)
			l.endedInterpret(this);
	}

	/**
	 * Calls the steppedForward() method on all bound listeners.
	 */
	protected void fireSteppedForward()
	{
		for (SimpleScriptInterpreterListener l : listeners)
			l.steppedForward(this);
	}
	
	/**
	 * Calls the breakInterpret() method on all bound listeners.
	 */
	protected void fireBreakInterpret()
	{
		for (SimpleScriptInterpreterListener l : listeners)
			l.breakInterpret(this);
	}
	
	/**
	 * Context encapsulation.
	 */
	public static class Context
	{
		/** Script to interpret. */
		protected SimpleScript script;
		/** Current command index. */
		protected int currentIndex; 
		/** Next command index. */
		protected int nextIndex;
		
		Context(SimpleScript script, int currentIndex, int nextIndex)
		{
			this.script = script;
			this.currentIndex = currentIndex;
			this.nextIndex = nextIndex;
		}

		public SimpleScript getScript()
		{
			return script;
		}
		
		public int getCurrentIndex()
		{
			return currentIndex;
		}
		
		public int getNextIndex()
		{
			return nextIndex;
		}
		
	}
	
}
