package com.sjsu.cmpe202.umlparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
    	/*CodeParser parses source code and converts parsed code to yuml string
    	 *UMLGenerator converts yuml string to UML Diagram
    	 */

    	CodeParser parser = new CodeParser();
    	System.out.println(parser.generateString());
    	//CodeConverter converter = new CodeConverter();
    	UMLGenerator generator = new UMLGenerator();
    	String yuml_string = parser.generateString();
    	generator.generatePNG(yuml_string);
    }
}