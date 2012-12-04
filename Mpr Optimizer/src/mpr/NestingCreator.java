package mpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;

import xml.Layout;
import xml.MprFile;
import xml.XMLParser;

public class NestingCreator {
	public Pattern mprLine = Pattern.compile("(\\w)=(\\w)");
	public static final int PARAMETER_NAME = 0;
	public static final int PARAMETER_VALUE = 1;
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

	public void createLayoutMprs (){
		for (Layout currentLayout : layouts){
			
		}
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
