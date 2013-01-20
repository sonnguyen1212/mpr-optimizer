package mpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;

import xml.Layout;
import xml.MprFile;

public class mprWriter {
	// empty constructor
	public static final String PLATE_DIRECTORY = "Nesting_Mprs";
	public static final String LEFT_OVER = "LeftOvers_Mprs";

	// search recursively the given file name in the directory, if found return
	// a File object of it,
	// if not - return NULL;
	
	public static File findFile(String fileName, String sourceDirectory) {
		File sourceDir = new File(sourceDirectory);
		if (!sourceDir.isDirectory()) {
			return null;
		}
		File[] fileList = sourceDir.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				File nestedFile = findFile(fileName, file.getAbsolutePath());
				if (nestedFile != null) {
					return nestedFile;
				}
			}
			if (file.getName().equals(fileName)) {
				return file;
			}
		}
		return null;
	}

	// this method will recieve the dimensios of the plate, and the lines to add
	// to it, it will generate
	// the header of the document and concat it to the lines given and will
	// write the file to the PLATE_DIRECFORY
	public static void createPlateMpr(Point3D dimensions,
			ArrayList<String> lines, String mprDir, String name) {
		ArrayList<String> headerLines = new ArrayList<>();
		headerLines.add("[H");
		headerLines.add("VERSION=\"4.0\"");
		headerLines.add("OP=\"2\"");
		headerLines.add("<100 \\WerkStck\\");
		headerLines.add("LA=\"" + dimensions.getX() + "\"");
		headerLines.add("BR=\"" + dimensions.getY() + "\"");
		headerLines.add("DI=\"" + dimensions.getZ() + "\"");
		headerLines.add("FNX=\"0\"");
		headerLines.add("FNY=\"0\"");
		headerLines.add("AX=\"0\"");
		headerLines.add("AY=\"0\"");
		for (String line : lines) {
			headerLines.add(line);
		}
		File plateDir = new File(mprDir+ File.separator + PLATE_DIRECTORY);
		if (!plateDir.exists()) {
			plateDir.mkdirs();
		}
		fileWriter(mprDir+ File.separator + PLATE_DIRECTORY + File.separator + name, headerLines);
	}

	// this method will recieve all the neccaserry lines for the this mpr, and
	// will write it to the
	// LEFT_OVER directory.
	public static void createLeftOverMpr(ArrayList<String> lines,String mprDir, String name) {
		File leftOverDir = new File(mprDir+ File.separator + LEFT_OVER);
		if (!leftOverDir.exists()) {
			leftOverDir.mkdirs();
		}
		fileWriter(mprDir+ File.separator + LEFT_OVER + File.separator + name, lines);
	}

	// this method will be used by the two methods above, it will open the
	// stream and write the files.
	private static void fileWriter(String destination, ArrayList<String> lines) {
		File destinationFile = new File(destination);
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(destinationFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (printWriter != null) {
			for (String line : lines) {
				printWriter.println(line);
			}
		}
		if (printWriter != null) {
			printWriter.close();
		}
	}
	
	
	public static ArrayList<String> readFileCompletly(MprFile fileToRead, String mprDirectory) throws IOException
	{
		File fileLocation = mprWriter.findFile(fileToRead.getPartCode(), mprDirectory);
		if (fileLocation!= null){
			BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
			ArrayList<String> lines = new ArrayList<>();
			while (reader.ready()){
				lines.add(reader.readLine());
			}
			reader.close();
			return lines;
		}
		return null;
	}
	

	
}
