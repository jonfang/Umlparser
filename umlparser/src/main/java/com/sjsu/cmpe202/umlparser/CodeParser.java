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
	private HashMap<String, HashMap<String, String>> multi_map; //multiplicity map that stores class relations  
	
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
		//printList();
		get_multiplicity(); //passing in with unmodified lists
        //printList();
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
		 /*
		 for(String attr:variable_list.get("A")){
			 System.out.println(attr);
		 }
		 */
		 for(String class_name:class_list){
			 //System.out.println(class_name);
			 Stack<String> removal_stack = new Stack(); //stack that pops element that has relationships
			 for(String attr:variable_list.get(class_name)){
				 //System.out.println("===>"+attr);
				 for(String c_name:class_list){
					 if(!c_name.equals(class_name) && attr.contains("<") && attr.contains(">") && attr.substring(attr.indexOf("<"), attr.indexOf(">")+1).contains(c_name)){
						 //check if collection of objects
						 //System.out.println("Coll" + attr);
						 multi_map.get(class_name).put(c_name,"*");
						 removal_stack.push(attr);
						 //break;
					 }
					 else if(!c_name.equals(class_name) && attr.contains(c_name)){ //check single instance of object
						 //System.out.println("Single: " + attr);
						 multi_map.get(class_name).put(c_name,"1");
						 removal_stack.push(attr);
						 //break;
					 }
				 } 
			}
			 //remove the classes atrributes that have multiplicity/relationship
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
		 //***Construct the class with attributes and methods
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
		 //***Construct the relationships and multiplicity
		 
		 List<String> tmp_list = new ArrayList();
		 List<String> multi_list = new ArrayList();
		 Stack<String> stack = new Stack();
		 for(String x:multi_map.keySet()){
			for(String y:multi_map.get(x).keySet()){
				tmp_list.add(x + "-" + multi_map.get(x).get(y) + y);
				//System.out.println(x + "-" + multi_map.get(x).get(y) + y);
			}
		 }
		 //construct multiplicity list
		 int class_len = class_list.get(0).length(); //get the length of the class string
		 for(int i=0;i<tmp_list.size();i++){ //merge the list
			 for(int j=i+1;j<tmp_list.size();j++){
				 if(tmp_list.get(i).substring(0, class_len).equals(tmp_list.get(j).substring(tmp_list.get(j).length()-class_len, tmp_list.get(j).length()))){
					 if(tmp_list.get(j).contains("1")){
						 multi_list.add(tmp_list.get(i).substring(0, class_len)+"1"+tmp_list.get(i).substring(class_len,tmp_list.get(i).length()));
					 }
					 else if(tmp_list.get(j).contains("*")){
					 multi_list.add(tmp_list.get(i).substring(0, class_len)+"*"+tmp_list.get(i).substring(class_len,tmp_list.get(i).length()));
					 }
					 stack.push(tmp_list.get(i));
					 tmp_list.remove(j);
					 break; 
				 }
			 }
		 }
		 
		 
		 while(!stack.isEmpty()){
			 tmp_list.remove(stack.pop());
		 }
		 for(String s:tmp_list){
			 //reverse the strings to suit the requirement
			 if(s.contains("1")){
				 s = s.substring(s.length()-class_len, s.length())+"1"+"-"+s.substring(0,class_len); 
			 }
			 else if(s.contains("*")){
				 s = s.substring(s.length()-class_len, s.length())+"*"+"-"+s.substring(0,class_len);
			 }
			 multi_list.add(s);
		 }
		 /*
		 for(String s:multi_list){
			 System.out.println(s);
		 }*/
		 
		 //construct the yuml for multi table
		 for(String s:multi_list){
			 StringBuilder buffer = new StringBuilder();
			 System.out.println("[" + (s.substring(0,class_len) + "]" + (s.substring(class_len,s.length()-class_len) + "[" + (s.substring(s.length()-class_len, s.length()) + "]"))));
			 buffer_list.add("[" + (s.substring(0,class_len) + "]" + (s.substring(class_len,s.length()-class_len) + "[" + (s.substring(s.length()-class_len, s.length()) + "]"))));
			 
		 }
		 
		 
		 yuml_string = new StringBuilder(String.join(",", buffer_list));
	 }
	 
	 public String generateString(){
		 return yuml_string.toString();
	 }
	 
}
