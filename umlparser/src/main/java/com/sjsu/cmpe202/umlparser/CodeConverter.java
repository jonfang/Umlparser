package com.sjsu.cmpe202.umlparser;

public class CodeConverter {
	
	public String generateYUMLString(){
		//generate string usable by YUML for UML Class Diagram
		//generate string usable by YUML for UML Class Diagram
		return "[A|-x:int;-y:int(*)]1-0..*[B],[A]-1[C],[A]-*[D]";
	}
}
