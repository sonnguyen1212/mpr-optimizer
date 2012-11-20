package xml;

public class MprFile {
	private double xOffset, yOffset;
	private double length, width;
	private String description;
	private String partCode;

	public MprFile(String partCode, String description, double xOffset, double yOffset, double length, double width) {
		this.partCode = partCode;
		this.description = description;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.length = length;
		this.width = width;
	}

	public String getPartCode() {
		return partCode;
	}

	public String getDescription() {
		return description;
	}

	public double getXOffset() {
		return xOffset;
	}

	public double getYOffset() {
		return yOffset;
	}
	
	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}

}
