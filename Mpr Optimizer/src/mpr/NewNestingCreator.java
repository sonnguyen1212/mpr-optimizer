package mpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;
import xml.Layout;
import xml.MprFile;
import xml.XMLParser;

public class NewNestingCreator {

	//maybe ? could be a problem in mprLine
	public static Pattern mprLine = Pattern.compile("(\\w+)=\"?([.\\*\\/\\s\\-\\+\\(\\)\\w]+)\"?");
	public static Pattern contourLine = Pattern.compile("(\\w)=([.\\*\\/\\s\\+\\-\\(\\)\\w]+)");

	public static Pattern lineStructForParam = Pattern.compile("(\\w+=\"?)([^\"]+)([\"\\s]?)");

	public static final int PARAMETER_NAME = 1;
	public static final int PARAMETER_VALUE = 2;
	public static final String partDimens = "<100 \\\\WerkStck\\\\";
	public static final String[] supportedOps = {"<102 \\\\BohrVert\\\\","<109 \\\\Nuten\\\\","<112 \\\\Tasche\\\\"};
	public static final String vertTrimmingHeader ="<105 \\\\Konturfraesen\\\\";
	public static final String componentHeader ="<139 \\\\Komponente\\\\";
	public static final String horizBoring = "<103 \\\\BohrHoriz\\\\";
	public static final String contourRegex = "](\\d+)";
	public static final String contourElementRegex = "\\$E\\d+";
	public static final String millingOperationRegex = "(\\w+=\")(\\d+)(:\\d+\")";
	public static final String parameterHeader = "\\[001";


	public static final String fileEnd = "!";
	public static String PART_THICK = "DI";

	//private Layout currentLayout;`
	//MprFile firstFile;
	private ArrayList<Layout> layouts;
	private XMLParser parser;
	private String mprDirectory;
	private ArrayList<String> errorMsg;
	private JProgressBar progressBar;
	private int mprCount;
	private boolean checkParameters, sawingSeperate,checkComponents;
	//	private OptionsManager options;
	//	private String mprMergePath="", mprMergeArgs="", componentsDir = "",a1path = ""; 

	public NewNestingCreator(String xmlFile, String mprDirectory, ArrayList<String> errorMsg, JProgressBar bar, boolean param, boolean sawing, OptionsManager options){
		this.parser = new XMLParser(xmlFile);
		this.mprCount = this.parser.parse();
		this.layouts = parser.getLayouts();
		this.errorMsg = errorMsg;
		this.progressBar = bar;
		this.mprDirectory = mprDirectory;
		this.checkParameters = param;
		this.sawingSeperate = sawing;
		//		this.options = options;

		this.checkComponents= param; 
		//		if (checkComponents)
		//		{
		//			mprMergePath = this.options.getProperty("mprMergePath");
		//			mprMergeArgs = this.options.getProperty("mprMergeArgs");
		//			componentsDir = this.options.getProperty("componentsDir");
		//			a1path = this.options.getProperty("a1path");
		//		}
	}

