package mpr;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xml.MprFile;

public class mprParser {

	public static Pattern mprLine = Pattern.compile("(\\w+)=\"([.\\-\\+\\(\\)\\w]+)\"");
	public static Pattern contourLine = Pattern.compile("(\\w)=([.\\+\\-\\(\\)\\w]+)");

	public static final String xContour = "X";
	public static final String yContour = "Y";

	public static final String[] xParameters = { "XA", "XE"};
	public static final String[] yParameters = { "YA", "YE"};
	
	
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