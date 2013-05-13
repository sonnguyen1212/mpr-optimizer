package mpr;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xml.MprFile;

public class NewmprParser {

	//maybe ? could be a problem in mprLine
	public static Pattern mprLine = Pattern.compile("(\\w+)=\"?([.\\*\\/\\s\\-\\+\\(\\)\\w]+)\"?");
	public static Pattern contourLine = Pattern.compile("(\\w)=([.\\*\\/\\s\\+\\-\\(\\)\\w]+)");

//	public static final String xContour = "X";
//	public static final String yContour = "Y";
//
//	public static final String[] xParameters = { "XA", "XE"};
//	public static final String[] yParameters = { "YA", "YE"};
//	
//	
//	public static void addOffsetToContour(ArrayList<String> lines,
//			String xOffset, String yOffset) {
//		int index=0;
//		for (String line : lines) {
//			Matcher contourMatcher = contourLine.matcher(line);
//			// if problem : try to replace with this:
//			//			if (contourMatcher.find()) {
//
//			if (contourMatcher.matches()) {
//				if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(xContour)) {
//					line = xContour + "="+ contourMatcher.group(NestingCreator.PARAMETER_VALUE) 
//							+ "+" + xOffset ;
//					lines.set(index, line);
//				}
//
//				if (contourMatcher.group(NestingCreator.PARAMETER_NAME).equals(yContour)) {
//					line = yContour	+ "=" + contourMatcher.group(NestingCreator.PARAMETER_VALUE)
//							+ "+" + yOffset ;
//					lines.set(index, line);
//				}
//			}
//			index++;
//		}
//		return;
//	}

//	public static void addOffsetToOperation(ArrayList<String> lines,
//			String xOffset, String yOffset) {
//		int index=0;
//		for (String line : lines) {
//			Matcher mprMatcher = mprLine.matcher(line);
//			if (mprMatcher.find()) {
//				for (int i = 0; i < xParameters.length; i++) {
//					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals(
//							xParameters[i])) {
//						line = xParameters[i]+ "=\""
//								+ mprMatcher.group(NestingCreator.PARAMETER_VALUE) + "+"
//								+ xOffset + "\"";
//						lines.set(index, line);
//						break;
//					}
//				}
//				for (int i = 0; i < yParameters.length; i++) {
//					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals(
//							yParameters[i])) {
//						line = yParameters[i] + "=\""
//								+ mprMatcher.group(NestingCreator.PARAMETER_VALUE)+ "+"
//								+ yOffset + "\"";
//						lines.set(index, line);
//						break;
//					}
//				}
//			}
//			index++;
//		}
//	}


	public static void changeOpCS(ArrayList<String> currentComp, int currentCSIndex, boolean isContour)
	{
		int index=0;
		boolean found = false;
		String newLine = isContour ? "KO=" + currentCSIndex : "KO=\"" + currentCSIndex + "\"";
		for(String line : currentComp) {
			Matcher mprMatcher = mprLine.matcher(line);
			if (mprMatcher.find()) {
				if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("KO")) 
				{
					currentComp.set(index, newLine);
					found = true;
					break;
				} 
			}
			index++;
		}
		
		if (!found)
		{
			currentComp.add(newLine);
		}
	}
	
	
	public static boolean shouldFlip (MprFile currentMpr, ArrayList<String> header)
	{
		for(String line: header) {
				Matcher mprMatcher = mprLine.matcher(line);
				if(mprMatcher.find() && (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("_BSX") || 
						mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("LA")))
				{
					String valueString = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
					double valueDouble = Double.parseDouble(valueString);
					if(Math.abs(valueDouble - currentMpr.getLength()) < 0.06) {
						return false;
					}
					else {
						return true;
					}
				}
			}
		return false;
	}

}