	public void createLayoutMprs () throws IOException{
		int currentMprCount = 0;
		for (Layout currentLayout : layouts){
			if (currentLayout==null)
				continue;

			int currentContourIndex = 0;
			int currentCSIndex = 4;

			ArrayList<MprFile> mprs = currentLayout.getMprFiles();
			ArrayList<String> currentPlateContours = new ArrayList<String>();
			ArrayList<String> currentPlateOperations = new ArrayList<String>();
			ArrayList<String> currentPlateMillings = new ArrayList<String>();

			ArrayList<String> currentPlateExtraCS = new ArrayList<String>();
			currentPlateExtraCS.add("[K");

			Point3D plateMeasurements = null;
			plateMeasurements = determinePlateMeasurements(currentLayout, mprs.get(0));
			if (plateMeasurements==null)
			{
				errorMsg.add("File " + currentLayout.getMprFiles().get(0).getPartCode() + " was not found in the current directory");
				errorMsg.add("Plate " + currentLayout.getNumber() + " Couldn't be created");
				//look for other mpr to take measurements!!!!!!
				continue;

			}

			for (int mprIndex=0; mprIndex<mprs.size() ; mprIndex++){
				MprFile currentMpr = mprs.get(mprIndex);
				//for (MprFile currentMpr:mprs)
				//{
				File mprFile = mprWriter.findFile(currentMpr.getPartCode(), mprDirectory);
				if (mprFile==null){
					errorMsg.add("File " + currentMpr.getPartCode() + " from plate #"
							+currentLayout.getNumber()+" was not found in the current directory");
					continue;
				}
				ArrayList<Parameter> currentMprParameters = null;


				double xOffset = currentMpr.getXOffset();
				double yOffset = currentMpr.getYOffset();
				BufferedReader reader = null;;

				reader = new BufferedReader(new FileReader(mprFile));

				ArrayList<String> header = new ArrayList<String>();
				ArrayList<String> contours = new ArrayList<>();
				ArrayList<String> operations = new ArrayList<>();

				String currentLine = null;
				
				//*******************Read Part And Break To Parts*********************************
				//seperate file header - run until contourRegex or isOperation
				while (reader.ready())
				{
					currentLine = reader.readLine();
					if (!currentLine.matches(contourRegex) && !isOperation(currentLine)){
						header.add(currentLine);
					}
					else
						break;
				}

				//read parameter table and analyze:
				if (checkParameters)
				{
					currentMprParameters = new ArrayList<Parameter>();
					currentMprParameters = createParameterList(header);
				}


				//If stopped at contour regex - copy contour lines 
				if (currentLine.matches(contourRegex)){
					while (reader.ready() && !isOperation(currentLine) && !currentLine.matches(partDimens))
					{
						contours.add(currentLine);
						currentLine = reader.readLine();
					}
				}

				//check if should add the WerkSteck part to the header
				if (currentLine.matches(partDimens))
				{
					while (reader.ready() && !isOperation(currentLine))
					{
						header.add(currentLine);
						currentLine = reader.readLine();
					}
				}

				//check if should flip xy coordinates
				boolean shouldFlip = mprParser.shouldFlip(currentMpr, header);

				//Add operations lines
				while (reader.ready() && !currentLine.matches(fileEnd))
				{
					operations.add(currentLine);
					currentLine = reader.readLine();
				}
				reader.close();


				//break operations to arraylist for each operation
				ArrayList<ArrayList<String>> operationsBreaked = breakToOperations(operations); 

				//*******************create new CS to move everything to*********************************
				boolean shouldMirror = currentMpr.getPartCode().startsWith("rp_");
				if (!shouldFlip)
				{
					if (!shouldMirror)
					{
						currentPlateExtraCS.addAll(createCS(currentCSIndex, xOffset, yOffset, 0.0, false));
					}
					else
					{
						double xOff = currentMpr.getLength() + xOffset;
						currentPlateExtraCS.addAll(createCS(currentCSIndex, xOff, yOffset, 0.0, true));
					}
				}
				if (shouldFlip)
				{
					if (!shouldMirror)
					{
						double xOff = currentMpr.getLength() + xOffset;
						currentPlateExtraCS.addAll(createCS(currentCSIndex, xOff, yOffset, 90.0, false));
					}
					else
					{//???????????
						double xOff = currentMpr.getLength() + xOffset;
						double yOff = currentMpr.getWidth() + yOffset;
						currentPlateExtraCS.addAll(createCS(currentCSIndex, xOff, yOff, 90.0, true));
					}
				}


				//*******************Replace Parameters For Components if any********************************
				if (checkComponents)
				{
					ArrayList<ArrayList<String>> components = extractSpecificOpType(operationsBreaked, componentHeader);
					if (components!=null)
					{
						for (ArrayList<String> currentComp:components)
						{
							mprParser.changeOpCS(currentComp, currentCSIndex);
							//replace PARAM 
							if (currentMprParameters!= null && currentMprParameters.size()>0)
								replaceParamForComponents(components, currentMprParameters);
							currentPlateOperations.addAll(currentComp);
						}
					}
				}

				//*******************If any Contours - traet them*********************************
				if (!contours.isEmpty())
				{
					ArrayList<ArrayList<String>> contoursBreaked = breakContours(contours, contourRegex);
					ArrayList<ArrayList<String>> vertMilling = extractSpecificOpType(operationsBreaked, vertTrimmingHeader);
					if (vertMilling == null)
						vertMilling = new ArrayList<ArrayList<String>>();
					if (contoursBreaked.size() != vertMilling.size())
						errorMsg.add("ERROR!!! Contouring and milling not the same size at file :" + currentMpr.getPartCode());

					updateIndexesContours(contoursBreaked, currentContourIndex);
					updateIndexesMilling(vertMilling, currentContourIndex, shouldMirror);
					currentContourIndex += contoursBreaked.size();

					int index =0;
					for (ArrayList<String> contour : contoursBreaked){
						ArrayList<ArrayList<String>> currentContourBreaked = breakContours(contour, contourElementRegex);

						//make change only for start point
						NewmprParser.changeOpCS(currentContourBreaked.get(1), currentCSIndex, true);
						contour = new ArrayList<String>();
						uniteArrayLists(currentContourBreaked, contour);
						contoursBreaked.set(index, contour);
						index++;
					}

					if (currentMprParameters!= null && currentMprParameters.size()>0)
					{
						replaceParamForArrayListOfArrayList(vertMilling, currentMprParameters);
						replaceParamForArrayListOfArrayList(contoursBreaked, currentMprParameters);
					}

					uniteArrayLists(vertMilling, currentPlateMillings);
					uniteArrayLists(contoursBreaked, currentPlateContours);

				}

				//createLeftOverMPR
				ArrayList<ArrayList<String>> unSupportedOps = extractSpecificOpType(operationsBreaked, horizBoring);
				if (sawingSeperate)
				{
					ArrayList<ArrayList<String>> sawingOps = extractSpecificOpType(operationsBreaked, supportedOps[1]);
					//if unSupportedOps is null - replace it with sawingUp
					if (unSupportedOps==null)
						unSupportedOps = sawingOps;
					//if it's not - and sawing is not null - add sawing to unSupportedOps
					else if (sawingOps!=null)
						unSupportedOps.addAll(sawingOps);
				}

				//in anycase if there are unsupported ops - create a leftOver file
				if (unSupportedOps!= null){
					uniteArrayLists(unSupportedOps, header);
					header.add(fileEnd);
					String fileName = currentMpr.getPartCode();
					mprWriter.createLeftOverMpr(header, mprDirectory, "LO_"+fileName); 
				}

				//create second side if needed
				if (currentMpr.getSecondPartCode()!=null)
				{
					ArrayList<String> lines = mprWriter.readFileCompletly(currentMpr, mprDirectory);
					mprWriter.createLeftOverMpr(lines, mprDirectory, "Back_Ops_"+currentMpr.getPartCode());
				}


				//add relevant plate lines
				for (ArrayList<String> operation : operationsBreaked){
					NewmprParser.changeOpCS(operation, currentCSIndex, false);
					if (shouldMirror && !sawingSeperate)
						NewmprParser.reverseOpLineSide(operation);
				}

				//check if should replace parameters
				if (currentMprParameters!= null && currentMprParameters.size()>0)
				{
					replaceParamForArrayListOfArrayList(operationsBreaked, currentMprParameters);
				}

				uniteArrayLists(operationsBreaked, currentPlateOperations);

				currentMprCount++;
				currentCSIndex++;
				progressBar.setValue(currentMprCount/mprCount*100);
				progressBar.repaint();


			}

			//createPlateFile:
			ArrayList<String> allPlateLines = new ArrayList<>();
			allPlateLines.addAll(currentPlateExtraCS);
			allPlateLines.addAll(currentPlateContours);
			allPlateLines.addAll(currentPlateMillings);
			allPlateLines.addAll(currentPlateOperations);


			mprWriter.createPlateMpr(plateMeasurements, allPlateLines, mprDirectory, "Plate"+ currentLayout.getNumber()+".mpr");

		}
	}

