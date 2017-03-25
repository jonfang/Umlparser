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
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;

public class CodeParser {

	private static final String source_file = "C:/Users/User/UMLParser_2017/umlparser/src/test/uml-parser-test-3/";
	private StringBuilder yuml_string; //stores the resulted string for diagram generator
	private List<CompilationUnit> cu_list; //stores AST trees of all source codes
	private HashMap<Integer, String> cu_map; //class and interface mapping to AST tree cu_list
	private List<String> class_list; //list that contains all the classes
	private List<String> interface_list; //list that contains all interfaces
	private HashMap<String,List<String>> variable_list; //variable list correspond to mapped class/interface
	private HashMap<String,List<String>> method_list; //method list correspond to mapped class/interface
	private HashMap<String, HashMap<String, String>> multi_map; //multiplicity map that stores class relations  
	private List<String> extends_implements_list; //all the extends and implements strings
	private List<String> use_case_list; //all the interface use cases 
	private HashMap<String, String>set_get_list; //lists contains class to setter/getter mapping
	
	public CodeParser(){
		//initialize variables
		 yuml_string= new StringBuilder();
		 cu_list = new ArrayList();
		 cu_map = new HashMap();
		 class_list = new ArrayList();
		 interface_list = new ArrayList();
		 variable_list = new HashMap();
		 method_list = new HashMap();
		 multi_map = new HashMap();
		 extends_implements_list = new ArrayList();
		 use_case_list = new ArrayList();
		 set_get_list = new HashMap();
		
		//read sources files into cu_list
		readSourceFiles(source_file,cu_list);
		//store classes
		for(int i=0;i<cu_list.size();i++){
			CompilationUnit cu = cu_list.get(i);
			getClassOrInterface(cu, class_list,i); //get the classes and interfaces, extends/implements also handled here
		}
		for(String class_name:class_list){
			variable_list.put(class_name, new ArrayList()); //Initialize variable list for each class
			method_list.put(class_name, new ArrayList()); //Initialize method list for each class
			multi_map.put(class_name, new HashMap()); //Initialize multiplicity map
		}
		
		for(String interface_name:interface_list){
			method_list.put(interface_name, new ArrayList()); 
		}
		
		//process the contents for each source file
		for(int i=0;i<cu_list.size();i++){
			process(cu_list.get(i), cu_map.get(i), variable_list);
		}
		

		//printList();
		get_multiplicity(); //passing in with unmodified lists
		get_useCase();  //get use case 
		rm_protected(); //removed protected/ packaged variables
		set_get();
		printList();
		construct();
        
	}
	
