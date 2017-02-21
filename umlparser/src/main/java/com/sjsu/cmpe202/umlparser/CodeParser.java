package com.sjsu.cmpe202.umlparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;

public class CodeParser {

	private static final String source_file = "src/test/test.java";
	private StringBuilder yuml_string= new StringBuilder();
	
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
        System.out.println(cu.toString());
        process(cu);
        // prints the resulting compilation unit to default system output
        
	}
	
	 public void process(Node node){
		 if (node instanceof TypeDeclaration) {
		      // do something with this type declaration
			 ClassOrInterfaceDeclaration dec = (ClassOrInterfaceDeclaration) node;
			 //get class name
			 yuml_string.append("[" + dec.getName()+"|");
		 } else if (node instanceof MethodDeclaration) {
		      // do something with this method declaration
			   System.out.println("Method: " + node);
		   } else if (node instanceof FieldDeclaration) {
		      // do something with this field declaration
			   FieldDeclaration field = (FieldDeclaration)node;
			   //set visibility
			   if(field.isPrivate()){
				   yuml_string.append("-");
			   }
			   else if(field.isProtected()){
				   yuml_string.append("+");
			   }
			   //get type
			   String type = field.getCommonType().toString();
			   if(type.contains("[]")){ //array of elements
				   type = type.replace("[]", "(*)");
			   }
			   yuml_string.append(type + ":");
			   //get variable
			   for(VariableDeclarator var:field.getVariables()){
				   //*do check later here for multiple variables
				   yuml_string.append(var+";");
			   }
		   }
		    // Do something with the node
		    for (Node child : node.getChildNodes()){
		    	process(child);
		    }
		    if(node instanceof TypeDeclaration){ //finish
		    	yuml_string.append("]");
		    }
	}
	 
	 public String generateString(){
		 return yuml_string.toString();
	 }
	 
}