	private void updateIndexesContours(ArrayList<ArrayList<String>> contours, int currentPlateIndex){
		String currentLine = null;
		Pattern patt = Pattern.compile(contourRegex);
		Matcher matcher;
		for (int i=0 ; i<contours.size() ; i++)
		{
			currentLine = contours.get(i).get(0);
			matcher = patt.matcher(currentLine);
			matcher.find();
			int index = Integer.parseInt(matcher.group(1));
			index+= currentPlateIndex;
			currentLine = "]" + index; //\n\r?????
			contours.get(i).set(0, currentLine);// = currentLine;
		}

	}

	private void updateIndexesMilling(ArrayList<ArrayList<String>> millings, int currentPlateIndex, boolean shouldReversSide){
		Pattern patt = Pattern.compile(millingOperationRegex);
		Matcher matcher;
		String currentLine;
		for (ArrayList<String> currentMill : millings)
		{
			if (shouldReversSide)
				NewmprParser.reverseOpLineSide(currentMill);

			for (int line=0 ; line<currentMill.size() ; line++)
			{
				currentLine = currentMill.get(line);
				matcher = patt.matcher(currentLine);
				if (matcher.find())
				{
					int index = Integer.parseInt(matcher.group(2));
					index+= currentPlateIndex;
					currentLine =  matcher.group(1) + index + matcher.group(3); //\n\r?????
					//currentMill.set(lineCounter, currentLine); //??????????
					currentMill.set(line, currentLine);
				}
				//lineCounter++;
			}
		}

	}


