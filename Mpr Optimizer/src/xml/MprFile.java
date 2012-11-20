package xml;

public class MprFile {
	private int xOffset, yOffset;
	private String description;
	private String partCode;

	public MprFile(String partCode, String description, int xOffset, int yOffset) {
		this.partCode = partCode;
		this.description = description;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	public String getPartCode() {
		return partCode;
	}

	public String getDescription() {
		return description;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}
}
