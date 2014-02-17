/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.script.test;

import com.blackrook.script.simple.SimpleScript;
import com.blackrook.script.simple.SimpleScriptEngine;
import com.blackrook.script.simple.SimpleScriptFactory;
import com.blackrook.script.simple.SimpleScriptInterpreter;
import com.blackrook.script.simple.control.ControlDescriptor;
import com.blackrook.script.simple.control.ControlInterpreter;

public class Test
{
	public static void main(String[] args) throws Exception
	{
		SimpleScript ss = SimpleScriptFactory.readScript("testscript.txt", 
				ClassLoader.getSystemResourceAsStream("com/blackrook/script/test/testscript.txt"),
				new ControlDescriptor());
		
		SimpleScriptEngine engine = new SimpleScriptEngine(){
			protected void instantiatedScript(SimpleScriptInterpreter interpreter)
			{
				System.out.println("Intantiated "+interpreter);
			}
			
			protected void freedScript(SimpleScriptInterpreter interpreter)
			{
				System.out.println("Freed "+interpreter);
			}

	};
		engine.setInterpreterType("butt", ControlInterpreter.class);
		engine.addScript("test", ss);
		engine.callScript("test");
		engine.go();
	}

}
