package mpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xml.Layout;
import xml.MprFile;
import xml.XMLParser;

public class NestingCreator {
	public Pattern mprLine = Pattern.compile("(\\w)=(\\w)");
	public static final int PARAMETER_NAME = 1;
	public static final int PARAMETER_VALUE = 2;
	public static final String partDimens = "<100 \\WerkStck\\";
	public static final String[] supportedOps = {"<102 \\BohrVert\\", "<109 \\Nuten\\"};
	public static final String vertTrimmingHeader ="<105 \\Konturfraesen\\";
	public static final String horizBoring = "<103 \\BohrHoriz\\";
	public static final String contourRegex = "]\\d+";
	public static final String fileEnd = "!";


	public static String PART_THICK = "DI";

	//private Layout currentLayout;`
	//MprFile firstFile;
	ArrayList<Layout> layouts;
	private XMLParser parser;
	String mprDirectory;

	public NestingCreator(String xmlFile, String mprDirectory){
		parser = new XMLParser(xmlFile);
		layouts = parser.getLayouts();
		this.mprDirectory = mprDirectory;
	}

	public void createLayoutMprs () throws IOException{
		for (Layout currentLayout : layouts){
			ArrayList<MprFile> mprs = currentLayout.getMprFiles();
			ArrayList<String> currentPlateLines = new ArrayList<String>();
			Point3D plateMeasurements;
			try {
				plateMeasurements = determinePlateMeasurements(currentLayout, mprs.get(0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (MprFile currentMpr:mprs)
			{
				File mprFile = mprWriter.findFile(currentMpr.getPartCode(), mprDirectory);
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

				while (reader.ready())
				{
					currentLine = reader.readLine();
					if (!currentLine.matches(contourRegex) && !isOperation(currentLine)){
						header.add(currentLine);
					}
					else
						break;
				}
				if (currentLine.matches(contourRegex)){
					while (reader.ready() && !isOperation(currentLine))
					{
						contours.add(currentLine);
						currentLine = reader.readLine();
					}
				}
				while (reader.ready() && !currentLine.matches(fileEnd))
				{
					operations.add(currentLine);
					currentLine = reader.readLine();
				}

				ArrayList<ArrayList<String>> operationsBreaked = breakToOperations(operations);
				if (!contours.isEmpty())
				{
					ArrayList<ArrayList<String>> contoursBreaked = breakContours(contours);
					ArrayList<String> vertMilling = extractSpecificOpType(operationsBreaked, vertTrimmingHeader);
				}

				//createLeftOverMPR
				ArrayList<String> unSupportedOps = extractSpecificOpType(operationsBreaked, horizBoring);
				if (unSupportedOps!= null){
					header.addAll(unSupportedOps);
					header.add(fileEnd);
					String fileName = currentMpr.getPartCode();
					mprWriter.createLeftOverMpr(header, fileName); 
				}
				
				
			}


		}
	}
	private ArrayList<ArrayList<String>> breakToOperations (ArrayList<String> allOperations){
		ArrayList<ArrayList<String>> operationsBreaked = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentOp = null;
		for (String line: allOperations)
		{
			if (!isOperation(line))
			{
				currentOp.add(line);
			}
			else
			{
				if (currentOp== null)
				{
					currentOp = new ArrayList<String>();
				}
				else
				{
					operationsBreaked.add(currentOp);
					currentOp = new ArrayList<String>();					
				}
			}
		}
		return operationsBreaked;
	}

	private ArrayList<ArrayList<String>> breakContours (ArrayList<String> allContours){
		ArrayList<ArrayList<String>> contoursBreaked = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentContour = null;
		for (String line: allContours)
		{
			if (!line.matches(contourRegex))
			{
				currentContour.add(line);
			}
			else
			{
				if (currentContour== null)
				{
					currentContour = new ArrayList<String>();
				}
				else
				{
					contoursBreaked.add(currentContour);
					currentContour = new ArrayList<String>();					
				}
			}
		}
		return contoursBreaked;
	}


	private boolean isOperation(String currentLine)
	{
		if (!currentLine.matches(horizBoring) && !currentLine.matches(vertTrimmingHeader) && !currentLine.matches(supportedOps[0])&& !currentLine.matches(supportedOps[0]))
			return false;
		else
			return true;
	}

	private ArrayList<String> extractSpecificOpType (ArrayList<ArrayList<String>> operationsBreaked, String regex)
	{
		ArrayList<String> operationType = new ArrayList<>();
		ArrayList<String> currentOp;
		Iterator<ArrayList<String>> iterator  = operationsBreaked.iterator();

		while (iterator.hasNext())
		{
			currentOp = iterator.next();
			if (currentOp.get(0).matches(regex))
			{
				operationsBreaked.remove(currentOp);
				operationType.addAll(currentOp); 
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
					if (match.group(PARAMETER_NAME).equals("DI"))
					{
						thickness = match.group(PARAMETER_VALUE);
						thickness = thickness.replaceAll("\"", "");
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