	public void getClassOrInterface(Node node, List<String> class_list, int index){ //get class and interface
			if (node instanceof TypeDeclaration) {
				// do something with this type declaration
				ClassOrInterfaceDeclaration class_interface = (ClassOrInterfaceDeclaration) node;
				if(class_interface.isInterface()){ //check if its an interface
					//System.out.println("Interface: " + class_interface.getName().toString());
					interface_list.add(class_interface.getName().toString());
					cu_map.put(index, class_interface.getName().toString());
			 }
			 else{//is a class
				 //System.out.println("Class: " + class_interface.getName().toString());
				 class_list.add(class_interface.getName().toString());
				 cu_map.put(index, class_interface.getName().toString());
				 //handles the implements and extends here 
				 //System.out.println("Extended: ");
				 for(Node ex:class_interface.getExtendedTypes()){
					 //System.out.println("["+ex + "]^-[" + class_interface.getName().toString() +"]");
					 extends_implements_list.add("["+ex + "]^-[" + class_interface.getName().toString() +"]");
				 }
				 //System.out.println("Implemented: ");
				 for(Node im:class_interface.getImplementedTypes()){
					 //System.out.println("[<<interface>>;" + im + "]^-.-[" + class_interface.getName().toString() +"]");
					 extends_implements_list.add("[<<interface>>;" + im + "]^-.-[" + class_interface.getName().toString() +"]");
				 }
			 }
		}
		for(Node child:node.getChildNodes()){
			getClassOrInterface(child, class_list, index);
		}
	}
	

	
	public void process(Node node, String class_interface_name, HashMap<String,List<String>> variable_list){
		
		if (node instanceof TypeDeclaration) {
		      // do something with this type declaration
			 ClassOrInterfaceDeclaration dec = (ClassOrInterfaceDeclaration) node;
			 //get class name
		 }else if (node instanceof ConstructorDeclaration){
			 ConstructorDeclaration construct = (ConstructorDeclaration)node;
			 List<String> meth_list = method_list.get(class_interface_name);
			 StringBuilder constr = new StringBuilder();
			 if(construct.isPrivate()){ //set visibility
				constr.append("-"); 
			 }
			 else if(construct.isPublic()){
				 constr.append("+");
			 }
			 constr.append(construct.getName()); //set name
			 constr.append("(");
			 for(Parameter para:construct.getParameters()){
				   constr.append(para.toString().replace(" ", ":"));
				   constr.append(",");
			 }
			 if(construct.getParameters().isEmpty()){
				 constr.append(")");
			 }
			 else{
				 constr.replace(constr.length()-1, constr.length(), "");constr.append(")");
			 }
			 //System.out.println(class_interface_name + " Constructor: " + constr.toString());
			 meth_list.add(constr.toString());
		 }
		 else if (node instanceof MethodDeclaration) {
		      // do something with this method declaration
			   MethodDeclaration method = (MethodDeclaration)node;
			   List<String> meth_list = method_list.get(class_interface_name);
			   StringBuilder meth = new StringBuilder();
			   if(method.isPrivate()){ //set visibility
				   meth.append("-");
			   }
			   else if(method.isPublic()){
				   meth.append("+");
			   }
			   meth.append(method.getName()); //set name
			   meth.append("(");
			   for(Parameter para:method.getParameters()){
				   meth.append(para.toString().replace(" ", ":"));
				   meth.append(",");
			   }
			   if(method.getParameters().isEmpty()){
				   meth.append(")");
			   }
			   else{
				   meth.replace(meth.length()-1, meth.length(), "");meth.append(")");
			   }
			   //if(!method.getType().toString().equals("void")){ 
			   meth.append(":" + method.getType().toString());  //set type
			   //}
			   if(meth.toString().contains("main")){ //add main function use case here for test case 5
				  for(Node n: method.getChildNodes()){
					  String tmp_str = n.toString();
					  if(tmp_str.contains("=")){
						  System.out.println(class_interface_name);
						  for(String interface_name:interface_list){
							  if(tmp_str.substring(0, tmp_str.indexOf("=")).contains(interface_name)){
								  System.out.println(interface_name);
								  use_case_list.add("[" + class_interface_name + "]" + "uses -.->" + "[<<interface>>;"+ interface_name + "]");
							  }
						  }
					  }
					  
				  }
			   }
			   
			   //System.out.println(class_interface_name + " Method: " + meth.toString());
			   
			   //handling get and sets
			   if(meth.toString().contains("get")||meth.toString().contains("set")){
				   //if its a getter or setter, ignore
				   set_get_list.put(class_interface_name,meth.toString());
			   }
			   else{
				   meth_list.add(meth.toString());
			   }
			   
		   } else if (node instanceof FieldDeclaration) {
		      // do something with this field declaration
			   FieldDeclaration field = (FieldDeclaration)node;
			   List<String> var_list = variable_list.get(class_interface_name);
			   StringBuilder var = new StringBuilder();
			   //set visibility
			   if(field.isPrivate()){
				   var.append("-");
			   }
			   else if(field.isPublic()){
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
			   if(var.toString().contains("=")){//clear up variables with initialization
				   var = new StringBuilder(var.substring(0, var.indexOf("=")).trim() + ";");
			   }
			   //System.out.println(class_interface_name + " Variables : " + var.toString());
			   var_list.add(var.toString());
		   }
		    // Do something with the node
		    for (Node child : node.getChildNodes()){
		    	process(child, class_interface_name, variable_list);
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
				 for(String c_name:class_list){ //check class
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
				 for(String c_name:interface_list){  //check interface
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
	 
	 public void get_useCase(){
		for(String class_interface_name:method_list.keySet()){
			for(String method:method_list.get(class_interface_name)){
				//System.out.println(method);
				String checkString = new String(method.substring(method.indexOf("("), method.indexOf(")")+1));
				for(String interface_name:interface_list){
					if(checkString.contains(interface_name) && checkString.substring(1,checkString.indexOf(":")).equals(interface_name)){
						if(interface_list.contains(class_interface_name)){//check whether its interface uses or class uses interface
							if(!use_case_list.contains("[<<interface>>;" + class_interface_name + "]" + "uses -.->" + "[<<interface>>;"+ interface_name + "]")){
								use_case_list.add("[<<interface>>;" + class_interface_name + "]" + "uses -.->" + "[<<interface>>;"+ interface_name + "]");
							}
						}
						else{
							if(!use_case_list.contains("[" + class_interface_name + "]" + "uses -.->" + "[<<interface>>;"+ interface_name + "]")){
								use_case_list.add("[" + class_interface_name + "]" + "uses -.->" + "[<<interface>>;"+ interface_name + "]");
							}
						}
					}
				}
			}
		}
	 }
	 
	 public void rm_protected(){
		 Stack<String> removal_stack = new Stack();
		 for(String class_name:class_list){
			 for(int i=0;i<variable_list.get(class_name).size();i++){
				 String attr = variable_list.get(class_name).get(i);	
				 if(attr.substring(0,1).equals("+")||attr.substring(0,1).equals("-")){
				 		//System.out.println(attr);
				 }
				 else{
					 variable_list.get(class_name).remove(i);
					 i--;
				 }
				} 
		}
	}
	 
	public void set_get(){
		for(String key:set_get_list.keySet()){
			for(String attr:variable_list.get(key)){
				if(set_get_list.get(key).toLowerCase().contains(attr.substring(attr.indexOf(":")+1, attr.indexOf(";")))){
					String new_attr = attr.replace("-", "+");
					variable_list.get(key).add(0,new_attr);
					variable_list.get(key).remove(attr);
					break;
				}
			}
		}
	}
	 
	 public void printList(){
		 System.out.println("=======Classes=======");
		 for(String class_name:class_list){
			 System.out.println("Class: " + class_name);
			 System.out.println("Variables: " + variable_list.get(class_name));
			 System.out.println("Methods: " + method_list.get(class_name));
		 }
		 System.out.println("=======Interfaces=======");
		 for(String interface_name:interface_list){
			 System.out.println("Interface: " + interface_name);
			 System.out.println("Methods: " + method_list.get(interface_name));
		 }
		 System.out.println("=======Extends/Implements=======");
		 for(String extend_impl:extends_implements_list){
			 System.out.println(extend_impl);
		 }
		 System.out.println("=======Use Cases=======");
		 for(String use_case:use_case_list){
			 System.out.println(use_case);
		 }
		 System.out.println("=======Mutliplicity=======");
		 for(String x:multi_map.keySet()){
			for(String y:multi_map.get(x).keySet()){
				System.out.println(x + "-" + multi_map.get(x).get(y) + y);
			}
		}
	 }
	 
	 public void construct(){
		 ArrayList<String> buffer_list = new ArrayList();
		 //***Construct the class with attributes and methods
		 for(String class_name:class_list){
			 StringBuilder buffer = new StringBuilder();
			 buffer.append("[" + class_name);
			 if(!variable_list.get(class_name).isEmpty()|| !method_list.get(class_name).isEmpty()){
				 buffer.append("|");
			 }
			 for(String attr:variable_list.get(class_name)){
				 //reformat string 
				 String reformat_str = attr.substring(0,1) + attr.substring(attr.indexOf(":")+1,attr.indexOf(";"))+ ":" + attr.substring(1,attr.indexOf(":")) + ";";
				 buffer.append(reformat_str);
				 //buffer.append(attr);
			 }
			 buffer.append(String.join(";", method_list.get(class_name)));
			 /*
			 for(String meth:method_list.get(class_name)){
				 buffer.append(meth);
			 }
			 */
			 buffer.append("]");
			 buffer_list.add(buffer.toString());
		 }
		 //***Construct the interface
		 for(String interface_name:interface_list){
			 StringBuilder buffer = new StringBuilder();
			 buffer.append("[<<interface>>;" + interface_name);
			 //interface cannot be displayed
			 /*
			 if(!method_list.get(interface_name).isEmpty()){
				 buffer.append("|");
				 buffer.append(String.join(";", method_list.get(interface_name)));
			 }*/
			 buffer.append("]");
			 buffer_list.add(buffer.toString());
		 }
		 //***Construct the relationships and multiplicity
		 
		 List<String> tmp_list = new ArrayList();
		 List<String> multi_list = new ArrayList();
		 List<Integer> class_len_list = new ArrayList();
		 Stack<String> stack = new Stack();
		 for(String x:multi_map.keySet()){
			for(String y:multi_map.get(x).keySet()){
				tmp_list.add(x + "-" + multi_map.get(x).get(y) + y);
				//System.out.println(x + "-" + multi_map.get(x).get(y) + y);
			}
		 }
		 //construct multiplicity list
		 
		 for(int i=0;i<tmp_list.size();i++){ //merge the list
			 String s1 = tmp_list.get(i);
			 for(int j=i+1;j<tmp_list.size();j++){
				 String s2 = tmp_list.get(j);
				 if((s1.substring(0, s1.indexOf("-")).equals(s2.substring(s2.indexOf("-")+2,s2.length()))) && (s2.substring(0, s2.indexOf("-")).equals(s1.substring(s1.indexOf("-")+2,s1.length())))){
					 if(s2.contains("1")){
						 multi_list.add(s1.substring(0, s1.indexOf("-"))+"1"+s1.substring(s1.indexOf("-"),s1.length()));
					 }
					 else if(tmp_list.get(j).contains("*")){
					 multi_list.add(s1.substring(0, s1.indexOf("-"))+"*"+s1.substring(s1.indexOf("-"),s1.length()));
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
		 /*
		 System.out.println("====Debug====");
		 for(String s:multi_list){
			 System.out.println(s);
		 }
		 for(String s:tmp_list){
			 System.out.println(s);
		 }
		 */
		 
		 for(String s:tmp_list){
			 //reverse the strings to suit the requirement
			 if(s.contains("1")){
				 s = s.substring(s.indexOf("1")+1, s.length())+"1"+"-"+s.substring(0,s.indexOf("-")); 
			 }
			 else if(s.contains("*")){
				 s = s.substring(s.indexOf("*")+1, s.length())+"*"+"-"+s.substring(0,s.indexOf("-"));
			 }
			 multi_list.add(s);
		 }
		 /*
		 for(String s:multi_list){
			 System.out.println(s);
		 }
		 */
		 
		 //construct multiplicity table
		 for(String s:multi_list){
			 StringBuilder buffer = new StringBuilder();
			 //System.out.println("[" + (s.substring(0,class_len) + "]" + (s.substring(class_len,s.length()-class_len) + "[" + (s.substring(s.length()-class_len, s.length()) + "]"))));
			 buffer.append("[");
			 int i=0;
			 for(;i<s.length();i++){
				 if(s.charAt(i)=='*' || s.charAt(i)=='1' || s.charAt(i)=='-'){
					 if(interface_list.contains(s.substring(0,i))){
						 buffer.append("<<interface>>;" + s.substring(0,i));
					 }
					 else{
						 buffer.append(s.substring(0,i));
					 }
					 break;
				 }
			 }
			 buffer.append("]");
			 int j = i;
			 while(s.charAt(i)=='*' || s.charAt(i)=='1' || s.charAt(i)=='-'){ //skip to the next class
				 i++;
			 }
			 buffer.append(s.substring(j,i));
			 if(interface_list.contains(s.substring(i,s.length()))){
				 buffer.append("[<<interface>>;" + s.substring(i,s.length())+"]");
			 }
			 else{
				 buffer.append("[" + s.substring(i,s.length())+"]"); 
			 }
			 //System.out.println(buffer.toString());
			 buffer_list.add(buffer.toString());
			 
		 }
		 //***construct the extends and implements
		 for(String s:extends_implements_list){
			 buffer_list.add(s);
		 }
		 /*
		  *  //construct multiplicity list
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
			 System.out.println("====Debug====");
			 for(String s:multi_list){
				 System.out.println(s);
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
			 
			 //construct multiplicity table
			 for(String s:multi_list){
				 StringBuilder buffer = new StringBuilder();
				 //System.out.println("[" + (s.substring(0,class_len) + "]" + (s.substring(class_len,s.length()-class_len) + "[" + (s.substring(s.length()-class_len, s.length()) + "]"))));
				 buffer_list.add("[" + (s.substring(0,class_len) + "]" + (s.substring(class_len,s.length()-class_len) + "[" + (s.substring(s.length()-class_len, s.length()) + "]"))));
				 
			 }
			 
			 //***construct the extends and implements
			 for(String s:extends_implements_list){
				 buffer_list.add(s);
			 }
			 
			 //***construct use case table
			 for(String s:use_case_list){
				 buffer_list.add(s);
			 }
			 yuml_string = new StringBuilder(String.join(",", buffer_list));
		 }
		 
		  */
		 
		 //***construct use case table
		 for(String s:use_case_list){
			 buffer_list.add(s);
		 }
		 yuml_string = new StringBuilder(String.join(",", buffer_list));
	 }
	 

	 
	 public String generateString(){
		 return yuml_string.toString();
	 }
	 
}