	private ArrayList<ArrayList<String>> breakToOperations (ArrayList<String> allOperations){
		ArrayList<ArrayList<String>> operationsBreaked = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentOp =  new ArrayList<String>();
		for (String line: allOperations)
		{
			if (!isOperation(line))
			{
				currentOp.add(line);
			}
			else
			{
				if (!currentOp.isEmpty())
					operationsBreaked.add(currentOp);
				currentOp = new ArrayList<String>();
				currentOp.add(line);
			}

		}
		if (!currentOp.isEmpty())
			operationsBreaked.add(currentOp);
		return operationsBreaked;
	}

	private ArrayList<ArrayList<String>> breakContours (ArrayList<String> allContours, String regex){
		ArrayList<ArrayList<String>> contoursBreaked = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentContour = new ArrayList<String>();
		for (String line: allContours)
		{
			if (!line.matches(regex))
			{
				currentContour.add(line);
			}
			else
			{
				if (!currentContour.isEmpty())
					contoursBreaked.add(currentContour);
				currentContour = new ArrayList<String>();
				currentContour.add(line);
			}
		}
		if (!currentContour.isEmpty())
			contoursBreaked.add(currentContour);
		return contoursBreaked;
	}

	private void uniteArrayLists(ArrayList<ArrayList<String>> source, ArrayList<String> dest)
	{
		for (ArrayList<String> sourceElement : source)
			dest.addAll(sourceElement);
	}

	private boolean isOperation(String currentLine)
	{
		if (!currentLine.matches(horizBoring) && !currentLine.matches(vertTrimmingHeader) && !currentLine.matches(supportedOps[0])
				&& !currentLine.matches(supportedOps[1])&& !currentLine.matches(supportedOps[2]) && !currentLine.matches(componentHeader))
			return false;
		else
			return true;
	}

	private ArrayList<ArrayList<String>> extractSpecificOpType (ArrayList<ArrayList<String>> operationsBreaked, String regex)
	{
		ArrayList<ArrayList<String>> operationType = new ArrayList<>();
		for (int i=0 ; i<operationsBreaked.size() ; i++)
		{
			if (operationsBreaked.get(i).get(0).matches(regex))
			{
				operationType.add(operationsBreaked.get(i)); 
				operationsBreaked.remove(i);
				i--;
			}
		}
		if (operationType.isEmpty())
			return null;
		else
			return operationType;
	}


