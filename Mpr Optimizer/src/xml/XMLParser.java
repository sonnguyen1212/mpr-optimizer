package xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

	private static final String XML_LAYOUT = "Layout";
	private static final String XML_LAYOUT_NUMBER = "Number";
	private static final String XML_LAYOUT_LENGTH = "Length";
	private static final String XML_LAYOUT_WIDTH = "Width";
	private static final String XML_PART = "Part";
	private static final String XML_PARTCODE = "Partcode";
	private static final String XML_LEFT_OFFSET = "Left";
	private static final String XML_TOP_OFFSET = "Top";
	private static final String XML_DESCRIPTION = "Description";
	private static final String XML_PART_LENGTH = "Length";
	private static final String XML_PART_WIDTH = "Width";
	private static final String XML_PART_SECOND_PROG = "Notes";
	private static final String XML_REPLACEMENT_HEADER = "<?xml version=\"1.0\" encoding=\"Cp1252\"?>";

	private String xmlFileName;
	private Document document;
	private ArrayList<Layout> layoutsList;

	public XMLParser(String xmlFileName) {
		this.xmlFileName = xmlFileName;
		layoutsList = new ArrayList<>();
	}

	// returns number of mpr files in the xml
	public int parse() {
		int mprCount = 0;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		File newFile = null;
		try {
			newFile = replaceHeader(xmlFileName);
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(new FileInputStream(newFile), "Cp1252");

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Element docEle = document.getDocumentElement();

		NodeList rootNodesList = docEle.getElementsByTagName(XML_LAYOUT);
		if (rootNodesList != null && rootNodesList.getLength() > 0) {
			for (int i = 0; i < rootNodesList.getLength(); i++) {
				System.out.println("Layout " + i);
				Element element = (Element) rootNodesList.item(i);
				int layoutNumber = Integer.parseInt(getTextValue(element,
						XML_LAYOUT_NUMBER));
				String layoutLengthString = getTextValue(element,
						XML_LAYOUT_LENGTH).replaceAll("[^\\d\\.]", "");
				String layoutWidthString = getTextValue(element,
						XML_LAYOUT_WIDTH).replaceAll("[^\\d\\.]", "");
				double layoutLength = Double.parseDouble(layoutLengthString);
				double layoutWidth = Double.parseDouble(layoutWidthString);

				Layout layout = new Layout(layoutNumber, layoutLength,
						layoutWidth);
				NodeList layoutsNodeList = element
						.getElementsByTagName(XML_PART);
				for (int j = 0; j < layoutsNodeList.getLength(); j++) {
					System.out.println("-Part " + j);
					Element layoutElement = (Element) layoutsNodeList.item(j);
					String partCode = getTextValue(layoutElement, XML_PARTCODE);
					
					//verify that this part has a cnc program
					if (partCode==null)
						continue;
					else
						partCode = partCode.trim() + ".mpr";
					
					String description = getTextValue(layoutElement,
							XML_DESCRIPTION);
					String xOffsetString = getTextValue(layoutElement,
							XML_LEFT_OFFSET).replaceAll("[^\\d\\.]", "");
					String yOffsetString = getTextValue(layoutElement,
							XML_TOP_OFFSET).replaceAll("[^\\d\\.]", "");
					String partLengthString = getTextValue(layoutElement,
							XML_PART_LENGTH).replaceAll("[^\\d\\.]", "");
					String partWidthString = getTextValue(layoutElement,
							XML_PART_WIDTH).replaceAll("[^\\d\\.]", "");
					String secondPartCode = getTextValue(layoutElement,
							XML_PART_SECOND_PROG);
					double xOffset = Double.parseDouble(xOffsetString);
					double yOffset = Double.parseDouble(yOffsetString);
					double partLength = Double.parseDouble(partLengthString);
					double partWidth = Double.parseDouble(partWidthString);
					MprFile mprFile = new MprFile(partCode, secondPartCode,
							description, xOffset, yOffset, partLength,
							partWidth);
					mprCount++;
					layout.addMprFile(mprFile);
				}
				layoutsList.add(layout);
			}
		}
		if (newFile != null) {
			newFile.delete();
		}
		return mprCount;
	}

	private String getTextValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			if (el.getFirstChild() != null) {
				textVal = el.getFirstChild().getNodeValue();
			}
		}

		return textVal;
	}

	private File replaceHeader(String xmlFile) {
		File newFile = new File(xmlFile + "temp.xml");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					xmlFile)));
			BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
			writer.write(XML_REPLACEMENT_HEADER + "\n\r", 0,
					(XML_REPLACEMENT_HEADER + "\n\r").length());
			reader.readLine();
			String line = "";
			while ((line = reader.readLine()) != null) {
				writer.write(line, 0, line.length());
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newFile;
	}

	public ArrayList<Layout> getLayouts() {
		return layoutsList;
	}
}
