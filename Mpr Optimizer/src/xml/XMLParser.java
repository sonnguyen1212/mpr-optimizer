package xml;

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

	private String xmlFileName;
	private Document document;
	private ArrayList<Layout> layoutsList;

	public XMLParser(String xmlFileName) {
		this.xmlFileName = xmlFileName;
		layoutsList = new ArrayList<>();
	}

	public void parse() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(xmlFileName);

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
				Element element = (Element) rootNodesList.item(i);
				int layoutNumber = Integer.parseInt(getTextValue(element, XML_LAYOUT_NUMBER));
				String layoutLengthString = getTextValue(element, XML_LAYOUT_LENGTH).replaceAll("\\D", "");
				String layoutWidthString = getTextValue(element, XML_LAYOUT_WIDTH).replaceAll("\\D", "");
				int layoutLength = Integer.parseInt(layoutLengthString);
				int layoutWidth = Integer.parseInt(layoutWidthString);

				Layout layout = new Layout(layoutNumber, layoutLength, layoutWidth);
				NodeList layoutsNodeList = element.getElementsByTagName(XML_PART);
				for (int j = 0; j < layoutsNodeList.getLength(); j++) {
					Element layoutElement = (Element) layoutsNodeList.item(j);
					String partCode = getTextValue(layoutElement, XML_PARTCODE);
					String description = getTextValue(layoutElement, XML_DESCRIPTION);
					String xOffsetString = getTextValue(layoutElement, XML_LEFT_OFFSET).replaceAll("\\D", "");
					String yOffsetString = getTextValue(layoutElement, XML_TOP_OFFSET).replaceAll("\\D", "");
					int xOffset = Integer.parseInt(xOffsetString);
					int yOffset = Integer.parseInt(yOffsetString);
					MprFile mprFile = new MprFile(partCode, description, xOffset, yOffset);
					layout.addMprFile(mprFile);
				}
				layoutsList.add(layout);
			}
		}
	}

	private String getTextValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	public ArrayList<Layout> getLayouts() {
		return layoutsList;
	}

	public static void main(String[] args) {
		XMLParser parser = new XMLParser("xml.xml");
		parser.parse();
		ArrayList<Layout> layouts = parser.getLayouts();
		for (Layout l : layouts) {
			System.out.println("Layout #" + l.getNumber() + " L: " + l.getLength() + " W: " + l.getWidth());
			for (MprFile file : l.getMprFiles()) {
				System.out.println(file.getPartCode() + " | " + file.getDescription() + " | " + file.getXOffset()
						+ " | " + file.getYOffset());
			}
			System.out.println();
		}

	}
}
