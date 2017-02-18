package com.sjsu.umlparser.javaparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws FileNotFoundException
    {
    	FileInputStream in = new FileInputStream("test.java");

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);
        System.out.println( "Hello World!" );
        System.out.println(cu.toString());
    }
}
