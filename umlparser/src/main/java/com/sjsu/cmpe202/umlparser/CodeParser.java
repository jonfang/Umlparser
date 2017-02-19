package com.sjsu.cmpe202.umlparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CodeParser {

	private static final String source_file = "src/test/test.java";
	
	public CodeParser(){
		FileInputStream in = null;
		try {
			in = new FileInputStream(source_file);	//read input file
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);

        // prints the resulting compilation unit to default system output
        System.out.println(cu.toString());
        new MethodVisitor().visit(cu, null);
        
	}
	
	 private static class MethodVisitor extends VoidVisitorAdapter<Void> {
	        @Override
	        public void visit(MethodDeclaration n, Void arg) {
	            /* here you can access the attributes of the method.
	             this method will be called for all methods in this 
	             CompilationUnit, including inner class methods */
	            System.out.println(n.getName());
	            super.visit(n, arg);
	        }
	    }
}
