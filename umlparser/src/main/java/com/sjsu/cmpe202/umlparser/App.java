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
        String source_folder = args[0]; //"C:/Users/User/UMLParser_2017/umlparser/src/test/uml-parser-test-5/";
        String output_file = args[1]; //"C:/Users/User/UMLParser_2017/umlparser/src/test/output.png";//args[1];
    	
    	CodeParser parser = new CodeParser(source_folder);
    	System.out.println("=======Output String=======");
    	System.out.println(parser.generateString());
    	//CodeConverter converter = new CodeConverter();
    	UMLGenerator generator = new UMLGenerator(output_file);
    	String yuml_string = parser.generateString();
    	generator.generatePNG(yuml_string);
    }
}