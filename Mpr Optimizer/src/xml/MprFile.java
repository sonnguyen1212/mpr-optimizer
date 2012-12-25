package xml;

public class MprFile {
	private double xOffset, yOffset;
	private double length, width;
	private String description;
	private String partCode;
	private String secondPartCode;


	public MprFile(String partCode, String secondPartCode, String description, double xOffset, double yOffset, double length, double width) {
		this.partCode = partCode;
		this.description = description;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.length = length;
		this.width = width;
		this.secondPartCode = secondPartCode;
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
	public String getSecondPartCode() {
		return secondPartCode;
	}

}
