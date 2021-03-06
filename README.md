﻿# Java UML Parser
A parser that converts native java code into UML class diagram
***
### Tools and Libraries:
 - Eclipse MARS.2: IDE environment used to write, compile and test the project. Executable jar ic exported through Maven plugin.
 - JavaParser library: library used for parsing java source files. Reference: https://github.com/javaparser/javaparser
 - yUML: online tool that converts specific syntax into corresponding UML class diagram. Reference: https://yuml.me/diagram/scruffy/class/draw
***
### Compilation Instructions
#### Requirements:
- Java JDK/JRE version 1.8
- Working internet connection (yUML site https://yuml.me/diagram/scruffy/class/draw should be accessible)
#### Running the parser:
1. Download the project and locate the umlparser.jar under "output" directory. Source code is in "umlparser" directory
2. Open terminal and navigate to the directory where the jar and test files are located
3. Run the command in the following format:

	java -jar umlparser.jar \<source folder\> \<output file name\>

	\<source folder\> is a folder name where all the .java source files will be
	
	\<output file name\> is the name of the output image file you program will generate
	
	E.g. java -jar umlparser.jar ./test1 ./output.png
