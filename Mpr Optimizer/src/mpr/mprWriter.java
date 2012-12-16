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
	public static final String PLATE_DIRECTORY = "c:\\Nesting_Mprs";
	public static final String LEFT_OVER = "c:\\LeftOvers_Mprs";
	public static Pattern mprLine = Pattern.compile("(\\w+)=\"([.\\-\\+\\(\\)\\w]+)\"");
	public static Pattern contourLine = Pattern.compile("(\\w)=([.\\+\\-\\(\\)\\w]+)");

	public static final String xContour = "X";
	public static final String yContour = "Y";

	public static final String[] xParameters = { "XA", "XE"};
	public static final String[] yParameters = { "YA", "YE"};

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

	public static void addOffsetToContour(ArrayList<String> lines,
			String xOffset, String yOffset) {
		int index=0;
		for (String line : lines) {
			Matcher contourMatcher = contourLine.matcher(line);
			if (contourMatcher.find()) {
				if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(xContour)) {
					line = xContour + "="+ contourMatcher.group(NestingCreator.PARAMETER_VALUE) 
							+ "+" + xOffset ;
					lines.set(index, line);
				}

				if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(yContour)) {
					line = yContour	+ "=" + contourMatcher.group(NestingCreator.PARAMETER_VALUE)
							+ "+" + yOffset ;
					lines.set(index, line);
				}
			}
			index++;
		}
		return;
	}

	public static void addOffsetToOperation(ArrayList<String> lines,
			String xOffset, String yOffset) {
		int index=0;
		for (String line : lines) {
			Matcher mprMatcher = mprLine.matcher(line);
			if (mprMatcher.find()) {
				for (int i = 0; i < xParameters.length; i++) {
					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals(
							xParameters[i])) {
						line = xParameters[i]+ "=\""
								+ mprMatcher.group(NestingCreator.PARAMETER_VALUE) + "+"
								+ xOffset + "\"";
						lines.set(index, line);
						break;
					}
				}
				for (int i = 0; i < yParameters.length; i++) {
					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals(
							yParameters[i])) {
						line = yParameters[i] + "=\""
								+ mprMatcher.group(NestingCreator.PARAMETER_VALUE)+ "+"
								+ yOffset + "\"";
						lines.set(index, line);
						break;
					}
				}
			}
			index++;
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
		int index=0;
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
				lines.set(index, line);
			}
			index++;
		}
	}
	
	public static void flipXYContour(ArrayList<String> lines, String yLen) {
		String oldX="", oldY="";
		for (String line : lines) {
			Matcher contourMatcher = contourLine.matcher(line);
			if (contourMatcher.find()) {
				if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(xContour)) {
					oldX = contourMatcher.group(NestingCreator.PARAMETER_VALUE);
				} else if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(yContour)) {
					oldY = contourMatcher.group(NestingCreator.PARAMETER_VALUE);
				}
			}
		}
		int index=0;
		for(String line : lines) {
			Matcher contourMatcher = contourLine.matcher(line);
			if (contourMatcher.find()) {
				 if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(xContour)) {
					line = "X=" + yLen + "-(" + oldY + ")";
				} else if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(yContour)) {
					line = "Y=" + "("+oldX+ ")";
				}
				lines.set(index, line);
			}
			index++;
		}
		return;
	}
	
	public static boolean shouldFlip (MprFile currentMpr, ArrayList<String> header)
	{
		for(String line: header) {
				Matcher mprMatcher = mprLine.matcher(line);
				if(mprMatcher.find() && mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("LA")){
					String valueString = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
					double valueDouble = Double.parseDouble(valueString);
					if(Math.abs(valueDouble - currentMpr.getLength()) < 0.01) {
						return false;
					}
					else {
						return true;
					}
				}
			}
		
		//search and determine
		return false;
	}
}
