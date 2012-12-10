package mpr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xml.MprFile;

public class mprWriter {
	// empty constructor
	public static final String PLATE_DIRECTORY = "Nesting_Mprs";
	public static final String LEFT_OVER = "LeftOvers_Mprs";
	public static Pattern mprLine = Pattern.compile("(\\w)=(\"\\w\")");
	public static final String[] xParameters = { "XA", "XE" };
	public static final String[] yParameters = { "YA", "YE" };

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
			ArrayList<String> lines, String name) {
		ArrayList<String> headerLines = new ArrayList<>();
		headerLines.add("[H\n\r");
		headerLines.add("VERSION=\"4.0\"\n\r");
		headerLines.add("OP=\"2\"\n\r");
		headerLines.add("<100 \\WerkStck\\\n\r");
		headerLines.add("LA=\"" + dimensions.getX() + "\"\n\r");
		headerLines.add("BR=\"" + dimensions.getY() + "\"\n\r");
		headerLines.add("DI=\"" + dimensions.getZ() + "\"\n\r");
		headerLines.add("FNX=\"0\"\n\r");
		headerLines.add("FNY=\"0\"\n\r");
		headerLines.add("AX=\"0\"n\r");
		headerLines.add("AY=\"0\"\n\r");
		for (String line : lines) {
			headerLines.add(line);
		}
		File plateDir = new File(PLATE_DIRECTORY);
		if (!plateDir.exists()) {
			plateDir.mkdirs();
		}
		fileWriter(PLATE_DIRECTORY + File.separator + name, headerLines);
	}

	// this method will recieve all the neccaserry lines for the this mpr, and
	// will write it to the
	// LEFT_OVER directory.
	public static void createLeftOverMpr(ArrayList<String> lines, String name) {
		File leftOverDir = new File(LEFT_OVER);
		if (!leftOverDir.exists()) {
			leftOverDir.mkdirs();
		}
		fileWriter(LEFT_OVER + File.separator + name, lines);
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

	public static void addOffsetToOperation(ArrayList<String> lines,
			String xOffset, String yOffset) {
		for (String line : lines) {
			Matcher mprMatcher = mprLine.matcher(line);
			if (mprMatcher.find()) {
				for (int i = 0; i < xParameters.length; i++) {
					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals(
							xParameters[i])) {
						line = xParameters[i]
								+ "\""
								+ mprMatcher
										.group(NestingCreator.PARAMETER_VALUE)
								+ xOffset + "\"\n\r";
						break;
					}
				}
				for (int i = 0; i < yParameters.length; i++) {
					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals(
							yParameters[i])) {
						line = yParameters[i]
								+ "\""
								+ mprMatcher
										.group(NestingCreator.PARAMETER_VALUE)
								+ yOffset + "\"\n\r";
						break;
					}
				}
			}
		}
	}

	public static void flipXY(ArrayList<String> lines, String yLen) {
		String oldXA = "", oldYA = "", oldXE = "", oldYE = "";
		for (String line : lines) {
			Matcher mprMatcher = mprLine.matcher(line);
			if (mprMatcher.find()) {
				if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("XA")) {
					oldXA = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
				} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("XE")) {
					oldXE = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
				} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("YA")) {
					oldYA = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
				} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("YE")) {
					oldYE = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
				}
			}
		}
		for(String line : lines) {
			Matcher mprMatcher = mprLine.matcher(line);
			if (mprMatcher.find()) {
				if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("XA")) {
					line = "XA=\"" + yLen + "-(" + oldYA + ")\"";
				} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("XE")) {
					line = "XE=\"" + yLen + "-(" + oldYE + ")\"";
				} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("YA")) {
					line = "YA=\"" + oldXA + "\"";
				} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME)
						.equals("YE")) {
					line = "YE=\"" + oldXE + "\"";
				}
			}
		}
	}
	
	public static boolean shouldFlip (MprFile currentMpr, ArrayList<String> header)
	{
		//search and determine
		return false;
	}
}
