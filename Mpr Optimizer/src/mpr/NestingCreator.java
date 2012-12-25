package mpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JProgressBar;
import xml.Layout;
import xml.MprFile;
import xml.XMLParser;

public class NestingCreator {
	public Pattern mprLine = Pattern.compile("(\\w+)=\"(\\w+)\"");
	public static final int PARAMETER_NAME = 1;
	public static final int PARAMETER_VALUE = 2;
	public static final String partDimens = "<100 \\\\WerkStck\\\\";
	public static final String[] supportedOps = {"<102 \\\\BohrVert\\\\", "<109 \\\\Nuten\\\\"};
	public static final String vertTrimmingHeader ="<105 \\\\Konturfraesen\\\\";
	public static final String horizBoring = "<103 \\\\BohrHoriz\\\\";
	public static final String contourRegex = "](\\d+)";
	public static final String contourElementRegex = "\\$E\\d+";
	public static final String millingOperationRegex = "(\\w+=\")(\\d+)(:\\d+\")";

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
	
	public NestingCreator(String xmlFile, String mprDirectory, ArrayList<String> errorMsg, JProgressBar bar){
		this.parser = new XMLParser(xmlFile);
		this.mprCount = this.parser.parse();
		this.layouts = parser.getLayouts();
		this.errorMsg = errorMsg;
		this.progressBar = bar;
		this.mprDirectory = mprDirectory;
	}

	public void createLayoutMprs () throws IOException{
		int currentMprCount = 0;
		for (Layout currentLayout : layouts){
			int currentContourIndex = 0;
			ArrayList<MprFile> mprs = currentLayout.getMprFiles();
			ArrayList<String> currentPlateContours = new ArrayList<String>();
			ArrayList<String> currentPlateOperations = new ArrayList<String>();
			ArrayList<String> currentPlateMillings = new ArrayList<String>();

			Point3D plateMeasurements = null;
			try {
				plateMeasurements = determinePlateMeasurements(currentLayout, mprs.get(0));
				if (plateMeasurements==null)
				{
					errorMsg.add("File " + currentLayout.getMprFiles().get(0).getPartCode() + " was not found in the current directory");
					errorMsg.add("Plate " + currentLayout.getNumber() + " Couldn't be created");
					//look for other mpr to take measurements!!!!!!
					continue;

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (MprFile currentMpr:mprs)
			{
				File mprFile = mprWriter.findFile(currentMpr.getPartCode(), mprDirectory);
				if (mprFile==null){
					errorMsg.add("File " + currentMpr.getPartCode() + " from plate #"
							+currentLayout.getNumber()+" was not found in the current directory");
					continue;
				}
				
				double xOffset = currentMpr.getXOffset();
				double yOffset = currentMpr.getYOffset();
				BufferedReader reader = null;;
				try {
					reader = new BufferedReader(new FileReader(mprFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<String> header = new ArrayList<String>();
				ArrayList<String> contours = new ArrayList<>();
				ArrayList<String> operations = new ArrayList<>();

				String currentLine = null;

				//seperate file header
				while (reader.ready())
				{
					currentLine = reader.readLine();
					if (!currentLine.matches(contourRegex) && !isOperation(currentLine)){
						header.add(currentLine);
					}
					else
						break;
				}
				//check if should flip xy coordinates
				boolean shouldFlip = mprParser.shouldFlip(currentMpr, header);
				//contour lines
				if (currentLine.matches(contourRegex)){
					while (reader.ready() && !isOperation(currentLine))
					{
						contours.add(currentLine);
						currentLine = reader.readLine();
					}
				}
				//operations lines
				while (reader.ready() && !currentLine.matches(fileEnd))
				{
					operations.add(currentLine);
					currentLine = reader.readLine();
				}
				reader.close();
				//break operations to arraylist for each operation
				ArrayList<ArrayList<String>> operationsBreaked = breakToOperations(operations);

				//if there are contours in the file, break the contours to seperate contours and extract the vertTrimming
				if (!contours.isEmpty())
				{
					ArrayList<ArrayList<String>> contoursBreaked = breakContours(contours, contourRegex);
					ArrayList<ArrayList<String>> vertMilling = extractSpecificOpType(operationsBreaked, vertTrimmingHeader);
					if (contoursBreaked.size() != vertMilling.size())
						errorMsg.add("ERROR!!! Contouring and milling not the same size at file :" + currentMpr.getPartCode());

					updateIndexesContours(contoursBreaked, currentContourIndex);
					updateIndexesMilling(vertMilling, currentContourIndex);
					currentContourIndex += contoursBreaked.size();
					
					int index =0;
					for (ArrayList<String> contour : contoursBreaked){
						ArrayList<ArrayList<String>> currentContourBreaked = breakContours(contour, contourElementRegex);
						if (shouldFlip)
							for (int i=0 ; i<currentContourBreaked.size() ; i++)
								mprParser.flipXYContour(currentContourBreaked.get(i), Double.toString(currentMpr.getLength()));
						
						if (xOffset>0 || yOffset>0)
							for (int i=0 ; i<currentContourBreaked.size() ; i++)
								mprParser.addOffsetToContour(currentContourBreaked.get(i), Double.toString(xOffset), Double.toString(yOffset));

						contour = new ArrayList<String>();
						uniteArrayLists(currentContourBreaked, contour);
						contoursBreaked.set(index, contour);
						index++;
					}

					uniteArrayLists(vertMilling, currentPlateMillings);
					uniteArrayLists(contoursBreaked, currentPlateContours);

				}

				//createLeftOverMPR
				ArrayList<ArrayList<String>> unSupportedOps = extractSpecificOpType(operationsBreaked, horizBoring);
				if (unSupportedOps!= null){
					uniteArrayLists(unSupportedOps, header);
					header.add(fileEnd);
					String fileName = currentMpr.getPartCode();
					mprWriter.createLeftOverMpr(header, mprDirectory, fileName); 
				}
				
				//create second side if needed
				if (currentMpr.getSecondPartCode().length()>1)
				{
					ArrayList<String> lines = mprWriter.readFileCompletly(currentMpr, mprDirectory);
					mprWriter.createLeftOverMpr(lines, mprDirectory, "Back_Ops_"+currentMpr.getPartCode());
				}

				
				//add relevant plate lines
				for (ArrayList<String> operation : operationsBreaked){
					if (shouldFlip)
						mprParser.flipXY(operation, Double.toString(currentMpr.getLength()));
					
					if (xOffset>0 || yOffset>0)
						mprParser.addOffsetToOperation(operation, Double.toString(xOffset), Double.toString(yOffset));

				}
				uniteArrayLists(operationsBreaked, currentPlateOperations);
				
				currentMprCount++;
				progressBar.setValue(currentMprCount/mprCount*100);
			}

			//createPlateFile:
			ArrayList<String> allPlateLines = new ArrayList<>();
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

	private void updateIndexesMilling(ArrayList<ArrayList<String>> millings, int currentPlateIndex){
		Pattern patt = Pattern.compile(millingOperationRegex);
		Matcher matcher;
		String currentLine;
		for (ArrayList<String> currentMill : millings)
		{
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
		if (!currentLine.matches(horizBoring) && !currentLine.matches(vertTrimmingHeader) && !currentLine.matches(supportedOps[0])&& !currentLine.matches(supportedOps[1]))
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
					System.out.println(match.group(PARAMETER_NAME).matches("DI"));
					if (match.group(PARAMETER_NAME).matches("DI"))
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
}
