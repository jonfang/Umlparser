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
    	//CodeParser parser = new CodeParser();
    	UMLGenerator generator = new UMLGenerator();
    	generator.generatePNG();
    }
}