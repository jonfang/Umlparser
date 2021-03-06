package com.sjsu.cmpe202.umlparser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class UMLGenerator {
	
	private static final String yuml_URL = "https://yuml.me/diagram/scruffy/class/";  //URL of yuml 
	private static String png_location;
	private static int byte_size = 2048;
	
	public UMLGenerator(String output_file){
		System.out.println("Draw UML");
		png_location = output_file;
	}
	
	public void generatePNG(String yuml_string){
		try{
			URL url = new URL(yuml_URL + yuml_string + ".png");
			InputStream istream = url.openStream();
			OutputStream ostream = new FileOutputStream(png_location);
			byte[] buffer = new byte[byte_size];
			int length;

			while ((length = istream.read(buffer)) != -1) {
				ostream.write(buffer, 0, length);
			}
			istream.close();
			ostream.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
