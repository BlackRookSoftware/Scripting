/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.simple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.commons.linkedlist.Queue;


/**
 * A conglomeration of loaded scripts and mappings to Interpreter
 * classes. Scripts are bound to interpreters using metadata clauses
 * inside the scripts.
 * @author Matthew Tropiano
 */
public class SimpleScriptEngine
{
	/** Metadata key for deciding what interpreter to use. */
	public static final String METADATA_KEY = "type";
	
	/** Table of script files/resource paths to read scripts. */
	private CaseInsensitiveHashMap<SimpleScript> scriptTable;
	/** Table of types to interpreter classes. */
	private CaseInsensitiveHashMap<Class<? extends SimpleScriptInterpreter>> scriptInterpreterTable;
	/** List of active interpreters. */
	private Queue<SimpleScriptInterpreter> activeInterpreters; 
	
	/**
	 * Creates a new simple script engine.
	 */
	public SimpleScriptEngine()
	{
		scriptTable = new CaseInsensitiveHashMap<SimpleScript>();
		scriptInterpreterTable = new CaseInsensitiveHashMap<Class<? extends SimpleScriptInterpreter>>();
		activeInterpreters = new Queue<SimpleScriptInterpreter>();
	}
	
	/**
	 * Assigns a script to a name to be primed later.
	 * @param name the name to bind to a loaded script instance.
	 * @param script the script to use.
	 */
	public void addScript(String name, SimpleScript script)
	{
		scriptTable.put(name, script);
	}

	/**
	 * Gets a script by its bound name.
	 * @param name the name of a loaded script instance.
	 * @return the corresponding instance or null if not found.
	 */
	public SimpleScript getScript(String name)
	{
		return scriptTable.get(name);
	}

	/**
	 * Removes a script from the engine.
	 * @param name the name to use to remove a loaded script instance.
	 */
	public SimpleScript removeScript(String name)
	{
		return scriptTable.removeUsingKey(name);
	}

	/**
	 * Sets the interpreter class to use for scripts of a certain type.
	 * If the type is already bound to a particular interpreter, its binding
	 * is replaced by the new class.
	 * The interpreter class to use MUST contain a constructor that takes
	 * a SimpleScript as an argument.
	 * @param type the script type to link to a particular interpreter.
	 * @param interpClass the interpreter class type to use (if null, it will be unbound from the type).
	 */
	public void setInterpreterType(String type, Class<? extends SimpleScriptInterpreter> interpClass)
	{
		if (interpClass == null)
			scriptInterpreterTable.removeUsingKey(type);
		else
			scriptInterpreterTable.put(type, interpClass);
	}
	
	/**
	 * Calls a script by its bound name in this Engine, loading it into the list
	 * of active scripts and started once go() is called.
	 * @param name the name of a loaded script instance.
	 * @return true if the script was instantiated, false otherwise.
	 * @throws SecurityException if the interpreter's constructor cannot be 
	 *         captured or there is no permission to instantiate the interpreter class.
	 * @throws NoSuchMethodException if the interpreter does not have the required constructor.  
	 * @throws InvocationTargetException if an exception occurs upon the calling of the constructor.
	 * @throws IllegalAccessException if there is no permission to call the constructor.
	 * @throws InstantiationException if the class could not be instantiated.
	 */
	public synchronized boolean callScript(String name) throws NoSuchMethodException, 
		InvocationTargetException, InstantiationException, IllegalAccessException
	{
		return callScript(name, null);
	}
	
	/**
	 * Calls a script by its bound name in this Engine at a starting label, loading it into the list
	 * of active scripts and started once go() is called.
	 * @param name the name of a loaded script instance.
	 * @return true if the script was instantiated, false otherwise.
	 * @throws SecurityException if the interpreter's constructor cannot be 
	 *         captured or there is no permission to instantiate the interpreter class.
	 * @throws NoSuchMethodException if the interpreter does not have the required constructor.  
	 * @throws InvocationTargetException if an exception occurs upon the calling of the constructor.
	 * @throws IllegalAccessException if there is no permission to call the constructor.
	 * @throws InstantiationException if the class could not be instantiated.
	 */
	public synchronized boolean callScript(String name, String startLabel) throws NoSuchMethodException, 
		InvocationTargetException, InstantiationException, IllegalAccessException
	{
		SimpleScript script = scriptTable.get(name);
		if (script == null) return false;

		String type = script.getMetaData(METADATA_KEY);
		Class<? extends SimpleScriptInterpreter> c = scriptInterpreterTable.get(type);
		if (c == null) return false;
		
		Constructor<? extends SimpleScriptInterpreter> constr = c.getConstructor(SimpleScript.class);
		SimpleScriptInterpreter interp = constr.newInstance(script);
		if (startLabel != null)
			interp.setNextCommandIndexByLabel(startLabel);
		activeInterpreters.add(interp);
		instantiatedScript(interp);
		return true;
	}
	
	/**
	 * Calls go() on each active script.
	 */
	public synchronized void go()
	{
		// don't bother doing all that crap if it's empty.
		if (activeInterpreters.isEmpty())
			return;
		
		Iterator<SimpleScriptInterpreter> it = activeInterpreters.iterator();
		SimpleScriptInterpreter interp = null;
		while (it.hasNext())
		{
			interp = it.next();
			try {
				interp.go();
				if (!interp.isActive())
				{
					freedScript(interp);
					it.remove();
				}
			} catch (SimpleScriptRunawayException exception) {
				errorRunawayScript(exception);
				freedScript(interp);
				it.remove();
			} catch (SimpleScriptRuntimeException exception) {
				errorRuntimeScript(exception);
				freedScript(interp);
				it.remove();
			} catch (Exception exception) {
				errorScript(exception);
				freedScript(interp);
				it.remove();
			}
		}
	}
	
	/**
	 * Called when a script is instantiated via a callScript() call.
	 * Does nothing by default, should be overridden.
	 */
	protected void instantiatedScript(SimpleScriptInterpreter interpreter)
	{
		// Do nothing.
	}
	
	/**
	 * Called when a script is freed after a call to go() figures out 
	 * that a script is now inactive and has subsequently been removed.
	 * Does nothing by default, should be overridden.
	 */
	protected void freedScript(SimpleScriptInterpreter interpreter)
	{
		// Do nothing.
	}
	
	/**
	 * Called if a script dies on a runaway script exception.
	 * Does nothing by default, should be overridden.
	 */
	protected void errorRunawayScript(SimpleScriptRunawayException exception)
	{
		// Do nothing.
	}
	
	/**
	 * Called if a script dies on a script runtime exception.
	 * Does nothing by default, should be overridden.
	 */
	protected void errorRuntimeScript(SimpleScriptRuntimeException exception)
	{
		// Do nothing.
	}
	
	/**
	 * Called if a script dies on any other exception.
	 * Does nothing by default, should be overridden.
	 */
	protected void errorScript(Exception exception)
	{
		// Do nothing.
	}
	
	
}
