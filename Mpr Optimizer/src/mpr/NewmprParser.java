package mpr;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xml.MprFile;

public class NewmprParser {

	//maybe ? could be a problem in mprLine
	public static Pattern mprLine = Pattern.compile("(\\w+)=\"?([.\\*\\/\\s\\-\\+\\(\\)\\w]+)\"?");
	public static Pattern contourLine = Pattern.compile("(\\w)=([.\\*\\/\\s\\+\\-\\(\\)\\w]+)");

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
	public static void reverseOpLineSide(ArrayList<String> currentComp)
	{
		int index=0;
		String leftGoing = "RK=\"WRKL\"";
		String rightGoing = "RK=\"WRKR\"";

		for(String line : currentComp) {
			Matcher mprMatcher = mprLine.matcher(line);
			if (mprMatcher.find()) {
				//if found relevant section for left/right going
				if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("RK")) 
				{
					//if was left - change to right
					if (mprMatcher.group(NestingCreator.PARAMETER_VALUE).trim().equals("WRKL"))
						currentComp.set(index, rightGoing);
				
					//if was right - change to left
					if (mprMatcher.group(NestingCreator.PARAMETER_VALUE).trim().equals("WRKR"))
						currentComp.set(index, leftGoing);
					
					//if was middle - don't touch.
					break;
				} 
			}
			index++;
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