	private Point3D determinePlateMeasurements(Layout currentLayout, MprFile firstFile) throws IOException
	{
		double length = currentLayout.getLength();
		double width = currentLayout.getWidth();
		File fileLocation = mprWriter.findFile(firstFile.getPartCode(), mprDirectory);
		Point3D point = null;
		if (fileLocation!= null){
			BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
			point = new Point3D(Double.toString(length),Double.toString( width), "0.0");
			String currentLine;
			Matcher match;
			String thickness = "";
			while (reader.ready()){
				currentLine = reader.readLine();
				match = mprLine.matcher(currentLine);
				if (match.find())
				{
					if (match.group(PARAMETER_NAME).matches("_BSZ") || match.group(PARAMETER_NAME).matches("DI"))
					{
						thickness = match.group(PARAMETER_VALUE);
						break;
					}
				}
			}
			reader.close();
			point.setZ(thickness);
		}
		return point;
	}

	public static Point3D readMprAndFileDims(File mprFile) throws IOException
	{
		String length="", width="", thickness="";
		Point3D point = null;
		if (mprFile.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(mprFile));
			String currentLine;
			Matcher match;
			while (reader.ready()){
				currentLine = reader.readLine();
				match = mprLine.matcher(currentLine);
				if (match.find())
				{
					if (match.group(PARAMETER_NAME).matches("_BSX") || match.group(PARAMETER_NAME).matches("LA"))
						length = match.group(PARAMETER_VALUE);

					else if (match.group(PARAMETER_NAME).matches("_BSY") || match.group(PARAMETER_NAME).matches("BR"))
						width = match.group(PARAMETER_VALUE);

					else if (match.group(PARAMETER_NAME).matches("_BSZ") || match.group(PARAMETER_NAME).matches("DI"))
					{
						thickness = match.group(PARAMETER_VALUE);
						break;
					}
				}
			}
			reader.close();
			point = new Point3D(length, width, thickness);


		}
		return point;
	}

	private ArrayList<Parameter> createParameterList(ArrayList<String> lines)
	{
		ArrayList<Parameter> paramList = new ArrayList<Parameter>();
		Parameter tempParam;

		int index = 0;
		//go over the lines until reached the parameter header
		while (index<lines.size())
		{
			if (lines.get(index).matches(parameterHeader))
				break;
			index++;
		}

		index++; //set the current line to the first parameter header
		Matcher match;
		while (index<lines.size() && !lines.get(index).matches("\\s*") )
		{
			match = mprLine.matcher(lines.get(index));
			if (match.find())
			{
				if (!match.group(PARAMETER_NAME).matches("KM"))
				{
					tempParam = new Parameter(match.group(PARAMETER_NAME), replaceParamByValue(match.group(PARAMETER_VALUE), paramList));
					paramList.add(tempParam);
				}
			}
			index++;

		}

		return paramList;
	}

	private String replaceParamByValue(String line, ArrayList<Parameter> paramList)
	{
		if (paramList == null || paramList.size()==0)
			return line;

		String temp = line;
		for (int i=0 ; i<paramList.size() ; i++)
		{
			temp = paramList.get(i).replaceParameterByVal(temp);
		}
		return temp;
	}

	private void replaceParamForArrayList (ArrayList<String> lines,  ArrayList<Parameter> paramList)
	{
		if (lines==null)
			return;

		String temp;
		Matcher matcher;
		for (int i=0 ; i<lines.size(); i++)
		{
			matcher = lineStructForParam.matcher(lines.get(i));
			if (matcher.matches())
			{
				temp = matcher.group(1) + replaceParamByValue(matcher.group(2), paramList)+matcher.group(3);
				System.out.println(matcher.group(1));
				System.out.println(matcher.group(2));
				System.out.println(matcher.group(3));

				lines.set(i, temp);

			}
		}
	}


	private void replaceParamForArrayListOfArrayList (ArrayList<ArrayList<String>> lines,  ArrayList<Parameter> paramList)
	{
		if (lines==null)
			return;

		for (int i=0 ; i<lines.size(); i++)
		{
			replaceParamForArrayList(lines.get(i), paramList);
		}
	}

	private void replaceParamForComponents (ArrayList<ArrayList<String>> components,  ArrayList<Parameter> paramList)
	{
		if (components==null)
			return;
		int componentsIndexes=0;
		for (ArrayList<String> currentComp:components)
		{
			int currIndex=0;
			for(String line : currentComp) {

				//matcher = lineStructForParam.matcher(lines.get(i));
				//				if (matcher.matches())
				//				{
				//					temp = matcher.group(1) + replaceParamByValue(matcher.group(2), paramList)+matcher.group(3);

				Matcher mprMatcher = lineStructForParam.matcher(line);
				if (mprMatcher.matches()) {
					if (!mprMatcher.group(1).matches("VA=\"")) 
					{
						//						line = mprMatcher.group(NestingCreator.PARAMETER_NAME) + "=\"" + replaceParamByValue(mprMatcher.group(NestingCreator.PARAMETER_VALUE), paramList) + "\"";
						line = mprMatcher.group(1) + replaceParamByValue(mprMatcher.group(2), paramList)+mprMatcher.group(3);
						components.get(componentsIndexes).set(currIndex, line);
					} else {
						String value = mprMatcher.group(2);
						int firstSpace = value.indexOf(" ");
						String paramName = value.substring(0,firstSpace);
						String paramValue = replaceParamByValue(value.substring(firstSpace), paramList);
						line = mprMatcher.group(1) + paramName + paramValue+ mprMatcher.group(3);
						components.get(componentsIndexes).set(currIndex, line);
					}
				}
				currIndex++;
			}
			componentsIndexes++;
		}
	}
	//	private String analyzeComponents(ArrayList<ArrayList<String>> componenets)
	//	{
	//		String x,y,z,name;
	//		StringBuilder args = new StringBuilder();
	//		for (ArrayList<String> component: componenets)
	//		{
	//			x=""; y=""; z=""; name="";
	//			args.append("[ ");
	//			for (String line: component)
	//			{
	//				Matcher mprMatcher = mprLine.matcher(line);
	//				if (mprMatcher.find()) {
	//					if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("XA")) {
	//						x = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
	//
	//					} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("YA")) {
	//						y = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
	//
	//					} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("ZA")) {
	//						z = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
	//
	//					} else if (mprMatcher.group(NestingCreator.PARAMETER_NAME).equals("IN")) {
	//						name = mprMatcher.group(NestingCreator.PARAMETER_VALUE);
	//					}
	//				}
	//			}
	//			args.append(componentsDir);
	//			args.append(name + " ");
	//			args.append(x + "," + y + "," +z);
	//			args.append(" 1,1,1 0,0,0 ");
	//			args.append("] ");
	//		}
	//
	//		return args.toString();
	//	}
	private ArrayList<String> createCS(int index, double xOff, double yOff, double angle, boolean mirror)
	{
		ArrayList<String> csData = new ArrayList<>();
		csData.add("<00 \\\\Koordinatensystem\\\\");
		csData.add("NR=\""+index+"\"");
		csData.add("XP=\""+xOff+"\"");
		csData.add("XF=\"1.0\"");
		csData.add("YP=\""+yOff+"\"");
		csData.add("YF=\"1.0\"");
		csData.add("ZP=\"0\"");
		csData.add("ZF=\"1.0\"");
		csData.add("D1=\""+angle+"\"");
		csData.add("KI=\"0\"");
		csData.add("D2=\"0\"");
		csData.add("MI=\""+ (mirror?"1":"0") + "\"");

		return csData;
	}
}
