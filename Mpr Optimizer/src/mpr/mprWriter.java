package mpr;

import java.io.File;
import java.util.ArrayList;

public class mprWriter {
//empty constructor
	public static final String PLATE_DIRECTORY = "Nesting_Mprs";
	public static final String LEFT_OVER = "LeftOvers_Mprs";
	
	//search recursively the given file name in the directory, if found return a File object of it,
	//if not - return NULL;
	public static File findFile(String fileName, String sourceDirectory)
	{
		
	}
	
	//this method will recieve the dimensios of the plate, and the lines to add to it, it will generate
	//the header of the document and concat it to the lines given and will write the file to the PLATE_DIRECFORY
	public static void createPlateMpr(Point3D dimensions, ArrayList<String> lines, String name){
		[H
		 VERSION="4.0"
		 OP="2"

		 <100 \WerkStck\
		 LA="716" - length
		 BR="600" - width
		 DI="17" - thickness
		 FNX="0"
		 FNY="0"
		 AX="0"
		 AY="0"	
	}
	
	//this method will recieve all the neccaserry lines for the this mpr, and will write it to the
	//LEFT_OVER directory.
	public static void createLeftOverMpr(ArrayList<String> lines, String name){
		
	}
	
	//this method will be used by the two methods above, it will open the stream and write the files.
	private static void mprWriter (String destination , ArrayList<String> lines)
	{
		
	}
}
