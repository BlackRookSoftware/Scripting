# Black Rook Scripting

Copyright (c) 2009-2017 Black Rook Software. All rights reserved.  
[http://blackrooksoftware.com/projects.htm?name=scripting](http://blackrooksoftware.com/projects.htm?name=scripting)  
[https://github.com/BlackRookSoftware/Scripting](https://github.com/BlackRookSoftware/Scripting)

### Required Libraries

Black Rook Commons 2.14.0+  
[https://github.com/BlackRookSoftware/Common](https://github.com/BlackRookSoftware/Common)

Black Rook Common Lang 2.3.0+  
[https://github.com/BlackRookSoftware/CommonLang](https://github.com/BlackRookSoftware/CommonLang)

### Introduction

The purpose of the Scripting library is to allow programmers to create simple,
yet powerful scripting components for host programs and creating a means to
link them into the host program easily.

### Languages

The current included language in the scripting library is SimpleScript, an
iterative, command-driven script system. Included is a parser and classes
to assist in writing interpreters and readers for specialized SimpleScripts.
Comes with a sample descriptor and interpreter for making realtime scripts
and can serve as a base for more powerful script types.

### Library

Contained in this release is a series of libraries that enable the user to
create scripts and other interpreted data of this type and link them to
a host program.

### Compiling with Ant

To download the dependencies for this project (if you didn't set that up yourself already), type:

	ant dependencies

A *build.properties* file will be created/appended to with the *dev.base* property set.
	
To compile this library with Apache Ant, type:

	ant compile

To make a JAR of this library, type:

	ant jar

And it will be placed in the *build/jar* directory.

### Other

This program and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (blackrook-license.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 
