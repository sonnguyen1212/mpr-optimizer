package xml;

import java.util.ArrayList;

public class Layout {
	private int number;
	private double  length, width;
	private ArrayList<MprFile> mprFiles;
	
	public Layout(int number, double length, double width) {
		this.number = number;
		this.length = length;
		this.width = width;
		this.mprFiles = new ArrayList<>();
	}
	
	public int getNumber() {
		return number;
	}
	
	public double getLength() {
		return length;
	}
	
	public double getWidth() {
		return width;
	}
	
	public ArrayList<MprFile> getMprFiles() {
		return mprFiles;
	}
	
	public void addMprFile(MprFile mprFile) {
		mprFiles.add(mprFile);
	}
}
