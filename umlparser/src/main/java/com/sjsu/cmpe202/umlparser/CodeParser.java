package com.sjsu.cmpe202.umlparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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

	private static final String source_file = "src/test/uml-parser-test-1/";
	private StringBuilder yuml_string; //stores the resulted string for diagram generator
	private List<CompilationUnit> cu_list; //stores AST trees of all source codes
	private List<String> class_list; //list that contains all the classes
	private HashMap<String,List<String>> variable_list; //variable list correspond to mapped class
	private HashMap<String, HashMap<String, Integer>> multi_map; //multiplicity map that stores class relations  
	
	public CodeParser(){
		//initialize variables
		 yuml_string= new StringBuilder();
		 cu_list = new ArrayList();
		 class_list = new ArrayList();
		 variable_list = new HashMap();
		 multi_map = new HashMap();
		
		//read sources files  
		readSourceFiles(source_file,cu_list);
		//store classes
		for(CompilationUnit cu:cu_list){
			getClass(cu, class_list);
		}
		for(String class_name:class_list){
			variable_list.put(class_name, new ArrayList()); //Initialize variable list for each class
			multi_map.put(class_name, new HashMap()); //Initialize multiplicity map
		}
		for(int i=0;i<cu_list.size();i++){
			process(cu_list.get(i), class_list.get(i), variable_list);
		}
		/*
		for(String c:class_list){
			System.out.println(c);
		}
		*/
        //process(cu);
		//construct();
		get_multiplicity(); //passing in with unmodified lists
        construct();
        
	}
	
	public void getClass(Node node, List<String> class_list){ //later modify this to interface
		if (node instanceof TypeDeclaration) {
		      // do something with this type declaration
			 ClassOrInterfaceDeclaration class_name = (ClassOrInterfaceDeclaration) node;
			 class_list.add(class_name.getName().toString());
		}
		for(Node child:node.getChildNodes()){
			getClass(child, class_list);
		}
	}
	

	
	public void process(Node node, String class_name, HashMap<String,List<String>> variable_list){
		 if (node instanceof TypeDeclaration) {
		      // do something with this type declaration
			 ClassOrInterfaceDeclaration dec = (ClassOrInterfaceDeclaration) node;
			 //get class name
		 } else if (node instanceof MethodDeclaration) {
		      // do something with this method declaration
			   System.out.println("Method: " + node);
		   } else if (node instanceof FieldDeclaration) {
		      // do something with this field declaration
			   FieldDeclaration field = (FieldDeclaration)node;
			   List<String> var_list = variable_list.get(class_name);
			   StringBuilder var = new StringBuilder();
			   //set visibility
			   if(field.isPrivate()){
				   var.append("-");
			   }
			   else if(field.isProtected()){
				   var.append("+");
			   }
			   //get type
			   String type = field.getCommonType().toString();
			   if(type.contains("[]")){ //array of elements
				   type = type.replace("[]", "(*)");
			   }
			   var.append(type + ":");
			   //get variable
			   for(VariableDeclarator v:field.getVariables()){
				   //*do check later here for multiple variables
				   var.append(v+";");
			   }
			   var_list.add(var.toString());
		   }
		    // Do something with the node
		    for (Node child : node.getChildNodes()){
		    	process(child, class_name, variable_list);
		    }
	}
	 
	 private void readSourceFiles(String path, List<CompilationUnit> cu_list){
		 File folder = new File(path);
		 FileInputStream instream = null;
		 for(File file:folder.listFiles()){
			 if(file.getName().endsWith("java") &&file.isFile()){
					try {
						instream = new FileInputStream(file);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CompilationUnit cu = JavaParser.parse(instream);
					cu_list.add(cu);
					try {
						instream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
			}
		 } 
	 }
	 
	 public void get_multiplicity(){ //get multiplicity
		 for(String class_name:class_list){
			 //System.out.println(class_name);
			 Stack<String> removal_stack = new Stack(); //stack that pops element that has relationships
			 for(String attr:variable_list.get(class_name)){
				 //System.out.println("===>"+attr);
				 for(String c_name:class_list){
					 if(!c_name.equals(class_name) && attr.contains(new String("<" + c_name + ">"))){
						 //check if collection of objects
						 //System.out.println(attr);
						 removal_stack.push(attr);
						 break;
					 }
					 else if(!c_name.equals(class_name) && attr.contains(c_name)){ //check single instance of object
						 //System.out.println(attr);
						 removal_stack.push(attr);
						 break;
					 }
				 }
				 /*
				 while(!removal_stack.isEmpty()){
					 variable_list.get(class_name).remove(removal_stack.pop());
				 }
				 */
				 
			}
			 while(!removal_stack.isEmpty()){
				 variable_list.get(class_name).remove(removal_stack.pop());
			 }
		}
	}
	 
	 public void printList(){
		 for(String class_name:class_list){
			 System.out.println(class_name);
			 System.out.println(variable_list.get(class_name));
		 }
	 }
	 
	 public void construct(){
		 ArrayList<String> buffer_list = new ArrayList();
		 for(String class_name:class_list){
			 StringBuilder buffer = new StringBuilder();
			 buffer.append("[" + class_name);
			 if(!variable_list.get(class_name).isEmpty()){
				 buffer.append("|");
			 }
			 for(String attr:variable_list.get(class_name)){
				 buffer.append(attr);
			 }
			 buffer.append("]");
			 buffer_list.add(buffer.toString());
		 }
		 yuml_string = new StringBuilder(String.join(",", buffer_list));
		 /*
		 for(String class_name:class_list){
			 yuml_string.append("[" + class_name+"|");
			 for(String var: variable_list.get(class_name)){
				 yuml_string.append(var);
			 }
			 yuml_string.append("]"+",");
		 }
		 */
	 }
	 
	 public String generateString(){
		 return yuml_string.toString();
	 }
	 
}
