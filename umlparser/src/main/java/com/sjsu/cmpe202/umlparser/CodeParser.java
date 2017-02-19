package com.sjsu.cmpe202.umlparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class CodeParser {

	public CodeParser(){
		
		FileInputStream in = null;
		try {
			in = new FileInputStream("src/test.java");	//read input file
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);

        // prints the resulting compilation unit to default system output
        System.out.println(cu.toString());
        
	}
}
